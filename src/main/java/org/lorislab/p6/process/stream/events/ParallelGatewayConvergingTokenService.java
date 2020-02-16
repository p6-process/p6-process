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
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConvergingTokenService extends EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {

        String next = node.sequence.to.get(0);

        ProcessTokenContent ptc = processTokenContentDAO.findBy(token.getGuid());
        ProcessToken gatewayToken = processTokenRepository.findByReferenceAndCreateNodeName(token.getParent(), next);
        if (gatewayToken == null) {
            ProcessToken parent = processTokenRepository.findBy(token.getParent());
            gatewayToken = new ProcessToken();
            gatewayToken.setStatus(ProcessTokenStatus.CREATED);
            gatewayToken.setProcessId(token.getProcessId());
            gatewayToken.setProcessVersion(token.getProcessVersion());
            gatewayToken.setNodeName(next);
            gatewayToken.setCreateNodeName(next);
            gatewayToken.setType(pd.getNodeProcessTokenType(next));
            gatewayToken.setParent(parent.getParent());
            gatewayToken.setProcessInstanceGuid(token.getProcessInstanceGuid());
            gatewayToken.setReferenceTokenGuid(token.getParent());
            gatewayToken.getCreatedFrom().add(token.getGuid());
            gatewayToken = processTokenRepository.create(gatewayToken, true);

            ProcessTokenContent pc = new ProcessTokenContent();
            pc.setGuid(gatewayToken.getGuid());
            pc.setData(ptc.getData());
            processTokenContentDAO.create(pc);
        } else {
            // add finished parent
            gatewayToken.getCreatedFrom().add(token.getGuid());
            gatewayToken = processTokenRepository.update(gatewayToken);
            // merge the DATA from to token to gateway token (last token win)
            ProcessTokenContent pt = processTokenContentDAO.findBy(gatewayToken.getGuid());
            pt.setData(DataUtil.merge(pt.getData(), ptc.getData()));
            processTokenContentDAO.update(pt);
        }

        // child token finished
        token.setStatus(ProcessTokenStatus.FINISHED);
        processTokenRepository.update(token);

        int size1 = gatewayToken.getCreatedFrom().size();
        int size2 = node.sequence.from.size();
        log.info("Token finished {} node parents {}. Result {}>={}, {}", gatewayToken.getCreatedFrom(), node.sequence.from, size1, size2, size1 >= size2);
        if (size1 >= size2) {
            log.info("Parallel gateway finished Token:{}", gatewayToken);
            return Collections.singletonList(gatewayToken);
        }
        return Collections.emptyList();
    }
}
