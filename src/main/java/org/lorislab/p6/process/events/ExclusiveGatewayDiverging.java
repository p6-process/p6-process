package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.model.ExclusiveGateway;
import org.lorislab.p6.process.reactive.ExecutorItem;
import org.lorislab.p6.process.reactive.ProcessExpressionHelper;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.EXCLUSIVE_GATEWAY_DIVERGING)
public class ExclusiveGatewayDiverging implements EventService {

    @Override
    public Uni<ExecutorItem> execute(ExecutorItem item) {
        String next = null;
        ExclusiveGateway gateway = (ExclusiveGateway) item.node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (next == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), item.token.data.getMap());
            if (tmp) {
                next = key;
            }
        }

        log.info("ExclusiveGateway node: {} next: {} default: {}", item.node.name, next, gateway.defaultNext);
        if (next == null) {
            next = gateway.defaultNext;
        }

        item.moveToNextItem(next);
        return Uni.createFrom().item(item.copy());
    }
}
