package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.stream.reactive.ExecutorItem;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.START_EVENT)
public class StartEvent implements EventService {

    @Override
    public Uni<ExecutorItem> execute(ExecutorItem item) {
        item.moveToNextItem(item.node.next.get(0));

        ExecutorItem i = new ExecutorItem();
        i.end = item.end;
        i.msg = item.msg;
        i.node = item.node;
        i.pd = item.pd;
        i.token = item.token;
        i.tx = item.tx;
        log.info("#### START {}", i.token.type);
        return Uni.createFrom().item(i);
    }
}
