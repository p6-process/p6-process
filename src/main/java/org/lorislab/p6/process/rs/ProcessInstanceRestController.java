package org.lorislab.p6.process.rs;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.pgclient.PgPool;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.stream.ProcessStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

import static org.lorislab.p6.process.rs.P6Application.*;

@ApplicationScoped
@RouteBase(path = "instance", produces = APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    PgPool client;

    @Inject
    ProcessStream processStream;

    @Route(path = ":id", methods = HttpMethod.GET)
    public void get(RoutingContext rc) {
        String id = rc.pathParam("id");
        ProcessInstance.findById(client, id).subscribe().with(ok(rc), error(rc));
    }

    @Route(path = "", methods = HttpMethod.POST, consumes = APPLICATION_JSON)
    public void startProcess(RoutingContext rc) {
        StartProcessRequest request = rc.getBodyAsJson().mapTo(StartProcessRequest.class);
        if (request == null) {
            rc.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end("Start process request not found!");
        }
        processStream.startProcess(request).subscribe().with(ok(rc), error(rc));
    }

    @RegisterForReflection
    public static class StartProcessRequest {
        public String id;
        public String processId;
        public String processVersion;
        public Map<String, Object> data;
    }
}
