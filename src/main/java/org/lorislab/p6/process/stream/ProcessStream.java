package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.smallrye.reactive.messaging.amqp.AmqpMessageBuilder;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessStream {

    @Inject
    Logger log;

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    DeploymentService deploymentService;

    @Incoming("process-start-in")
    @Outgoing("process-start-out")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public PublisherBuilder<AmqpMessage<ProcessToken>> processStart(AmqpMessage<StartProcessRequest> message) {
        try {
            Map<String, ProcessToken> tokens = createTokens(message.getAmqpMessage().id(), message.getPayload());
            if (tokens == null || tokens.isEmpty()) {
                return ReactiveStreams.empty();
            }
            return ReactiveStreams.of(tokens.values().toArray(new ProcessToken[0]))
                    .map(ProcessStream::createMessage)
                    .onError(e -> {
                        log.error("Error start tokens. Message {}", e.getMessage());
                        message.getAmqpMessage().modified(true, false);
                    })
                    .onComplete(() -> message.getAmqpMessage().accepted());
        } catch (Exception wex) {
            log.error("Error process start message. Message {}", message);
            message.getAmqpMessage().modified(true, false);
        }
        return ReactiveStreams.empty();
    }

    public static AmqpMessage<ProcessToken> createMessage(ProcessToken token) {
        AmqpMessageBuilder<ProcessToken> builder = AmqpMessage.builder();
        builder.withContentType(MediaType.APPLICATION_JSON);
        builder.withAddress(token.type.route);
        builder.withId(token.executionId);
        builder.withJsonObjectAsBody(JsonObject.mapFrom(token));
        return builder.build();
    }

    public Map<String, ProcessToken> createTokens(String messageId, StartProcessRequest request) {

        ProcessDefinitionModel pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.processInstanceId, request.processId, request.processVersion);
            return Collections.emptyMap();
        }

        ProcessInstance pi = new ProcessInstance();
        pi.guid = request.processInstanceId;
        pi.messageId = messageId;
        pi.status = ProcessInstanceStatus.CREATED;
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        pi.data = request.data;

        pi = processInstanceDAO.create(pi);

        final ProcessInstance ppi = pi;
        Map<String, ProcessToken> tokens = pd.start.stream()
                .map(node -> {
                    ProcessToken token = new ProcessToken();
                    token.guid = UUID.randomUUID().toString();
                    token.executionId = UUID.randomUUID().toString();
                    token.messageId = messageId;
                    token.processId = ppi.processId;
                    token.processVersion = ppi.processVersion;
                    token.processInstance = ppi.guid;
                    token.nodeName = node.name;
                    token.status = ProcessTokenStatus.CREATED;
                    token.type = ProcessTokenType.valueOf(node);
                    token.data = ppi.data;
//                    token.setStartNodeName(node.name);
//                    token.setCreateNodeName(node.name);
//                    token.setStatus(ProcessTokenStatus.CREATED);
//                    token.setPreviousName(null);

                    return token;
                }).collect(Collectors.toMap(t -> t.guid, t -> t));

        processTokenDAO.createAll(tokens);

        return tokens;
    }

    public class StartProcessRequest {
        public String processId;
        public String processInstanceId;
        public String processVersion;
        public Map<String, Object> data;
    }
}
