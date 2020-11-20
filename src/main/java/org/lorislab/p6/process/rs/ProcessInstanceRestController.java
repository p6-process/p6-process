package org.lorislab.p6.process.rs;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.lorislab.p6.process.model.ProcessInstanceRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.rs.Application.*;

@ApplicationScoped
@RouteBase(path = "processes", produces = APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    @Route(path = "instance/:id", methods = HttpMethod.GET)
    public void get(RoutingContext rc) {
        String id = rc.pathParam("id");
        processInstanceRepository.findById(id).subscribe().with(ok(rc), error(rc));
    }

    @Route(path = "command/:id", methods = HttpMethod.GET)
    public void getByCmdId(RoutingContext rc) {
        String commandId = rc.pathParam("id");
        processInstanceRepository.findByCmdId(commandId).subscribe().with(ok(rc), error(rc));
    }
}
