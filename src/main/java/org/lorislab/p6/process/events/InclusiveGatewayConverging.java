package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;

@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.INCLUSIVE_GATEWAY_CONVERGING)
public class InclusiveGatewayConverging implements EventService {
    
    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        item.moveToNext();
        return uni(item);
    }
}
