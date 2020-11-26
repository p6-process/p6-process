package org.lorislab.p6.process.model;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.model.SQL.*;

@ApplicationScoped
public class ProcessInstanceRepository {

    private static final String TABLE = "PROCESS_INSTANCE";

    private static final String SELECT_BY_CMD_ID = select(all())
            .from(TABLE)
            .where(equal(ProcessInstance_.CMD_ID))
            .build();

    private static final String SELECT_BY_ID = select(all())
            .from(TABLE)
            .where(equal(ProcessInstance_.ID))
            .build();

    private static final String CREATE_PI = insert(
            ProcessInstance_.ID, ProcessInstance_.PARENT, ProcessInstance_.PROCESS_ID,
            ProcessInstance_.PROCESS_VERSION, ProcessInstance_.STATUS,
            ProcessInstance_.DATA, ProcessInstance_.CMD_ID
            ).into(TABLE)
            .returning(ProcessInstance_.ID)
            .build();

    private static final String UPDATE_PI =  SQL.update(TABLE)
            .set(ProcessInstance_.PARENT, ProcessInstance_.STATUS, ProcessInstance_.DATA, ProcessInstance_.FINISHED)
            .where(equal(ProcessInstance_.ID))
            .returning(ProcessInstance_.ID)
            .build();

    @Inject
    PgPool pool;

    @Inject
    ProcessInstanceMapper processInstanceMapper;

    public Uni<String> create(Transaction tx, ProcessInstance m) {
        return tx.preparedQuery(CREATE_PI)
                .execute(tuple(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data, m.cmdId))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }

    public Uni<ProcessInstance> findById(String id) {
        return findById(pool, id);
    }

    public Uni<ProcessInstance> findById(SqlClient client, String id) {
        return client.preparedQuery(SELECT_BY_ID)
                .execute(tuple(id))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? processInstanceMapper.map(iterator.next()) : null);
    }

    public Uni<ProcessInstance> findByCmdId(String taskId) {
        return findByCmdId(pool, taskId);
    }

    public Uni<ProcessInstance> findByCmdId(SqlClient client, String cmdId) {
        return client.preparedQuery(SELECT_BY_CMD_ID)
                .execute(tuple(cmdId))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? processInstanceMapper.map(iterator.next()) : null);
    }

    public Uni<String> update(Transaction tx, ProcessInstance pi) {
        return tx.preparedQuery(UPDATE_PI)
                .execute(tuple(pi.parent, pi.status.name(), pi.data, pi.finished, pi.id))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }
//
//    public Uni<String> update(Transaction tx, List<ProcessInstance> pi) {
//        return tx.preparedQuery(
//                "UPDATE PROCESS_INSTANCE SET parent=$2,processId=$3,processVersion=$4,status=$5,data=$6 WHERE id=$1 RETURNING (id)")
//                .executeBatch(tuple(pi))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
//    }
}
