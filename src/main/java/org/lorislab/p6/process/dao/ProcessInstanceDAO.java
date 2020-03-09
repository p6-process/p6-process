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
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.lorislab.p6.process.dao.model.ProcessInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ProcessInstanceDAO {

    @Inject
    @Remote("instances")
    RemoteCache<String, ProcessInstance> cache;

    public List<ProcessInstance> findAll() {
        QueryFactory qf = Search.getQueryFactory(cache);
        return qf.create("from ProcessInstance b").list();
    }

    public ProcessInstance findByGuid(String guid) {
        return cache.get(guid);
    }

    public ProcessInstance update(ProcessInstance pi) {
        return cache.replace(pi.guid, pi);
    }

    public ProcessInstance create(ProcessInstance pi) {
        return cache.put(pi.guid, pi);
    }

}
