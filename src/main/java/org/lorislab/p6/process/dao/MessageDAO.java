package org.lorislab.p6.process.dao;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.MessageType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class MessageDAO {

//    public Uni<Long> createMessage(Transaction tx, ProcessToken token) {
//        return tx.preparedQuery("INSERT INTO $1 (ref) VALUES ($2) RETURNING (id)")
//                .execute(Tuple.of(token.type.message.table, token.id))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
//    }
//
//    public Uni<Long> createMessages(Transaction tx, List<ProcessToken> tokens, MessageType type) {
//        List<Tuple> tuples = tokens.stream().map(x -> Tuple.of(x.id))
//                .collect(Collectors.toList());
//
//        return tx.preparedQuery("INSERT INTO " + type.table + " (ref) VALUES ($1) RETURNING (id)")
//                .executeBatch(tuples)
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
//    }
//
//    public Uni<Long> createTokenMessages(Transaction tx, List<ProcessToken> tokens) {
//        List<Tuple> tuples = tokens.stream().map(x -> Tuple.of(x.id)).collect(Collectors.toList());
//        return tx.preparedQuery("INSERT INTO TOKEN_MSG (ref) VALUES ($1) RETURNING (id)")
//                .executeBatch(tuples)
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
//    }
//
    public void createProcessMessage(Txn txn, String ref, String cmd) {
        Message msg = Message.create("/p6/pi/data/" + ref);
        msg.cmd = cmd;

        ByteSequence value = ByteSequence.from(Json.encode(msg).getBytes());
        txn.Then(Op.put(ByteSequence.from(("/p6/pi/pending/" + ref).getBytes()), value, PutOption.DEFAULT));
        txn.Then(Op.put(ByteSequence.from(("/p6/pi/queue/" + ref).getBytes()), value, PutOption.DEFAULT));
    }
//
//    public Uni<Message> nextProcessMessage(Transaction tx) {
//        return tx.query("DELETE FROM PROCESS_MSG WHERE id = (SELECT id FROM PROCESS_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
//                .execute()
//                .map(RowSet::iterator)
//                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
//    }
//
//    public Uni<Message> nextTokenMessage(Transaction tx) {
//        return tx.query("DELETE FROM TOKEN_MSG WHERE id = (SELECT id FROM TOKEN_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
//                .execute()
//                .map(RowSet::iterator)
//                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
//    }
//
//    public Uni<Message> nextSingletonMessage(Transaction tx) {
//        return tx.query("DELETE FROM SINGLETON_MSG WHERE id = (SELECT id FROM SINGLETON_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
//                .execute()
//                .map(RowSet::iterator)
//                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
//    }
//
//    public Multi<String> findAllMessages(MessageType type) {
//        return client.query("SELECT count(id) FROM " + type.table)
//                .execute()
//                .map(RowSet::iterator)
//                .map(r -> r.hasNext() ? r.next().getLong(0) : null)
//                .onItem().produceMulti(i -> {
//                    if (i == null || i == 0) {
//                        return Multi.createFrom().nothing();
//                    }
//                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
//                });
//}

    @Inject
    KV client;

    public Multi<KeyValue> findAllMessages(MessageType type) {

        String tmp = "/p6/pi/pending";
        GetOption option = GetOption.newBuilder()
                .withRange(ByteSequence.from((tmp + "0").getBytes()))
                .withSortField(GetOption.SortTarget.CREATE)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .build();

        return Uni.createFrom().completionStage(client.get(ByteSequence.from(tmp.getBytes()), option))
                .map(GetResponse::getKvs)
                .onItem().produceMulti(r -> {
                    if (r == null || r.isEmpty()) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(r);
                });
    }
}
