package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.dao.model.ProcessInstance;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessInstanceDAO {

//    private Tuple tuple(ProcessInstance m) {
//        return Tuple.of(m.id, m.parent, m.processId, m.processVersion, m.status.name(), m.data);
//    }
//    private List<Tuple> tuple(List<ProcessInstance> processInstances) {
//        return processInstances.stream().map(this::tuple).collect(Collectors.toList());
//    }
//
//    public void create(Txn txn, ProcessInstance pi) {
//
//        String tmp = "/p6/pi/data/" + pi.id;
//        ByteSequence key = ByteSequence.from(tmp.getBytes());
//        ByteSequence value = ByteSequence.from(Json.encode(pi).getBytes());
//
//        ByteSequence json = ByteSequence.from((tmp + "/json").getBytes());
//        txn
//                .If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0)))
//                .Then(
//                        Op.put(json, value, PutOption.DEFAULT),
//                        Op.put(ByteSequence.from((tmp + "/processVersion").getBytes()),
//                                ByteSequence.from(pi.processVersion.getBytes()), PutOption.DEFAULT)
//                );
//    }

    public Uni<ProcessInstance> findById(String id) {
        return Uni.createFrom().nullItem();
    }

//    public Uni<ProcessInstance> findById(Transaction tx, String id) {
//        return tx.preparedQuery("SELECT id,parent,processId,processVersion,status,data FROM PROCESS_INSTANCE WHERE id = $1")
//                .execute(Tuple.of(id))
//                .onItem().apply(RowSet::iterator)
//                .onItem().apply(i -> i.hasNext() ? ProcessInstanceMapperImpl.mapS(i.next()) : null);
//    }
//
//    public Uni<String> update(Transaction tx, ProcessInstance pi) {
//        return tx.preparedQuery(
//                "UPDATE PROCESS_INSTANCE SET parent=$2,processId=$3,processVersion=$4,status=$5,data=$6 WHERE id=$1 RETURNING (id)")
//                .execute(tuple(pi))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
//    }
//
//    public Uni<String> update(Transaction tx, List<ProcessInstance> pi) {
//        return tx.preparedQuery(
//                "UPDATE PROCESS_INSTANCE SET parent=$2,processId=$3,processVersion=$4,status=$5,data=$6 WHERE id=$1 RETURNING (id)")
//                .executeBatch(tuple(pi))
//                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString(0));
//    }
}
