package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.lorislab.p6.process.model.ProcessDefinition;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDiverging implements EventService {

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        item.node.next.forEach(next -> {
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
