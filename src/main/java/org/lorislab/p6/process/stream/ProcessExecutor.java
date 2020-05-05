package org.lorislab.p6.process.stream;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.MessageMapperImpl;

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

    Long timerId;

    @Inject
    PgPool client;

    void onStart(@Observes StartupEvent ev) {
        timerId = vertx.setTimer(TimeUnit.SECONDS.toMillis(scheduler), this::action);
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (timerId != null) {
            vertx.cancelTimer(timerId);
        }
    }

    void action(Long id) {
        log.info("Process time execution {}", id);
        Uni.createFrom().nullItem()
                .onItem().produceUni(i -> execute())
                .repeat().until(m -> m.message == null)
                .onCompletion().invoke(() -> {
                    System.out.println("Start timer");
                    timerId = vertx.setTimer(TimeUnit.SECONDS.toMillis(scheduler), this::action);
                    System.out.println("Timer ID " + timerId);
                })
                .subscribe().with(m -> {
                    System.out.println("Execute " + m);
                },
                f -> {
                    f.printStackTrace();
                });
    }

    private static final String SELECT_PROCESS_MESSAGE = "DELETE FROM PROCESS_MSG WHERE id = (SELECT id FROM PROCESS_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref";

    private Uni<TimeExecutor> execute() {
        log.info("Execute time");
        return processMessage().onItem().apply(TimeExecutor::new);
    }

    private Uni<Message> processMessage() {
        log.info("Process message");
        return client.begin()
                .flatMap(tx -> tx.query(SELECT_PROCESS_MESSAGE)
                        .map(RowSet::iterator)
                        .map(it -> it.hasNext() ? it.next() : null)
                        .map(MessageMapperImpl::mapS)
                        .onItem().apply(m -> tx.commit()
                                        .onItem().apply(x -> m)
                                ).flatMap(x -> x)
                        );
    }

    public static class TimeExecutor {
        Message message;
        public TimeExecutor(Message message) {
            this.message = message;
        }
    }
}
