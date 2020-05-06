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

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import org.lorislab.p6.process.model.Gateway;
import org.lorislab.p6.process.model.Node;
import org.lorislab.vertx.sql.mapper.SqlColumn;

import java.util.HashSet;
import java.util.Set;

@RegisterForReflection
public class ProcessToken {

    @SqlColumn(ignore = true)
    public boolean created;

    public String id;

    public String processInstance;

    public String processId;

    public String processVersion;

    public String nodeName;

    public Status status;

    public Type type;

    public String executionId;

    public String parent;

    public String reference;

    public Set<String> createdFrom = new HashSet<>();

    public JsonObject data = new JsonObject();

    public ProcessToken copy() {
        return this;
    }

    @Override
    public String toString() {
        return "ProcessToken:" + id;
    }

    public enum Status {

        CREATED,

        IN_EXECUTION,

        FAILED,

        FINISHED;

    }

    private static final String ROUTE_DEFAULT = "token-execute";

    private static final String ROUTE_SINGLETON = "token-singleton";

    private static final String ROUTE_SERVICE_TASK = "service-task";

    public enum Type {

        START_EVENT(ROUTE_DEFAULT, 1),

        END_EVENT(ROUTE_SINGLETON, 0),

        SERVICE_TASK(ROUTE_DEFAULT, 1),

        SERVICE_TASK_COMPLETE(ROUTE_SERVICE_TASK, 1),

        PARALLEL_GATEWAY_DIVERGING(ROUTE_DEFAULT, -1),

        PARALLEL_GATEWAY_CONVERGING(ROUTE_SINGLETON, 1),

        EXCLUSIVE_GATEWAY_DIVERGING(ROUTE_DEFAULT, -1),

        EXCLUSIVE_GATEWAY_CONVERGING(ROUTE_DEFAULT, 1),

        INCLUSIVE_GATEWAY_DIVERGING(ROUTE_DEFAULT, -1),

        INCLUSIVE_GATEWAY_CONVERGING(ROUTE_SINGLETON, 1),
        ;

        public final String route;

        public final int nextNodeCount;

        Type(String route, int nextNodeCount) {
            this.nextNodeCount = nextNodeCount;
            this.route = route;
        }

        public static Type valueOf(Node node) {
            String tmp = node.type.name();
            if (node instanceof Gateway) {
                Gateway pg = (Gateway) node;
                tmp = pg.type + "_" + pg.sequence;
            }
            return Type.valueOf(tmp);
        }

    }

}
