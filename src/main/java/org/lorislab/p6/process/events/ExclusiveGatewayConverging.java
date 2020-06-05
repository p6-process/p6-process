package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.reactive.ExecutorItem;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.EXCLUSIVE_GATEWAY_CONVERGING)
public class ExclusiveGatewayConverging implements EventService {

    @Override
    public Uni<ExecutorItem> execute(ExecutorItem item) {
        item.moveToNextItem(item.node.next.get(0));
        return Uni.createFrom().item(item.copy());
    }
}
