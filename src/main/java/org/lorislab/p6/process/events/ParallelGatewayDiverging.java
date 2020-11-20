package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.lorislab.p6.process.model.ProcessDefinition;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDiverging implements EventService {

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        item.changeLog.tokens = createChildTokens(item.token, item.pd, item.node.next);
        item.changeLog.messages = item.changeLog.tokens;
        item.token.status = ProcessToken.Status.FINISHED;
        item.savePoint = true;
        item.moveToNull();
        return uni(item);
    }

    protected List<ProcessToken> createChildTokens(ProcessToken token, ProcessDefinition pd, List<String> items) {
        return items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
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
