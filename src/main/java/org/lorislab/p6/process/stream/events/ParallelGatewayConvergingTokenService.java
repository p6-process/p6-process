package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConvergingTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {

        String next = node.sequence.to.get(0);
//FIXME:
        ProcessToken gt = processTokenDAO.findByReferenceAndCreateNodeName(token.parent, next);
        if (gt == null) {
            ProcessToken parent = processTokenDAO.findByGuid(token.parent);
            gt = new ProcessToken();
            gt.status = ProcessTokenStatus.CREATED;
            gt.processId = token.processId;
            gt.processVersion = token.processVersion;
            gt.nodeName = next;
//            gt.setCreateNodeName(next);
            gt.type = ProcessTokenType.valueOf(pd.nodes.get(next));
            gt.parent = parent.parent;
            gt.processInstance = token.processInstance;
//            gt.setReferenceTokenGuid(token.getParent());
            gt.createdFrom.add(token.guid);
            gt.messageId = messageId;
            gt.executionId = UUID.randomUUID().toString();
            gt = processTokenDAO.create(gt);
        } else {
            // add finished parent
            gt.createdFrom.add(token.guid);
            gt.data.putAll(token.data);
            gt = processTokenDAO.update(gt);
        }

        // child token finished
        token.status = ProcessTokenStatus.FINISHED;

        int size1 = gt.createdFrom.size();
        int size2 = node.sequence.from.size();
        log.info("Token finished {} node parents {}. Result {}>={}, {}", gt.createdFrom, node.sequence.from, size1, size2, size1 >= size2);
        if (size1 >= size2) {
            log.info("Parallel gateway finished Token:{}", gt);
            return Collections.singletonList(gt);
        }
        return Collections.emptyList();
    }
}
