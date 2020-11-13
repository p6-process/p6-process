package org.lorislab.p6.process.events;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.token.RuntimeToken;

public interface EventService {

    Uni<RuntimeToken> execute(RuntimeToken item);
}
