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

package org.lorislab.p6.process.dao;

import io.quarkus.infinispan.client.Remote;
import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProcessInstanceDAO {

    @Inject
    @Remote("instances")
    RemoteCache<String, ProcessInstanceModel> cache;

    public ProcessInstance findByGuid(String guid) {
        return map(cache.get(guid));
    }

    public void update(ProcessInstance pi) {
        cache.replace(pi.guid, map(pi));
    }

    public void create(ProcessInstance pi) {
        cache.put(pi.guid, map(pi));
    }

    private ProcessInstance map(ProcessInstanceModel m) {
        if (m == null) {
            return null;
        }
        return new JsonObject(m.data).mapTo(ProcessInstance.class);
    }

    private ProcessInstanceModel map(ProcessInstance p) {
        ProcessInstanceModel m = new ProcessInstanceModel();
        m.guid = p.guid;
        m.processId = p.processId;
        m.processVersion = p.processVersion;
        m.data = JsonObject.mapFrom(p).toString();
        return m;
    }
}
