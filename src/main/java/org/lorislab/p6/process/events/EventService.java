package org.lorislab.p6.process.events;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.model.ProcessDefinition;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

public interface EventService {

    Uni<RuntimeToken> execute(RuntimeToken item);

    default Uni<RuntimeToken> uni(RuntimeToken item) {
        return Uni.createFrom().item(item);
    }

    default ProcessToken createToken(String next, RuntimeToken item) {
        ProcessToken child = new ProcessToken();
        child.nodeName = next;
        child.processId = item.token.processId;
        child.processVersion = item.token.processVersion;
        child.processInstance = item.token.processInstance;
        child.status = ProcessToken.Status.IN_EXECUTION;
        child.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));
        child.data = item.token.data;
        child.createdFrom.add(item.token.id);
        child.previousFrom.add(item.token.nodeName);
        return child;
    }
}
