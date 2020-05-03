package org.lorislab.p6.process.dao.model;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;

import java.time.Instant;

public class Message {

    public Long id;

    public Long created;

    public String ref;

    public static Uni<Long> create(Transaction tx, String ref) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(Instant.now().getEpochSecond(), ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Long> create(Transaction tx) {
        return tx.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(created, ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Long> create(PgPool client) {
        return client.preparedQuery("INSERT INTO PROCESS_MSG (created,ref) VALUES ($1,$2) RETURNING (id)", Tuple.of(created, ref))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Message create(String ref) {
        Message message = new Message();
        message.created = Instant.now().getEpochSecond();
        return message;
    }
}
