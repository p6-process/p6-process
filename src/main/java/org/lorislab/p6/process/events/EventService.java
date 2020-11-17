package org.lorislab.p6.process.events;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.token.RuntimeToken;

public interface EventService {

    Uni<RuntimeToken> execute(RuntimeToken item);

    default Uni<RuntimeToken> uni(RuntimeToken item) {
        return Uni.createFrom().item(item);
    }
}
