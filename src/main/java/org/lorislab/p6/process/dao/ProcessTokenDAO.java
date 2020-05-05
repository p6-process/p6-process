package org.lorislab.p6.process.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenMapperImpl;

public class ProcessTokenDAO {

    public static Uni<ProcessToken> findById(PgPool client, String id) {
        return client.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE id = $1", Tuple.of(id))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? ProcessTokenMapperImpl.mapS(iterator.next()) : null);
    }

    public static Uni<ProcessToken> findById(Transaction tx, String id) {
        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE id = $1", Tuple.of(id))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
    }

    public static Uni<ProcessToken> findByReferenceAndNodeName(Transaction tx, String ref, String nn) {
        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE reference = $1 and nodeName = $2", Tuple.of(ref, nn))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
    }
}
