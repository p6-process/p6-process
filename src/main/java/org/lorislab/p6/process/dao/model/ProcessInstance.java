/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.dao.model;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.vertx.sql.mapper.SqlColumn;

public class ProcessInstance {

    @SqlColumn(ignore=true)
    public boolean created;

    public String id;

    public String parent;

    public String processId;

    public String processVersion;

    public ProcessInstanceStatus status;

    public JsonObject data = new JsonObject();

    public Uni<String> create(Transaction tx) {
        return tx.preparedQuery("INSERT INTO PROCESS_INSTANCE (id, parent,processId,processVersion,status,data) VALUES ($1,$2,$3,$4,$5, $6) RETURNING (id)"
                , Tuple.of(id, parent, processId, processVersion, status.name(), data))
                .onItem().apply(pgRowSet -> pgRowSet.iterator().next().getString("id"));
    }

    public Uni<Long> create(PgPool client) {
        return client.preparedQuery("INSERT INTO PROCESS_INSTANCE (parent,processId,processVersion,status,data) VALUES ($1,$2,$3,$4,$5) RETURNING (id)"
                , Tuple.of(parent, processId, processVersion, status.name(), data))
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + id;
    }
}
