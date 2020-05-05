package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceMapperImpl;

public class ProcessInstanceDAO {

    public static Uni<String> create(Transaction tx, ProcessInstance m) {
        return tx.preparedQuery("INSERT INTO PROCESS_INSTANCE (id, parent,processId,processVersion,status,data) VALUES ($1,$2,$3,$4,$5, $6) RETURNING (id)"
                , Tuple.of(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString("id"));
    }

    public static Uni<Long> create(PgPool client,  ProcessInstance m) {
        return client.preparedQuery("INSERT INTO PROCESS_INSTANCE (id,parent,processId,processVersion,status,data) VALUES ($1,$2,$3,$4,$5, $6) RETURNING (id)"
                , Tuple.of(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<ProcessInstance> findById(PgPool client, String id) {
        return client.preparedQuery("SELECT * FROM PROCESS_INSTANCE WHERE id = $1", Tuple.of(id))
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? ProcessInstanceMapperImpl.mapS(it.next()) : null);
    }

    public static Uni<ProcessInstance> findById(Transaction tx, String id) {
        return tx.preparedQuery("SELECT * FROM PROCESS_INSTANCE WHERE id = $1", Tuple.of(id))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessInstanceMapperImpl.mapS(i.next()) : null);
    }
}
