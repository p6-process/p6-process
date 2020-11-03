package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.reactive.ExecutorItem;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConverging implements EventService {

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Override
    public Uni<ExecutorItem> execute(ExecutorItem item) {
        String next = item.node.next.get(0);
        return Uni.createFrom().nullItem();
//        return processTokenDAO
//                .findByReferenceAndNodeName(item.tx, item.token.parent, next)
//                .onItem().apply(x -> {
//
//                    ProcessToken token;
//                    if (x == null) {
//                        // create token
//                        token = new ProcessToken();
//                        token.id = UUID.randomUUID().toString();
//                        token.status = ProcessToken.Status.CREATED;
//                        token.processId = item.token.processId;
//                        token.processVersion = item.token.processVersion;
//                        token.nodeName = next;
//                        token.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));
//                        token.parent = item.token.parent;
//                        token.processInstance = item.token.processInstance;
//                        token.reference = token.parent;
//                        token.createdFrom.add(item.token.id);
//                        token.data = item.token.data;
//
////                        if (token.parent != null) {
////                            ProcessToken parent = processTokenDAO.findByGuid(token.parent);
////                            if (parent != null && parent.parent != null) {
////                                token.parent = parent.parent;
////                            }
////                        }
//
//                        item.createTokens = Collections.singletonList(token);
//                    } else {
//                        // update token
//                        token = x;
//                        token.createdFrom.add(item.token.id);
//                        token.data.putAll(item.token.data);
//                        item.updateToken = token;
//                    }
//
//
//                    // create message if we finished the gateway
//                    int size1 = token.createdFrom.size();
//                    int size2 = item.node.previous.size();
//                    log.info("Token finished {} node parents {}. Result {}>={}, {}", token.createdFrom, item.node.previous, size1, size2, size1 >= size2);
//                    if (size1 >= size2) {
//                        log.info("Parallel gateway finished Token:{}", token);
//                        item.messages.add(token);
//                    }
//
//                    // finished current token
//                    item.token.status = ProcessToken.Status.FINISHED;
//                    item.moveToNextItem(null);
//                    return item;
//                });
    }


}
