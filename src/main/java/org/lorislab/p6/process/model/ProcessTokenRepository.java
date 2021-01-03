package org.lorislab.p6.process.model;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.lorislab.p6.process.model.SQL.*;

@ApplicationScoped
public class ProcessTokenRepository {

    private static final String TABLE = "PROCESS_TOKEN";

    private static final String SELECT_BY_ID = select(all())
            .from(TABLE)
            .where(equal(ProcessToken_.ID))
            .build();

    private static final String CREATE_TOKEN = SQL.insert(
                ProcessToken_.ID, ProcessToken_.PROCESS_INSTANCE, ProcessToken_.PROCESS_ID,
                ProcessToken_.PROCESS_VERSION, ProcessToken_.NODE_NAME, ProcessToken_.STATUS,
                ProcessToken_.TYPE, ProcessToken_.PARENT, ProcessToken_.CREATED_FROM,
                ProcessToken_.DATA, ProcessToken_.PREVIOUS_FROM, ProcessToken_.PREVIOUS_NODE_NAME
            ).into(TABLE).returning(ProcessToken_.ID)
            .build();

    private static final String UPDATE_TOKEN =  SQL.update(TABLE).set(
                ProcessToken_.PROCESS_INSTANCE, ProcessToken_.PROCESS_ID,
                ProcessToken_.PROCESS_VERSION, ProcessToken_.NODE_NAME, ProcessToken_.STATUS,
                ProcessToken_.TYPE, ProcessToken_.PARENT,
                ProcessToken_.CREATED_FROM, ProcessToken_.DATA, ProcessToken_.FINISHED,
                ProcessToken_.PREVIOUS_FROM, ProcessToken_.PREVIOUS_NODE_NAME
            ).where(equal(ProcessToken_.ID))
            .returning(ProcessToken_.ID)
            .build();

    private static final String SELECT_BY_PI_NODE_NAME = select(all())
            .from(TABLE)
            .where(equal(ProcessToken_.PROCESS_INSTANCE), equal(ProcessToken_.NODE_NAME), equal(ProcessToken_.STATUS))
            .build();

    private static final String SELECT_COUNT_ACTIVE = select(count())
            .from(TABLE)
            .where(equal(ProcessToken_.PROCESS_INSTANCE), not(ProcessToken_.ID), not(ProcessToken_.STATUS), not(ProcessToken_.STATUS))
            .build();


    @Inject
    PgPool pool;

    @Inject
    ProcessTokenMapper processTokenMapper;


    private Tuple update(ProcessToken m) {
        return tuple(m.processInstance, m.processId, m.processVersion, m.nodeName, m.status.name(),
                m.type.name(), m.parent, m.createdFrom.toArray(new String[]{}), m.data, m.finished,
                m.previousFrom.toArray(new String[]{}), m.previousNodeName, m.id
        );
    }

    private Tuple create(ProcessToken m) {
        return tuple(
                m.id, m.processInstance, m.processId, m.processVersion, m.nodeName, m.status.name(),
                m.type.name(), m.parent, m.createdFrom.toArray(new String[]{}), m.data,
                m.previousFrom.toArray(new String[]{}), m.previousNodeName
        );
    }

    private List<Tuple> create(List<ProcessToken> tokens) {
        return tokens.stream().map(this::create).collect(Collectors.toList());
    }

    public Uni<Integer> create(Transaction tx, List<ProcessToken> tokens) {
        return tx.preparedQuery(CREATE_TOKEN)
                .executeBatch(create(tokens))
                .onItem().transform(SqlResult::size);
    }

    public Uni<ProcessToken> findById(SqlClient tx, String id) {
        return tx.preparedQuery(SELECT_BY_ID)
                .execute(tuple(id))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? processTokenMapper.map(iterator.next()) : null);
    }

    public Uni<ProcessToken> findById(String id) {
        return findById(pool, id);
    }

    public Uni<ProcessToken> findByPIAndNodeName(Transaction tx, String processInstance, String nodeName) {
        return tx.preparedQuery(SELECT_BY_PI_NODE_NAME)
                .execute(tuple(processInstance, nodeName, ProcessToken.Status.CREATED.name()))
                .map(RowSet::iterator)
                .map(i -> i.hasNext() ? processTokenMapper.map(i.next()) : null);
    }

    public Uni<String> update(Transaction tx, ProcessToken token) {
        return tx.preparedQuery(UPDATE_TOKEN)
                .execute(update(token))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }

    public Uni<Long> countActive(Transaction tx, String processInstance, String ignoreTokenId) {
        return tx.preparedQuery(SELECT_COUNT_ACTIVE)
                .execute(tuple(processInstance, ignoreTokenId, ProcessToken.Status.FAILED.name(), ProcessToken.Status.FINISHED.name()))
                .map(RowSet::iterator)
                .map(i -> i.hasNext() ? i.next().getLong(0) : 0);
    }
}
