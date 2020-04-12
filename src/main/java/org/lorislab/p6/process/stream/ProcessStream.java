package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.jms.IncomingJmsMessageMetadata;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.quarkus.reactive.jms.tx.IncomingJmsTxMessage;
import org.lorislab.quarkus.reactive.jms.tx.OutgoingJmsTxMessageMetadata;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
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
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> processStart(IncomingJmsTxMessage<StartProcessRequest> message) {
        try {
            log.info("Start process {}", message.getPayload());
            IncomingJmsMessageMetadata metadata = message.getJmsMetadata();
            List<ProcessToken> tokens = createTokens(metadata.getCorrelationId(), message.getPayload());
            message.send(tokens.stream().map(ProcessStream::createMessage));
            return message.ack();
        } catch (Exception wex) {
            log.error("Error start process", wex);
            log.error("Error process start message. Message {}", message);
            return message.rollback();
        }
    }

    public static Message<ProcessToken> createMessage(ProcessToken token) {
        Metadata m = OutgoingJmsTxMessageMetadata.builder()
                .withTypeQueue()
                .withDestination(token.type.route)
                .withCorrelationId(token.executionId)
                .build().of();
        return Message.of(token, m);
    }

    public List<ProcessToken> createTokens(String messageId, StartProcessRequest request) {

        ProcessDefinitionModel pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.processInstanceId, request.processId, request.processVersion);
            return Collections.emptyList();
        }

        ProcessInstance pi = new ProcessInstance();
        pi.id = request.processInstanceId;
        pi.messageId = messageId;
        pi.status = ProcessInstanceStatus.CREATED;
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        pi.data = request.data;

        processInstanceDAO.create(pi);

        final ProcessInstance ppi = pi;
        List<ProcessToken> tokens = pd.start.stream()
                .map(node -> {
                    ProcessToken token = new ProcessToken();
                    token.id = UUID.randomUUID().toString();
                    token.executionId = UUID.randomUUID().toString();
                    token.messageId = messageId;
                    token.processId = ppi.processId;
                    token.processVersion = ppi.processVersion;
                    token.processInstance = ppi.id;
                    token.nodeName = node.name;
                    token.status = ProcessTokenStatus.CREATED;
                    token.type = ProcessTokenType.valueOf(node);
                    token.data = ppi.data;
//                    token.setStartNodeName(node.name);
//                    token.setCreateNodeName(node.name);
//                    token.setStatus(ProcessTokenStatus.CREATED);
//                    token.setPreviousName(null);
                    return token;
                }).collect(Collectors.toList());

        processTokenDAO.persist(tokens);
        return tokens;
    }

    public static class StartProcessRequest {
        public String processId;
        public String processInstanceId;
        public String processVersion;
        public Map<String, Object> data;
    }
}
