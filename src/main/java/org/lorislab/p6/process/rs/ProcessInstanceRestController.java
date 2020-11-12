package org.lorislab.p6.process.rs;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageBuilder;
import org.lorislab.p6.process.message.MessageProducer;
import org.lorislab.p6.process.message.Queues;
import org.lorislab.p6.process.pi.ProcessMessageHeader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.lorislab.p6.process.rs.Application.*;

@ApplicationScoped
@RouteBase(path = "instances", produces = APPLICATION_JSON)
public class ProcessInstanceRestController {

//    @Inject
//    ProcessService processStream;

    @Inject
    MessageProducer messageProducer;

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

        ProcessMessageHeader header = new ProcessMessageHeader();
        header.command = ProcessMessageHeader.Command.START_PROCESS;

        Message message = MessageBuilder.builder()
                .queue(Queues.PROCESS_REQUEST)
                .data(rc.getBodyAsJson())
                .header(header)
                .build();

        messageProducer.send(message).subscribe().with(accepted(rc), error(rc));
    }

//    @Route(path = ":id/tokens", methods = HttpMethod.GET)
//    public void getTokens(RoutingContext rc) {
//        String id = rc.pathParam("id");
//        processTokenDAO.findByProcessInstance(id).subscribe().with(ok(rc), error(rc));
//    }

}
