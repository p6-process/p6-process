package org.lorislab.p6.process.rs;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.lorislab.p6.process.model.ProcessTokenRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.rs.Application.*;


@ApplicationScoped
@RouteBase(path = "tokens", consumes = APPLICATION_JSON)
public class ProcessTokenRestController {

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Route(path = ":id", methods = HttpMethod.GET)
    public void get(RoutingContext rc) {
        String id = rc.pathParam("id");
        processTokenRepository.findById(id).subscribe().with(ok(rc), error(rc));
    }

}
