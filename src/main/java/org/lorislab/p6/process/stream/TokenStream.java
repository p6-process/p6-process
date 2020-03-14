package org.lorislab.p6.process.stream;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.stream.events.EventService;
import org.lorislab.p6.process.stream.events.EventServiceType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TokenStream {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    DeploymentService deploymentService;

    @Incoming("token-in")
    @Outgoing("token-out")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public PublisherBuilder<AmqpMessage<JsonObject>> message(AmqpMessage<JsonObject> message) {
        return execute(message);
    }

    @Incoming("token-singleton-in")
    @Outgoing("token-singleton-out")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public PublisherBuilder<AmqpMessage<JsonObject>> singleton(AmqpMessage<JsonObject> message) {
        System.out.println("### " + message.getPayload().toString());
        return execute(message);
    }

    private PublisherBuilder<AmqpMessage<JsonObject>> execute(AmqpMessage<JsonObject> message) {
        try {
            List<ProcessToken> tokens = executeToken(message.getAmqpMessage().id(), message.getPayload().mapTo(ProcessToken.class));
            if (tokens == null || tokens.isEmpty()) {
                return ReactiveStreams.empty();
            }
            return ReactiveStreams.of(tokens.toArray(new ProcessToken[0]))
                    .map(ProcessStream::createMessage)
                    .onError(e -> {
                        log.error("Error execute token. Message {}", e.getMessage());
                        message.getAmqpMessage().modified(true, false);
                    })
                    .onComplete(() -> message.getAmqpMessage().accepted());
        } catch (Exception wex) {
            log.error("Error token message. Message {}", message);
            message.getAmqpMessage().modified(true, false);
        }
        return ReactiveStreams.empty();
    }

    public List<ProcessToken> executeToken(String messageId, ProcessToken token) {

        ProcessToken pt = processTokenDAO.findByGuid(token.guid);
        if (pt == null) {
            log.warn("No token found for the message token: {}", token);
            return Collections.emptyList();
        }
        if (!pt.executionId.equals(messageId)) {
            log.warn("Wrong message ID for the token {}. MessageId: {} excepted: {}", pt, messageId, pt.executionId);
            return Collections.emptyList();
        }

        ProcessDefinitionModel pd = deploymentService.getProcessDefinition(token.processId, token.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", token.processInstance, token.processId, token.processVersion);
            return Collections.emptyList();
        }

        Node node = pd.getNode(token.nodeName);
        if (node == null) {
            log.error("No node found in the process definition. The task will be ignored. Token: {}", token);
            return Collections.emptyList();
        }

        int nc = token.type.nextNodeCount;
        int size = 0;
        List<String> next = null;
        if (node.sequence != null && node.sequence.to != null) {
            next = node.sequence.to;
            size = next.size();
        }

        // next > 0
        if (nc == -1) {
            if (size == 0) {
                log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} > 0.", token.type, token.nodeName, size);
                return Collections.emptyList();
            }
        } else if (size != nc) {
            log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} == {}.", token.type, token.nodeName, nc, size);
            return Collections.emptyList();
        }

        log.info("Execute node: {} Next: {}", node.name, next);
        InstanceHandle<EventService> w = Arc.container().instance(EventService.class, EventServiceType.Literal.create(token.type));
        if (!w.isAvailable()) {
            throw new UnsupportedOperationException("Not supported token type '" + token.type + "'");
        }
        return w.get().execute(messageId, token, pd, node);
    }
}
