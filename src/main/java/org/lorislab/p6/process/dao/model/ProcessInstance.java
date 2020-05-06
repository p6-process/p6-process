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
import org.lorislab.vertx.sql.mapper.SqlColumn;

@RegisterForReflection
public class ProcessInstance {

    @SqlColumn(ignore = true)
    public boolean created;

    public String id;

    public String parent;

    public String processId;

    public String processVersion;

    public Status status;

    public JsonObject data = new JsonObject();

    @Override
    public String toString() {
        return "ProcessInstance:" + id;
    }

    public enum Status {

        CREATED,

        FAILED,

        FINISHED;

    }
}
