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

    public Uni<Long> createMessages(Transaction tx, List<ProcessToken> tokens) {
        List<Tuple> tuples = tokens.stream().map(x -> Tuple.of(x.id)).collect(Collectors.toList());
        return tx.preparedBatch("INSERT INTO TOKEN_MSG (ref) VALUES ($1) RETURNING (id)", tuples)
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Long> createMessage(Transaction tx, ProcessInstance pi) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (ref) VALUES ($1) RETURNING (id)", Tuple.of(pi.id))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Message> nextProcessMessage(Transaction tx) {
        return tx.query("DELETE FROM PROCESS_MSG WHERE id = (SELECT id FROM PROCESS_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref")
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
    }

    public Uni<Message> nextTokenMessage(Transaction tx) {
        return tx.query("DELETE FROM TOKEN_MSG WHERE id = (SELECT id FROM PROCESS_MSG ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, created, ref")
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? MessageMapperImpl.mapS(it.next()) : null);
    }

    public Multi<String> findAllTokenMessages() {
        return client.query("SELECT count(id) FROM TOKEN_MSG")
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
