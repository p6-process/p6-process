package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.Message;

import java.time.Instant;

public class MessageDAO {

    public static Uni<Long> create(Transaction tx, String ref) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(Instant.now().getEpochSecond(), ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Long> create(Transaction tx, Message m) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(m.created, m.ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Long> create(PgPool client, Message m) {
        return client.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(m.created, m.ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }
}
