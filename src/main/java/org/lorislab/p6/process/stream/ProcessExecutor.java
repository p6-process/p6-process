package org.lorislab.p6.process.stream;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class ProcessExecutor {

    @ConfigProperty(name = "process.token.executor.scheduler", defaultValue = "10")
    Long scheduler;

    @Inject
    Vertx vertx;

    Long id;

    void onStart(@Observes StartupEvent ev) {
        id = vertx.setTimer(TimeUnit.SECONDS.toMillis(scheduler), this::action);
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (id != null) {
            vertx.cancelTimer(id);
        }
    }

    void action(Long id) {
        log.info("TIMER {}", id);
        id = vertx.setTimer(TimeUnit.SECONDS.toMillis(scheduler), this::action);
        log.info("NEW TIMER {}", id);
    }
}
