package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConvergingTokenService extends EventService {

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {

        String next = node.next.get(0);
//FIXME:
        ProcessToken gt = processTokenDAO.findByReferenceAndNodeName(token.getParent(), next);
        if (gt == null) {

            gt = new ProcessToken();
            gt.setId(UUID.randomUUID().toString());
            gt.setStatus(ProcessTokenStatus.CREATED);
            gt.setProcessId(token.getProcessId());
            gt.setProcessVersion(token.getProcessVersion());
            gt.setNodeName(next);
//            gt.setCreateNodeName(next);
            gt.setType(ProcessTokenType.valueOf(pd.nodes.get(next)));

            gt.setParent(token.getParent());
            if (token.getParent() != null) {
                ProcessToken parent = processTokenDAO.findByGuid(token.getParent());
                if (parent != null && parent.getParent() != null) {
                    gt.setParent(parent.getParent());
                }
            }
            gt.setProcessInstance(token.getProcessInstance());
            gt.setReference(token.getParent());
            gt.getCreatedFrom().add(token.getId());
            gt.setMessageId(messageId);
            gt.setExecutionId(UUID.randomUUID().toString());
            processTokenDAO.create(gt);
        } else {
            // add finished parent
            gt.getCreatedFrom().add(token.getId());
            gt.getData().putAll(token.getData());
            processTokenDAO.update(gt);
        }

        // child token finished
        token.setStatus(ProcessTokenStatus.FINISHED);
        processTokenDAO.update(token);

        int size1 = gt.getCreatedFrom().size();
        int size2 = node.previous.size();
        log.info("Token finished {} node parents {}. Result {}>={}, {}", gt.getCreatedFrom(), node.previous, size1, size2, size1 >= size2);
        if (size1 >= size2) {
            log.info("Parallel gateway finished Token:{}", gt);
            return Collections.singletonList(gt);
        }
        return Collections.emptyList();
    }
}
