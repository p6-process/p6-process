package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ExclusiveGateway;
import org.lorislab.p6.process.model.InclusiveGateway;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.INCLUSIVE_GATEWAY_DIVERGING)
public class InclusiveGatewayDiverging implements EventService {

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {

        List<String> items = new ArrayList<>();

        // find the next sequence name
        InclusiveGateway gateway = (InclusiveGateway) item.node;
        for (Map.Entry<String, String> condition : gateway.condition.entrySet()) {
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.getValue(), item.token.data.getMap());
            if (tmp) {
                items.add(condition.getKey());
            }
        }
        log.info("InclusiveGateway node: {} next: {} default: {}", item.node.name, items, gateway.defaultNext);
        if (items.isEmpty()) {
            items.add(gateway.defaultNext);
        }

        items.forEach(next -> {
            ProcessToken tmp = createToken(next, item);
            item.changeLog.addMessage(tmp);
            item.changeLog.tokens.add(tmp);
        });
        item.token.status = ProcessToken.Status.FINISHED;
        item.token.finished = LocalDateTime.now();

        item.savePoint = true;
        item.moveToNull();
        return uni(item);
    }
}
