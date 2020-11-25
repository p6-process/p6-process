package org.lorislab.p6.process.message;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.pgclient.pubsub.PgSubscriber;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.pgclient.PgConnectOptions;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public abstract class MessageListener {

    @Inject
    PgConnectOptions pgConnectOptions;

    @Inject
    Vertx vertx;

    @Inject
    MessageRepository messageRepository;

    @Inject
    PgPool pool;

    protected abstract String name();

    protected abstract Uni<Long> onMessage(Transaction tx, Message message);

    public void start() {
        Multi.createBy().merging().streams(messageRepository.findAllMessages(name()), subscriber())
                .onItem().transformToUni(x -> execute())
                .concatenate()
                .subscribe().with(m -> log.info("Executed queue '{}' message {} ", name(), m), Throwable::printStackTrace);
    }

    private Multi<String> subscriber() {
        return Multi.createFrom().emitter(emitter -> {
            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, pgConnectOptions);
            subscriber
                    .connect()
                    .subscribe()
                    .with(c -> subscriber.channel(name()).handler(emitter::emit), Throwable::printStackTrace);
        });
    }

    public Uni<Long> execute() {
        return pool.begin().flatMap(tx -> messageRepository.nextProcessMessage(tx, name())
                .onItem().transform(m -> {
                    if (m == null) {
                        tx.close();
                        return Uni.createFrom().item((Long) null);
                    }
                    log.info("Queue {} message: {}", m.queue, m);
                    return onMessage(tx, m)
                            .onItem()
                            .transform(u -> tx.commit().onItem().transform(x -> u))
                            .flatMap(x -> x);
                }).flatMap(x -> x)
        );
    }

}
