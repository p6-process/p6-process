package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.ProcessTokenContentDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.quarkus.jel.jpa.exception.ConstraintDAOException;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDivergingTokenService extends EventService {

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

        // child tokens of the gateway node
        List<String> next = node.sequence.to;
        List<ProcessToken> tokens = next.stream().map(item -> {
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
            return child;
        }).collect(Collectors.toList());

        // close the gateway diverging token
        token.setStatus(ProcessTokenStatus.FINISHED);
        processTokenRepository.update(token);

        // create the child tokens
        try {
            processTokenRepository.create(tokens, true);

            List<ProcessTokenContent> contents = tokens.stream()
                    .map(ProcessToken::getGuid)
                    .map(x -> {
                        ProcessTokenContent pt = new ProcessTokenContent();
                        pt.setGuid(x);
                        pt.setData(ptc.getData());
                        return pt;
                    }).collect(Collectors.toList());

            processTokenContentDAO.create(contents);
        } catch (ConstraintDAOException ex) {
            log.warn("Tokens are already created. Task {}/{}", token.getGuid(), token.getNodeName());
            tokens = processTokenRepository.findAllTokensInExecution(token.getProcessInstanceGuid(), token.getGuid());
        }
        return tokens;
    }
}
