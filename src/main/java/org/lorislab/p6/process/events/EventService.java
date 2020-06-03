package org.lorislab.p6.process.events;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.reactive.ExecutorItem;

public interface EventService {

    Uni<ExecutorItem> execute(ExecutorItem item);
}