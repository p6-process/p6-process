package org.lorislab.p6.process.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.reactive.messaging.jms.IncomingJmsMessageMetadata;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.stream.events.EventService;
import org.lorislab.p6.process.stream.events.EventServiceType;
import org.lorislab.quarkus.reactive.jms.InputJmsMessage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

@ApplicationScoped
public class TokenStream {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    DeploymentService deploymentService;

    @Incoming("token-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> message(InputJmsMessage<ProcessToken> message) {
        return execute(message);
    }

    @Incoming("token-singleton-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> singleton(InputJmsMessage<ProcessToken> message) {
        return execute(message);
    }

    private CompletionStage<Void> execute(InputJmsMessage<ProcessToken> message) {
        try {
            IncomingJmsMessageMetadata metadata = message.getJmsMetadata();
            List<ProcessToken> tokens = executeToken(metadata.getCorrelationId(), message.getPayload());
            message.send(tokens.stream().map(ProcessStream::createMessage));
            return message.ack();
        } catch (Exception wex) {
            log.error("Error token message. Message {}", message);
            log.error("Error token message.", wex);
            return message.rollback();
        }
    }

    public List<ProcessToken> executeToken(String messageId, ProcessToken token) {

        ProcessToken pt = processTokenDAO.findByGuid(token.id);
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
