package org.lorislab.p6.process.reactive;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.pgclient.pubsub.PgSubscriber;
import io.vertx.pgclient.PgConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.MessageType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class SingletonExecutor {

    @Inject
    PgPool client;

    @Inject
    MessageDAO messageDAO;

    @Inject
    PgConnectOptions pgConnectOptions;

    @Inject
    TokenService tokenService;

    @Inject
    Vertx vertx;

    public void start() {
        Multi.createBy().merging().streams(messageDAO.findAllMessages(MessageType.SINGLETON_MSG), subscriber())
                .onItem().produceUni(x -> execute())
                .concatenate()
                .subscribe().with(m -> log.info("Executed singleton {} ", m), Throwable::printStackTrace);
    }

    private Multi<String> subscriber() {
        return Multi.createFrom().emitter(emitter -> {
            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, pgConnectOptions);
            subscriber.connect().subscribe().with(c -> {
                subscriber.channel(MessageType.SINGLETON_MSG.channel).handler(emitter::emit);
            }, Throwable::printStackTrace);
        });
    }

    public Uni<Message> execute() {
        return client.begin().flatMap(tx -> messageDAO.nextSingletonMessage(tx)
                .onItem().apply(m -> {
                    if (m == null) {
                        tx.close();
                        return Uni.createFrom().item((Message) null);
                    }
                    return tokenService.executeMessage(tx, m)
                            .onItem().apply(u -> tx.commit().onItem().apply(x -> u))
                            .flatMap(x -> x);
                }).flatMap(x -> x)
        );
    }

}
