package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.ProcessTokenContentDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.ExclusiveGateway;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.ConstraintDAOException;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.EXCLUSIVE_GATEWAY_DIVERGING)
public class ExclusiveGatewayDivergingTokenService extends EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {

        ProcessTokenContent ptc = processTokenContentDAO.findBy(token.getGuid());
        Map<String, Object> data = DataUtil.deserialize(ptc.getData());

        String item = null;
        ExclusiveGateway gateway = (ExclusiveGateway) node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (item == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), data);
            if (tmp) {
                item = key;
            }
        }
        log.info("ExclusiveGateway node: {} next: {} default: {}", node.name, item, gateway.defaultSequence);
        if (item == null) {
            item = gateway.defaultSequence;
        }


        // child token of the gateway node
        ProcessToken child = new ProcessToken();
        child.setNodeName(item);
        child.setProcessId(token.getProcessId());
        child.setProcessVersion(token.getProcessVersion());
        child.setCreateNodeName(item);
        child.setPreviousName(token.getNodeName());
        child.setParent(token.getGuid());
        child.setType(pd.getNodeProcessTokenType(item));
        child.setProcessInstanceGuid(token.getProcessInstanceGuid());
        child.setStatus(ProcessTokenStatus.IN_EXECUTION);

        // close the gateway diverging token
        token.setStatus(ProcessTokenStatus.FINISHED);
        processTokenRepository.update(token);

        // create the child token
        try {
            child = processTokenRepository.create(child, true);

            ProcessTokenContent pt = new ProcessTokenContent();
            pt.setGuid(child.getGuid());
            pt.setData(ptc.getData());

            processTokenContentDAO.create(pt);
        } catch (ConstraintDAOException ex) {
            log.warn("Tokens are already created. Task {}/{}", token.getGuid(), token.getNodeName());
            return processTokenRepository.findAllTokensInExecution(token.getProcessInstanceGuid(), token.getGuid());
        }
        return Collections.singletonList(child);
    }
}
