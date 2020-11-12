package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessTokenDAO {

    private Tuple tuple(ProcessToken m) {
        return Tuple.tuple(Arrays.asList(
                m.id, m.processInstance, m.processId, m.processVersion, m.nodeName, m.status.name(),
                m.type.name(), m.parent, m.reference,
                m.createdFrom.toArray(new String[]{}), m.data
        ));
    }

    private List<Tuple> tuple(List<ProcessToken> tokens) {
        return tokens.stream().map(this::tuple).collect(Collectors.toList());
    }

    public Uni<Integer> create(Transaction tx, List<ProcessToken> tokens) {
        return tx.preparedQuery(
                "INSERT INTO PROCESS_TOKEN (id,processinstance,processid,processversion,nodename,status,type,parent,reference,createdfrom,data) " +
                    "VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11) RETURNING (id)")
                .executeBatch(tuple(tokens))
                .onItem().transform(SqlResult::size);
    }
//
//    public Uni<ProcessToken> findById(Transaction tx, String id) {
//        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE id = $1")
//                .execute(Tuple.of(id))
//                .map(RowSet::iterator)
//                .map(iterator -> iterator.hasNext() ? ProcessTokenMapperImpl.mapS(iterator.next()) : null);
//    }

    public Uni<ProcessToken> findById(String id) {
        return Uni.createFrom().nullItem();
    }

//    public Uni<ProcessToken> findByReferenceAndNodeName(Transaction tx, String ref, String nn) {
//        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE reference = $1 and nodename = $2")
//                .execute(Tuple.of(ref, nn))
//                .onItem().apply(RowSet::iterator)
//                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
//    }

    public Uni<List<ProcessToken>> findByProcessInstance(String pi) {
        return Uni.createFrom().nullItem();
    }

//    public Uni<String> update(Transaction tx, ProcessToken tokens) {
//        return tx.preparedQuery(
//                "UPDATE PROCESS_TOKEN SET processinstance=$2,processid=$3,processversion=$4,nodename=$5," +
//                        "status=$6,type=$7,executionId=$8,parent=$9,reference=$10,createdfrom=$11,data=$12 " +
//                        " WHERE id=$1 RETURNING (id)")
//                .execute(tuple(tokens))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
//    }
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
