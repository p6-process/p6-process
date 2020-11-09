package org.lorislab.p6.process.reactive;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.pgclient.pubsub.PgSubscriber;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.pgclient.PgConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.ProcessQueueDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessQueue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class ProcessExecutor {

//    @Inject
//    DeploymentService deploymentService;
//
//    @Inject
//    ProcessInstanceDAO processInstanceDAO;
//
//    @Inject
//    MessageDAO messageDAO;
//
    @Inject
    PgConnectOptions pgConnectOptions;

    @Inject
    Vertx vertx;

    @Inject
    ProcessQueueDAO processQueueDAO;

    @Inject
    PgPool pool;

    public void start() {
        Multi.createBy().merging().streams(processQueueDAO.findAllMessages(), subscriber())
                .onItem().transformToUni(x -> execute())
                .concatenate()
                .subscribe().with(m -> log.info("Executed message {} ", m), Throwable::printStackTrace);
    }

    private Multi<String> subscriber() {
        return Multi.createFrom().emitter(emitter -> {
            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, pgConnectOptions);
            subscriber.connect().subscribe().with(c -> {
                subscriber.channel(ProcessQueueDAO.REQUEST_CHANNEL).handler(emitter::emit);
            }, Throwable::printStackTrace);
        });
    }

    public Uni<Long> execute() {
        return pool.begin().flatMap(tx -> processQueueDAO.nextProcessMessage(tx)
                .onItem().transform(m -> {
                    if (m == null) {
                        tx.close();
                        return Uni.createFrom().item((Long) null);
                    }
                    return executeMessage(tx, m)
                            .onItem()
                            .transform(u -> tx.commit().onItem().transform(x -> u))
                            .flatMap(x -> x);
                }).flatMap(x -> x)
        );
    }

    private Uni<Long> executeMessage(Transaction tx, ProcessQueue m) {
        log.info("Process message: {}", m);
        return Uni.createFrom().item(m.id);
    }

//    public Multi<Response> subscribe() {
//        System.out.println("_--------- client to group test");
//        return Multi.createFrom().emitter(emitter -> {
//
//            client.xreadgroup(List.of("GROUP", "group1", "client1", "COUNT", "1", "STREAMS", "stream1", ">"))
////                    .call(s -> s.getKeys().forEach(i -> emitter.emit(i));
//                    .onItem().invoke(c -> {
////                    .subscribe().with(c -> {
//                        if (c == null || c.size() == 0) {
//                            return;
//                        }
//
//                        System.out.println("# " + c);
////                        String tmp = c.get(0).get(1).get(0).get(0).toString();
////                        System.out.println("# " + tmp);
//                        int size = c.get(0).get(1).size();
//                        for (int i=0; i<size; i++) {
//                            Response a = c.get(0).get(1).get(i);
//                            System.out.println("Emitter: " + a);
//                            emitter.emit(a);
//                        }
//            });
//
////                    .transformToMulti(i -> {
////                    if (i == null || i.size() == 0) {
////                        return null;
////                    }
////                    return i.getKeys().stream();
////                }
////                    .call(emitter::emit);
//            }, BackPressureStrategy.ERROR);
//        }

//        return client.xread(List.of("COUNT", "1", "STREAMS", "stream1", "0"))
//                .onItem().transformToMulti(i -> {
//                    if (i == null || i.size() == 0) {
//                        return Multi.createFrom().nothing();
//                    }
//                    return Multi.createFrom().items(i.getKeys().stream());
//                });
//    }

//    public Uni<Message> execute(Response response) {
//        Message m = new Message();
//        m.id = response.get(0).toString();
//        System.out.println("########## " + m.id);
//        return client.xack(List.of("stream1","group1",m.id)).onItem().transform(r -> m);
//    }

