package org.lorislab.p6.process.mem.service;

import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.lorislab.p6.process.stream.model.ProcessInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class PersistenceInstanceService {

    @Inject
    @Remote("processInstance")
    RemoteCache<String, ProcessInstance> cache;

    public List<ProcessInstance> findAll() {
        QueryFactory qf = Search.getQueryFactory(cache);
        Query q = qf.from(ProcessInstance.class).build();
        return q.list();
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
