package org.lorislab.p6.process.message;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.lorislab.p6.process.model.SQL.*;

@ApplicationScoped
public class MessageRepository {

    private static final Select SELECT_COUNT = select(count(Message_.ID));

    private static final Insert INSERT = insert(Message_.DATA, Message_.HEADER).returning(Message_.ID);

    private static final Select SELECT_NEXT_MSG_IN = select(Message_.ID)
            .orderBy(asc(Message_.ID))
            .extend(update(), skipLocked())
            .limit(1L);

    @Inject
    PgPool pool;

    @Inject
    MessageMapper mapper;

    private static List<Tuple> tuple(List<Message> messages) {
        return messages.stream().map(MessageRepository::tuple).collect(Collectors.toList());
    }

    private static Tuple tuple(Message m) {
        return Tuple.of(m.data, m.header);
    }

    public Uni<Long> create(Message m)  {
        return create(pool, m);
    }

    public Uni<Long> create(SqlClient client, Message m) {
        return createMessage(client, m);
    }

    public Uni<Long> create(SqlClient client, String queue, List<Message> m) {
        return client
                .preparedQuery(INSERT.build(queue))
                .executeBatch(tuple(m))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public Uni<Message> nextProcessMessage(Transaction tx, String queue) {
        return nextProcessMessage(tx, queue, mapper);
    }

    public Multi<String> findAllMessages(String queue) {
        return pool.query(SELECT_COUNT.build(queue))
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

    public static Uni<Long> createMessage(SqlClient client, Message m) {
        return client.preparedQuery(INSERT.build(m.queue))
                .execute(tuple(m))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong(0));
    }

    public static Uni<Message> nextProcessMessage(SqlClient tx, String queue, MessageMapper mapper) {
        return tx.query(delete()
                    .from(queue)
                    .where(equal(Message_.ID, SELECT_NEXT_MSG_IN.build(queue)))
                    .returning(
                            Message_.ID,
                            Message_.DATE,
                            Message_.COUNT,
                            Message_.DATA,
                            Message_.HEADER,
                            alias(value(queue), Message_.QUEUE)
                    ).build())
                .execute()
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? mapper.map(it.next()) : null);
    }
}
