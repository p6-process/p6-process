package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.stream.service.ExecutorTokenService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ExecutorTokenStream {

    @Inject
    Logger log;

    @Inject
    ExecutorTokenService executorTokenService;

    @Incoming("token-execute")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> message(AmqpMessage<String> message) {
        return execute(message);
    }

    @Incoming("token-execute-singleton")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> singleton(AmqpMessage<String> message) {
        return execute(message);
    }

    private CompletionStage<Void> execute(AmqpMessage<String> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                JsonObject json = message.getApplicationProperties();
                String guid = json.getString("guid");
                String name = json.getString("name");

                List<ProcessToken> tokens = executorTokenService.executeToken(guid, name, message.getPayload());
                executorTokenService.executeResponse(guid, name, message.getAmqpMessage().correlationId(), tokens);
                message.getAmqpMessage().accepted();
            } catch (Exception e) {
                log.error("Error token message. Message {}", message);
                message.getAmqpMessage().modified(true, false);
            }
        });
    }

}
