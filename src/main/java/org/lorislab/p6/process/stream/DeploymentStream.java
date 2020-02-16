package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.lorislab.p6.process.stream.service.DeploymentService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class DeploymentStream {

    @Inject
    Logger log;

    @Inject
    DeploymentService deploymentService;

    @Incoming("deployment")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> deployment(AmqpMessage<String> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                String deploymentId = message.getAmqpMessage().correlationId();
                deploymentService.deploy(deploymentId, message.getPayload());
                message.getAmqpMessage().accepted();
            } catch (Exception wex) {
                log.error("Error deployment message. Message {}", message);
                message.getAmqpMessage().modified(true, false);
            }
        });
    }
}
