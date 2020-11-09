package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessQueue;
import org.lorislab.p6.process.dao.model.ProcessQueueMapperImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.LongStream;

@ApplicationScoped
public class ProcessQueueDAO {

    public static String REQUEST_CHANNEL = "request_process_queue";

    @Inject
    PgPool pool;

    public Uni<Long> create(JsonObject data) {
        return create(pool, data);
    }

    public Uni<Long> create(SqlClient client, JsonObject data) {
        return client.preparedQuery("INSERT INTO REQUEST_PROCESS_QUEUE (data) VALUES ($1) RETURNING (id)")
                .execute(Tuple.of(data))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<ProcessQueue> nextProcessMessage(Transaction tx) {
        return tx.query("DELETE FROM REQUEST_PROCESS_QUEUE WHERE id = (SELECT id FROM REQUEST_PROCESS_QUEUE ORDER BY id  FOR UPDATE SKIP LOCKED LIMIT 1) RETURNING id, date, count, data")
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? ProcessQueueMapperImpl.mapS(it.next()) : null);
    }

    public Multi<String> findAllMessages() {
        return pool.query("SELECT count(id) FROM REQUEST_PROCESS_QUEUE")
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
