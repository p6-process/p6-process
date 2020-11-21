package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.model.ProcessTokenRepository;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConverging implements EventService {

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        return processTokenRepository
                .findByReferenceAndNodeName(item.tx, item.token.parent, item.node.next.get(0))
                .onItem().transformToUni(t -> {
                    ProcessToken token;
                    if (t == null) {
                        token = createToken(item);
                        item.changeLog.tokens.add(token);
                    } else {
                        token = t;
                        item.changeLog.updateToken = token;
                    }
                    token.createdFrom.add(item.token.id);
                    token.data.getMap().putAll(item.token.data.getMap());

                    // create message if we finished the gateway
                    int size1 = token.createdFrom.size();
                    int size2 = item.node.previous.size();
                    log.info("Token finished {} node parents {}. Result {}>={}, {}", token.createdFrom, item.node.previous, size1, size2, size1 >= size2);
                    if (size1 >= size2) {
                        log.info("Parallel gateway finished Token:{}", token);
                        item.changeLog.addMessage(token);
                    }

                    // finished current token
                    item.token.status = ProcessToken.Status.FINISHED;
                    item.savePoint = true;
                    item.moveToNull();
                    return uni(item);
                });
    }

    private ProcessToken createToken(RuntimeToken item) {
        String next = item.node.next.get(0);
        ProcessToken token = new ProcessToken();
        token.processId = item.token.processId;
        token.processVersion = item.token.processVersion;
        token.nodeName = next;
        token.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));
        token.parent = item.token.parent;
        token.processInstance = item.token.processInstance;
        token.reference = token.parent;
        token.createdFrom.add(item.token.id);
        token.data = item.token.data;
//        if (token.parent != null) {
//            ProcessToken parent = processTokenDAO.findByGuid(token.parent);
//            if (parent != null && parent.parent != null) {
//                token.parent = parent.parent;
//            }
//        }
        return token;
    }

}
