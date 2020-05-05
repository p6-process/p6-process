package org.lorislab.p6.process.rs;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.pgclient.PgPool;
import org.lorislab.p6.process.dao.ProcessTokenDAO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.rs.P6Application.*;


@ApplicationScoped
@RouteBase(path = "token", consumes = APPLICATION_JSON)
public class ProcessTokenRestController {

    @Inject
    PgPool client;

    @Route(path = ":id", methods = HttpMethod.GET)
    public void get(RoutingContext rc) {
        String id = rc.pathParam("id");
        ProcessTokenDAO.findById(client, id).subscribe().with(ok(rc), error(rc));
    }
}
