package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceMapperImpl;
import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessInstanceDAO {

    @Inject
    PgPool client;

    private Tuple tuple(ProcessInstance m) {
        return Tuple.of(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data);
    }
    private List<Tuple> tuple(List<ProcessInstance> processInstances) {
        return processInstances.stream().map(this::tuple).collect(Collectors.toList());
    }

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

    public Uni<String> update(Transaction tx, ProcessInstance pi) {
        return tx.preparedQuery(
                "UPDATE PROCESS_INSTANCE SET parent=$2,processId=$3,processVersion=$4,status=$5,data=$6 WHERE id=$1 RETURNING (id)")
                .execute(tuple(pi))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }

    public Uni<String> update(Transaction tx, List<ProcessInstance> pi) {
        return tx.preparedQuery(
                "UPDATE PROCESS_INSTANCE SET parent=$2,processId=$3,processVersion=$4,status=$5,data=$6 WHERE id=$1 RETURNING (id)")
                .executeBatch(tuple(pi))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }
}
