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
@RouteBase(path = "command", produces = APPLICATION_JSON)
public class CommandRestController {

    @Inject
    MessageProducer messageProducer;

    @Route(path = "start-process", methods = HttpMethod.POST, consumes = APPLICATION_JSON)
    public void startProcess(@Body StartProcessCommandDTO request, RoutingContext rc) {
        if (request == null) {
            rc.response().setStatusCode(Application.ResponseStatus.BAD_REQUEST).end("Start process request not found!");
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
}
