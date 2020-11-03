package org.lorislab.p6.process.reactive;

import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.MessageType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ProcessExecutor {

//    @Inject
//    DeploymentService deploymentService;
//
//    @Inject
//    ProcessInstanceDAO processInstanceDAO;
//
    @Inject
    MessageDAO messageDAO;
//
//    @Inject
//    ProcessTokenDAO processTokenDAO;
//
//    @Inject
//    Vertx vertx;

    @Inject
    Watch watch;

    @Inject
    KV client;

    @Inject
    Lock lock;

    @Inject
    Lease lease;

    public void start() {
        Multi.createBy().merging().streams(running())
                .onItem().produceUni(this::checkRunningItem)
                .concatenate()
                .subscribe().with(m -> log.info("Check running item {} ", m), Throwable::printStackTrace);

        Multi.createBy().merging().streams(messageDAO.findAllMessages(MessageType.PROCESS_MSG), subscriber())
                .onItem().produceUni(this::execute)
                .concatenate()
                .subscribe().with(m -> log.info("Executed message {} ", m), Throwable::printStackTrace);
    }

    public Uni<String> execute(KeyValue value) {

        ByteSequence key = value.getKey();
        String tmp = key.toString(StandardCharsets.UTF_8);
        ByteSequence newKey = ByteSequence.from(tmp.replaceFirst("pending", "running").getBytes());
        System.out.println("----> EVENT " + tmp + " - " + value.getValue().toString(StandardCharsets.UTF_8));

        return Uni.createFrom().completionStage(lease.grant(20L))
                .flatMap(r -> Uni.createFrom().completionStage(
                        client.txn()
                            .If(
                                new Cmp(key, Cmp.Op.GREATER, CmpTarget.version(0)),
                                new Cmp(newKey, Cmp.Op.EQUAL, CmpTarget.version(0))
                            ).Then(
                                Op.put(newKey, value.getValue(), PutOption.newBuilder().withLeaseId(r.getID()).build()),
                                Op.delete(key, DeleteOption.DEFAULT)
                            ).commit()
                    ).map(x -> tmp) //TODO if ok execute process
                );
    }

    private Multi<KeyValue> subscriber() {
        ByteSequence key = ByteSequence.from("/p6/pi/pending".getBytes());
        return Multi.createFrom().emitter(emitter ->
            watch.watch(key,
                    WatchOption.newBuilder().withNoDelete(true).withPrefix(key).build(),
                    x -> x.getEvents().forEach(e -> emitter.emit(e.getKeyValue())))
        );
    }

    private Multi<KeyValue> running() {
        ByteSequence key = ByteSequence.from("/p6/pi/running".getBytes());
        return Multi.createFrom().emitter(emitter ->
            watch.watch(key,
                    WatchOption.newBuilder().withNoDelete(false).withPrefix(key).withPrevKV(true).withNoPut(true).build(),
                    x -> x.getEvents().forEach(e -> emitter.emit(e.getPrevKV())))
        );
    }

    public Uni<String> checkRunningItem(KeyValue item) {
        ByteSequence key = item.getKey();
        String tmp = key.toString(StandardCharsets.UTF_8);
        return Uni.createFrom().completionStage(lease.timeToLive(item.getLease(), LeaseOption.DEFAULT))
                .map(r -> r.getTTl() == -1)
                .onItem().produceUni(expired -> {
                    System.out.println("----> EVENT RUNNING " + tmp + " - " + expired + " - " + item.getValue().toString(StandardCharsets.UTF_8));
                   if (!expired) {
                       return Uni.createFrom().item(tmp);
                   } else {
                       Message msg = Json.decodeValue(item.getValue().toString(StandardCharsets.UTF_8), Message.class);
                       msg.count = msg.count + 1;
                       ByteSequence value = ByteSequence.from(Json.encode(msg).getBytes());
                       ByteSequence pk = ByteSequence.from(tmp.replaceFirst("running", "pending").getBytes());
                       ByteSequence qk = ByteSequence.from(tmp.replaceFirst("running", "queue").getBytes());
                       return Uni.createFrom().completionStage(
                         client.txn()
                                 .If(new Cmp(pk, Cmp.Op.EQUAL, CmpTarget.version(0)))
                                 .Then(
                                         Op.put(pk, value, PutOption.DEFAULT),
                                         Op.put(qk, value, PutOption.DEFAULT)
                                 ).commit()
                       ).map(r -> tmp);
                   }
                });
    }

//    public Uni<Message> execute() {
//        return client.begin().flatMap(tx -> messageDAO.nextProcessMessage(tx)
//                .onItem().apply(m -> {
//                    if (m == null) {
//                        tx.close();
//                        return Uni.createFrom().item((Message) null);
//                    }
//                    return executeMessage(tx, m)
//                            .onItem().apply(u -> tx.commit().onItem().apply(x -> u))
//                            .flatMap(x -> x);
//                }).flatMap(x -> x)
//        );
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
