package org.lorislab.p6.process.reactive;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class TokenExecutor {


//    @Inject
//    MessageDAO messageDAO;
//
//    @Inject
//    TokenService tokenService;
//
//    @Inject
//    Vertx vertx;
//
//    public void start() {
//        Multi.createBy().merging().streams(messageDAO.findAllMessages(MessageType.TOKEN_MSG), subscriber())
//                .onItem().produceUni(x -> execute())
//                .concatenate()
//                .subscribe().with(m -> log.info("Executed message {} ", m), Throwable::printStackTrace);
//    }
//
//    private Multi<String> subscriber() {
//        return Multi.createFrom().emitter(emitter -> {
//            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, pgConnectOptions);
//            subscriber.connect().subscribe().with(c -> {
//                subscriber.channel(MessageType.TOKEN_MSG.channel).handler(emitter::emit);
//            }, Throwable::printStackTrace);
//        });
//    }
//
//    public Uni<Message> execute() {
//        return client.begin().flatMap(tx -> messageDAO.nextTokenMessage(tx)
//                .onItem().apply(m -> {
//                    if (m == null) {
//                        tx.close();
//                        return Uni.createFrom().item((Message) null);
//                    }
//                    return tokenService.executeMessage(tx, m)
//                            .onItem().apply(u -> tx.commit().onItem().apply(x -> u))
//                            .flatMap(x -> x);
//                }).flatMap(x -> x)
//        );
//    }


}
