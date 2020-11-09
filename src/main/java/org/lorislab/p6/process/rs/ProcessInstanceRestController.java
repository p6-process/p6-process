package org.lorislab.p6.process.rs;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.lorislab.p6.process.reactive.ProcessService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.rs.Application.*;

@ApplicationScoped
@RouteBase(path = "instances", produces = APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    ProcessService processStream;

//    @Inject
//    ProcessInstanceDAO processInstanceDAO;
//
//    @Inject
//    ProcessTokenDAO processTokenDAO;

//    @Route(path = ":id", methods = HttpMethod.GET)
//    public void get(RoutingContext rc) {
//        String id = rc.pathParam("id");
//        processInstanceDAO.findById(id).subscribe().with(ok(rc), error(rc));
//    }
//
//    @Route(path = "test/:id", methods = HttpMethod.GET)
//    public void test(RoutingContext rc) {
//        String id = rc.pathParam("id");
//        processStream.find(id).subscribe().with(ok(rc), error(rc));
//    }

    @Route(path = "", methods = HttpMethod.POST, consumes = APPLICATION_JSON)
    public void startProcess(@Body StartProcessRequestDTO request, RoutingContext rc) {
        if (request == null) {
            rc.response().setStatusCode(ResponseStatus.BAD_REQUEST).end("Start process request not found!");
        }
        processStream.createRequest(rc.getBodyAsJson()).subscribe().with(accepted(rc), error(rc));
    }

//    @Route(path = ":id/tokens", methods = HttpMethod.GET)
//    public void getTokens(RoutingContext rc) {
//        String id = rc.pathParam("id");
//        processTokenDAO.findByProcessInstance(id).subscribe().with(ok(rc), error(rc));
//    }

}