//    public Uni<String> execute(KeyValue value) {
//
//        ByteSequence key = value.getKey();
//        String tmp = key.toString(StandardCharsets.UTF_8);
//        ByteSequence newKey = ByteSequence.from(tmp.replaceFirst("pending", "running").getBytes());
//        System.out.println("----> EVENT " + tmp + " - " + value.getValue().toString(StandardCharsets.UTF_8));
//
//        return Uni.createFrom().completionStage(lease.grant(20L))
//                .flatMap(r -> Uni.createFrom().completionStage(
//                        client.txn()
//                            .If(
//                                new Cmp(key, Cmp.Op.GREATER, CmpTarget.version(0)),
//                                new Cmp(newKey, Cmp.Op.EQUAL, CmpTarget.version(0))
//                            ).Then(
//                                Op.put(newKey, value.getValue(), PutOption.newBuilder().withLeaseId(r.getID()).build()),
//                                Op.delete(key, DeleteOption.DEFAULT)
//                            ).commit()
//                    ).map(x -> tmp) //TODO if ok execute process
//                );
//    }
//
//    private Multi<KeyValue> subscriber() {
//        ByteSequence key = ByteSequence.from("/p6/pi/pending".getBytes());
//        return Multi.createFrom().emitter(emitter ->
//            watch.watch(key,
//                    WatchOption.newBuilder().withNoDelete(true).withPrefix(key).build(),
//                    x -> x.getEvents().forEach(e -> emitter.emit(e.getKeyValue())))
//        );
//    }
//
//    private Multi<KeyValue> running() {
//        ByteSequence key = ByteSequence.from("/p6/pi/running".getBytes());
//        return Multi.createFrom().emitter(emitter ->
//            watch.watch(key,
//                    WatchOption.newBuilder().withNoDelete(false).withPrefix(key).withPrevKV(true).withNoPut(true).build(),
//                    x -> x.getEvents().forEach(e -> emitter.emit(e.getPrevKV())))
//        );
//    }
//
//    public Uni<String> checkRunningItem(KeyValue item) {
//        ByteSequence key = item.getKey();
//        String tmp = key.toString(StandardCharsets.UTF_8);
//        return Uni.createFrom().completionStage(lease.timeToLive(item.getLease(), LeaseOption.DEFAULT))
//                .map(r -> r.getTTl() == -1)
//                .onItem().produceUni(expired -> {
//                    System.out.println("----> EVENT RUNNING " + tmp + " - " + expired + " - " + item.getValue().toString(StandardCharsets.UTF_8));
//                   if (!expired) {
//                       return Uni.createFrom().item(tmp);
//                   } else {
//                       Message msg = Json.decodeValue(item.getValue().toString(StandardCharsets.UTF_8), Message.class);
//                       msg.count = msg.count + 1;
//                       ByteSequence value = ByteSequence.from(Json.encode(msg).getBytes());
//                       ByteSequence pk = ByteSequence.from(tmp.replaceFirst("running", "pending").getBytes());
//                       ByteSequence qk = ByteSequence.from(tmp.replaceFirst("running", "queue").getBytes());
//                       return Uni.createFrom().completionStage(
//                         client.txn()
//                                 .If(new Cmp(pk, Cmp.Op.EQUAL, CmpTarget.version(0)))
//                                 .Then(
//                                         Op.put(pk, value, PutOption.DEFAULT),
//                                         Op.put(qk, value, PutOption.DEFAULT)
//                                 ).commit()
//                       ).map(r -> tmp);
//                   }
//                });
//    }


//
//    private Uni<Message> executeMessage(Transaction tx, Message m) {
//        log.info("Process message: {}", m);
//        if (MessageCmd.START_PROCESS.equals(m.cmd)) {
//            return createTokens(tx, m.ref).onItem()
//                    .apply(t -> processTokenDAO.create(tx, t).and(messageDAO.createTokenMessages(tx, t)).map(x -> m))
//                    .flatMap(x -> x);
//        } else {
//            log.error("Not supported command type: {} of the message {}", m.cmd, m);
//            return Uni.createFrom().item(m);
//        }
//    }
//
//    private Uni<List<ProcessToken>> createTokens(Transaction tx, String ref) {
//        return processInstanceDAO.findById(tx, ref)
//                .onItem().produceUni(pi -> {
//                    ProcessDefinitionRuntime pdr = deploymentService.getProcessDefinition(pi.processId, pi.processVersion);
//                    return Uni.createFrom().item(
//                            pdr.startNodes.values().stream()
//                            .map(node -> createToken(pi, node)).collect(Collectors.toList())
//                    );
//                });
//    }
//
//    private static ProcessToken createToken(ProcessInstance pi, Node node) {
//        ProcessToken token = new ProcessToken();
//        token.id = UUID.randomUUID().toString();
//        token.processId = pi.processId;
//        token.processVersion = pi.processVersion;
//        token.processInstance = pi.id;
//        token.nodeName = node.name;
//        token.status = ProcessToken.Status.CREATED;
//        token.type = ProcessToken.Type.valueOf(node);
//        token.data = pi.data;
//        return token;
//    }
}
