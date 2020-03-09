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
import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProcessTokenDAO {

    @Inject
    @Remote("tokens")
    RemoteCache<String, ProcessToken> cache;

    public List<ProcessToken> findAll() {
        QueryFactory qf = Search.getQueryFactory(cache);
        return qf.create("from ProcessToken b").list();
    }

    public ProcessToken findByGuid(String guid) {
        return cache.get(guid);
    }

    public ProcessToken update(ProcessToken token) {
        return cache.replace(token.guid, token);
    }

    public ProcessToken create(ProcessToken token) {
        return cache.put(token.guid, token);
    }

    public void createAll(Map<String, ProcessToken> tokens) {
        cache.putAll(tokens);
    }

    public ProcessToken findByReferenceAndCreateNodeName(String reference, String createdNodeName) {
        // TODO:

        QueryFactory qf = Search.getQueryFactory(cache);
        List<ProcessToken> tokens = qf.create("from ProcessToken t where t.reference = '" + reference + "' and t.createNodeName = '" + createdNodeName + "'").list();
        return tokens.get(0);
    }
}
