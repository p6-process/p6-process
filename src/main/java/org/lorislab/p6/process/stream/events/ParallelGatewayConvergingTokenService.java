package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConvergingTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {

        String next = node.next.get(0);
//FIXME:
        ProcessToken gt = processTokenDAO.findByReferenceAndNodeName(token.parent, next);
        if (gt == null) {

            gt = new ProcessToken();
            gt.id = UUID.randomUUID().toString();
            gt.status = ProcessTokenStatus.CREATED;
            gt.processId = token.processId;
            gt.processVersion = token.processVersion;
            gt.nodeName = next;
//            gt.setCreateNodeName(next);
            gt.type = ProcessTokenType.valueOf(pd.nodes.get(next));

            gt.parent = token.parent;
            if (token.parent != null) {
                ProcessToken parent = processTokenDAO.findByGuid(token.parent);
                if (parent != null && parent.parent != null) {
                    gt.parent = parent.parent;
                }
            }
            gt.processInstance = token.processInstance;
            gt.reference = token.parent;
            gt.createdFrom.add(token.id);
            gt.messageId = messageId;
            gt.executionId = UUID.randomUUID().toString();
            processTokenDAO.create(gt);
        } else {
            // add finished parent
            gt.createdFrom.add(token.id);
            gt.data.putAll(token.data);
            processTokenDAO.update(gt);
        }

        // child token finished
        token.status = ProcessTokenStatus.FINISHED;
        processTokenDAO.update(token);

        int size1 = gt.createdFrom.size();
        int size2 = node.previous.size();
        log.info("Token finished {} node parents {}. Result {}>={}, {}", gt.createdFrom, node.previous, size1, size2, size1 >= size2);
        if (size1 >= size2) {
            log.info("Parallel gateway finished Token:{}", gt);
            return Collections.singletonList(gt);
        }
        return Collections.emptyList();
    }
}
