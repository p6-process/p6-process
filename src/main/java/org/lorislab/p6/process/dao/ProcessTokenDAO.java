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
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessTokenDAO {

    @Inject
    @Remote("tokens")
    RemoteCache<String, ProcessTokenModel> cache;

    public List<ProcessToken> findAll() {
        QueryFactory qf = Search.getQueryFactory(cache);
        return qf.create("from ProcessToken b").list();
    }

    public ProcessToken findByGuid(String guid) {
        return map(cache.get(guid));
    }

    public void update(ProcessToken token) {
        cache.replace(token.guid, map(token));
    }

    public void create(ProcessToken token) {
        cache.put(token.guid, map(token));
    }

    public void createAll(Map<String, ProcessToken> tokens) {
        cache.putAll(map(tokens));
    }

    public ProcessToken findByReferenceAndNodeName(String reference, String nodeName) {
        QueryFactory qf = Search.getQueryFactory(cache);
        List<ProcessTokenModel> tokens = qf.create("from p6_process.ProcessTokenModel t where t.reference = '" + reference + "' and t.nodeName = '" + nodeName + "'").list();
        if (tokens != null && !tokens.isEmpty()) {
            return map(tokens.get(0));
        }
        return null;
    }

    private Map<String, ProcessTokenModel> map(Map<String, ProcessToken> tokens) {
        return tokens.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        t -> map(t.getValue())
                ));
    }

    private ProcessTokenModel map(ProcessToken t) {
        ProcessTokenModel m = new ProcessTokenModel();
        m.guid = t.guid;
        m.nodeName = t.nodeName;
        m.reference = t.reference;
        m.data = JsonObject.mapFrom(t).toString();
        return m;
    }

    private ProcessToken map(ProcessTokenModel m) {
        if (m == null) {
            return null;
        }
        return new JsonObject(m.data).mapTo(ProcessToken.class);
    }
}
