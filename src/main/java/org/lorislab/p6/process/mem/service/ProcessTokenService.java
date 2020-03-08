package org.lorislab.p6.process.mem.service;

import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ProcessTokenService {

    @Inject
    @Remote("processToken")
    RemoteCache<String, ProcessTokenStream> cache;

    public List<ProcessTokenStream> findAll() {
        QueryFactory qf = Search.getQueryFactory(cache);
        Query q = qf.from(ProcessTokenStream.class).build();
        return q.list();
    }

    public ProcessTokenStream findByGuid(String guid) {
        return cache.get(guid);
    }

    public ProcessTokenStream update(ProcessTokenStream pi) {
        return cache.replace(pi.guid, pi);
    }

    public ProcessTokenStream create(ProcessTokenStream pi) {
        return cache.put(pi.guid, pi);
    }
}
