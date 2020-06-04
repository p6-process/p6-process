package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.MessageMapperImpl;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@ApplicationScoped
public class MessageDAO {

    @Inject
    PgPool client;

    public Uni<Long> createMessage(Transaction tx, ProcessToken token) {
        return tx.preparedQuery("INSERT INTO $1 (ref) VALUES ($2) RETURNING (id)")
                .execute(Tuple.of(token.type.route, token.id))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Long> createMessages(Transaction tx, List<ProcessToken> tokens, String table) {
        List<Tuple> tuples = tokens.stream().map(x -> Tuple.of(x.id))
                .collect(Collectors.toList());

        return tx.preparedQuery("INSERT INTO " + table + " (ref) VALUES ($1) RETURNING (id)")
                .executeBatch(tuples)
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Long> createTokenMessages(Transaction tx, List<ProcessToken> tokens) {
        List<Tuple> tuples = tokens.stream().map(x -> Tuple.of(x.id)).collect(Collectors.toList());
        return tx.preparedQuery("INSERT INTO TOKEN_MSG (ref) VALUES ($1) RETURNING (id)")
                .executeBatch(tuples)
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Long> createProcessMessage(Transaction tx, String ref, String cmd) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (ref, cmd) VALUES ($1,$2) RETURNING (id)")
                .execute(Tuple.of(ref, cmd))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Message> nextProcessMessage(Transaction tx) {
        return tx.query("DELETE FROM PROCESS_MSG WHERE id = (SELECT id FROM PROCESS_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
    }

    public Uni<Message> nextTokenMessage(Transaction tx) {
        return tx.query("DELETE FROM TOKEN_MSG WHERE id = (SELECT id FROM TOKEN_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
    }

    public Uni<Message> nextSingletonMessage(Transaction tx) {
        return tx.query("DELETE FROM SINGLETON_MSG WHERE id = (SELECT id FROM SINGLETON_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref, cmd")
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
    }

    public Multi<String> findAllTokenMessages() {
        return client.query("SELECT count(id) FROM TOKEN_MSG")
                .execute()
                .map(RowSet::iterator)
                .map(r -> r.hasNext() ? r.next().getLong(0) : null)
                .onItem().produceMulti(i -> {
                    if (i == null || i == 0) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
                });
    }

    public Multi<String> findAllSingletonMessages() {
        return client.query("SELECT count(id) FROM SINGLETON_MSG")
                .execute()
                .map(RowSet::iterator)
                .map(r -> r.hasNext() ? r.next().getLong(0) : null)
                .onItem().produceMulti(i -> {
                    if (i == null || i == 0) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
                });
    }

    public Multi<String> findAllProcessMessages() {
        return client.query("SELECT count(id) FROM PROCESS_MSG")
                .execute()
                .map(RowSet::iterator)
                .map(r -> r.hasNext() ? r.next().getLong(0) : null)
                .onItem().produceMulti(i -> {
                    if (i == null || i == 0) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
                });
    }
}
