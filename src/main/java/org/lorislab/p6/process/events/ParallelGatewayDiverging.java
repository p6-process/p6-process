package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.reactive.ExecutorItem;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDiverging implements EventService {

    @Override
    public Uni<ExecutorItem> execute(ExecutorItem item) {

        item.createTokens = createChildTokens(item.token, item.pd, item.node.next);
        item.messages = item.createTokens;

        item.token.status = ProcessToken.Status.FINISHED;
        item.moveToNextItem(null);
        return Uni.createFrom().item(item);

    }

    protected List<ProcessToken> createChildTokens(ProcessToken token, ProcessDefinitionRuntime pd, List<String> items) {
        return items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.id = UUID.randomUUID().toString();
            child.nodeName = item;
            child.processId = token.processId;
            child.processVersion = token.processVersion;
            child.parent = token.id;
            child.type = ProcessToken.Type.valueOf(pd.nodes.get(item));
            child.processInstance = token.processInstance;
            child.data = token.data;
            child.status= ProcessToken.Status.IN_EXECUTION;
            return child;
        }).collect(Collectors.toList());
    }
}
