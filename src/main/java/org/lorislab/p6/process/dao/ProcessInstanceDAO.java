package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceMapperImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProcessInstanceDAO {

    @Inject
    PgPool client;

    public Uni<String> create(Transaction tx, ProcessInstance m) {
        return tx.preparedQuery("INSERT INTO PROCESS_INSTANCE (id,parent,processId,processVersion,status,data) VALUES ($1,$2,$3,$4,$5, $6) RETURNING (id)")
                .execute(Tuple.of(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString("id"));
    }

    public Uni<ProcessInstance> findById(String id) {
        return client.preparedQuery("SELECT id,parent,processId,processVersion,status,data FROM PROCESS_INSTANCE WHERE id = $1")
                .execute(Tuple.of(id))
                .map(RowSet::iterator)
                .map(it -> it.hasNext() ? ProcessInstanceMapperImpl.mapS(it.next()) : null);
    }

    public Uni<ProcessInstance> findById(Transaction tx, String id) {
        return tx.preparedQuery("SELECT id,parent,processId,processVersion,status,data FROM PROCESS_INSTANCE WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessInstanceMapperImpl.mapS(i.next()) : null);
    }
}
