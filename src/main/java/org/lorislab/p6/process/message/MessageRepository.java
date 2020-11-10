package org.lorislab.p6.process.message;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.LongStream;

@ApplicationScoped
public class MessageRepository {

    @Inject
    PgPool pool;

    @Inject
    MessageMapper mapper;

    public Uni<Long> create(String queue, JsonObject header, JsonObject data)  {
        return create(pool, queue, header, data);
    }

    public Uni<Long> create(SqlClient client, String queue, JsonObject header, JsonObject data) {
        return client.preparedQuery("INSERT INTO " + queue + " (data, header) VALUES ($1, $2) RETURNING (id)")
                .execute(Tuple.of(data, header))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Message> nextProcessMessage(Transaction tx, String queue) {
        return tx.query("DELETE FROM " + queue + " WHERE id = (SELECT id FROM " + queue + " ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, date, count, data, label, header, '" + queue
         + "' as queue")
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? mapper.map(it.next()) : null);
    }

    public Multi<String> findAllMessages(String queue) {
        return pool.query("SELECT count(id) FROM " + queue)
                .execute()
                .map(RowSet::iterator)
                .map(r -> r.hasNext() ? r.next().getLong(0) : null)
                .onItem().transformToMulti(i -> {
                    if (i == null || i == 0) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
                });
    }

}