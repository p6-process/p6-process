package org.lorislab.p6.process.model;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessTokenRepository {

    private static final String TABLE = "PROCESS_TOKEN";

    private static final String SELECT_BY_ID = Sql.select(TABLE).from().where(ProcessToken_.ID);

    private static final String CREATE_TOKEN = Sql.insert(TABLE).columns(
                ProcessToken_.ID, ProcessToken_.PROCESS_INSTANCE, ProcessToken_.PROCESS_ID,
                ProcessToken_.PROCESS_VERSION, ProcessToken_.NODE_NAME, ProcessToken_.STATUS,
                ProcessToken_.TYPE, ProcessToken_.PARENT, ProcessToken_.REFERENCE,
                ProcessToken_.CREATED_FROM, ProcessToken_.DATA
            ).returning(ProcessToken_.ID);

    private static final String UPDATE_TOKEN =  Sql.update(TABLE).columns(
                ProcessToken_.PROCESS_INSTANCE, ProcessToken_.PROCESS_ID,
                ProcessToken_.PROCESS_VERSION, ProcessToken_.NODE_NAME, ProcessToken_.STATUS,
                ProcessToken_.TYPE, ProcessToken_.PARENT, ProcessToken_.REFERENCE,
                ProcessToken_.CREATED_FROM, ProcessToken_.DATA
            ).where(ProcessToken_.ID)
            .returning(ProcessToken_.ID);

    @Inject
    PgPool pool;

    @Inject
    ProcessTokenMapper processTokenMapper;


    private Tuple update(ProcessToken m) {
        return Sql.tuple(m.processInstance, m.processId, m.processVersion, m.nodeName, m.status.name(),
                m.type.name(), m.parent, m.reference,
                m.createdFrom.toArray(new String[]{}), m.data, m.id
        );
    }

    private Tuple create(ProcessToken m) {
        return Sql.tuple(
                m.id, m.processInstance, m.processId, m.processVersion, m.nodeName, m.status.name(),
                m.type.name(), m.parent, m.reference,
                m.createdFrom.toArray(new String[]{}), m.data
        );
    }

    private List<Tuple> create(List<ProcessToken> tokens) {
        return tokens.stream().map(this::create).collect(Collectors.toList());
    }

    public Uni<Integer> create(Transaction tx, List<ProcessToken> tokens) {
        return tx.preparedQuery(CREATE_TOKEN).executeBatch(create(tokens))
                .onItem().transform(SqlResult::size);
    }

    public Uni<ProcessToken> findById(SqlClient tx, String id) {
        return tx.preparedQuery(SELECT_BY_ID).execute(Tuple.of(id))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? processTokenMapper.map(iterator.next()) : null);
    }

    public Uni<ProcessToken> findById(String id) {
        return findById(pool, id);
    }

//    public Uni<ProcessToken> findByReferenceAndNodeName(Transaction tx, String ref, String nn) {
//        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE reference = $1 and nodename = $2")
//                .execute(Tuple.of(ref, nn))
//                .onItem().apply(RowSet::iterator)
//                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
//    }

    public Uni<String> update(Transaction tx, ProcessToken token) {
        return tx.preparedQuery(UPDATE_TOKEN).execute(update(token))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getString(0));
    }
//
//    public Uni<String> update(Transaction tx, List<ProcessToken> tokens) {
//        return tx.preparedQuery(
//                "UPDATE PROCESS_TOKEN SET processinstance=$2,processid=$3,processversion=$4,nodename=$5," +
//                        "status=$6,type=$7,executionId=$8,parent=$9,reference=$10,createdfrom=$11,data=$12 " +
//                        " WHERE id=$1 RETURNING (id)")
//                .executeBatch(tuple(tokens))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
//    }
}
