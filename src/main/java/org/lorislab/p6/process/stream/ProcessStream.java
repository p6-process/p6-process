package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.stream.service.ExecutorTokenService;
import org.lorislab.p6.process.stream.service.ProcessService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ProcessStream {

    @Inject
    Logger log;

    @Inject
    ProcessService processService;

    @Inject
    ExecutorTokenService executorTokenService;

    @Incoming("process-start")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> processStart(AmqpMessage<String> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                JsonObject data = message.getApplicationProperties();
                String processId = data.getString("processId");
                String processInstanceId = data.getString("processInstanceId");
                String processVersion = data.getString("processVersion");

                List<ProcessToken> tokens = processService.createTokens(processInstanceId, processId, processVersion, message.getPayload());
                executorTokenService.executeResponse(null, null,  message.getAmqpMessage().correlationId(), tokens);

                message.getAmqpMessage().accepted();
            } catch (Exception wex) {
                log.error("Error process start message. Message {}", message);
                message.getAmqpMessage().modified(true, false);
            }
        });
    }

}
