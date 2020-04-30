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
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;

import java.util.HashSet;
import java.util.Set;

public class ProcessToken {

    public String id;

    public Integer version;

    public String messageId;

    public String processInstance;

    public String processId;

    public String processVersion;

    public String nodeName;

    public ProcessTokenStatus status;

    public ProcessTokenType type;

    public String executionId;

    public String parent;

    public String reference;

    public Set<String> createdFrom = new HashSet<>();

    public JsonObject data = new JsonObject();

    public ProcessToken copy() {
        return this;
    }

    public static Uni<ProcessToken> findById(PgPool client, String id) {
        return client.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE id = $1", Tuple.of(id))
                .map(RowSet::iterator)
                .map(iterator -> iterator.hasNext() ? ProcessTokenMapperImpl.mapS(iterator.next()) : null);
    }

    public Uni<ProcessToken> findById(Transaction tx, String id) {
        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE id = $1", Tuple.of(id))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
    }

    public Uni<ProcessToken> findByReferenceAndNodeName(Transaction tx, String ref, String nn) {
        return tx.preparedQuery("SELECT * FROM PROCESS_TOKEN WHERE reference = $1 and nodeName = $2", Tuple.of(ref, nn))
                .onItem().apply(RowSet::iterator)
                .onItem().apply(i -> i.hasNext() ? ProcessTokenMapperImpl.mapS(i.next()) : null);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + id;
    }
}
