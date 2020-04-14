package org.lorislab.p6.process.stream;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
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
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.stream.events.EventService;
import org.lorislab.p6.process.stream.events.EventServiceType;
import org.lorislab.quarkus.reactive.jms.tx.OutgoingJmsTxMessageMetadata;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TokenExecutor {

    @Inject
    Logger log;

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    DeploymentService deploymentService;

    public static Message<ProcessToken> createMessage(ProcessToken token) {
        Metadata m = OutgoingJmsTxMessageMetadata.builder()
                .withTypeQueue()
                .withDestination(token.getType().route)
                .withCorrelationId(token.getExecutionId())
                .build().of();
        return Message.of(token, m);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<ProcessToken> createTokens(String messageId, ProcessStream.StartProcessRequest request) {

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.processInstanceId, request.processId, request.processVersion);
            return Collections.emptyList();
        }

        ProcessInstance pi = new ProcessInstance();
        pi.setId(request.processInstanceId);
        pi.setMessageId(messageId);
        pi.setStatus(ProcessInstanceStatus.CREATED);
        pi.setProcessId(request.processId);
        pi.setProcessVersion(request.processVersion);
        pi.setData(request.data);

        processInstanceDAO.create(pi);

        final ProcessInstance ppi = pi;
        List<ProcessToken> tokens = pd.startNodes.values().stream()
                .map(node -> {
                    ProcessToken token = new ProcessToken();
                    token.setId(UUID.randomUUID().toString());
                    token.setExecutionId(UUID.randomUUID().toString());
                    token.setMessageId(messageId);
                    token.setProcessId(ppi.getProcessId());
                    token.setProcessVersion(ppi.getProcessVersion());
                    token.setProcessInstance(ppi.getId());
                    token.setNodeName(node.name);
                    token.setStatus(ProcessTokenStatus.CREATED);
                    token.setType(ProcessTokenType.valueOf(node));
                    token.setData(ppi.getData());
//                    token.setStartNodeName(node.name);
//                    token.setCreateNodeName(node.name);
//                    token.setStatus(ProcessTokenStatus.CREATED);
//                    token.setPreviousName(null);
                    return token;
                }).collect(Collectors.toList());

        processTokenDAO.createTokens(tokens);
        return tokens;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<ProcessToken> executeToken(String messageId, ProcessToken token) {

        ProcessToken pt = processTokenDAO.findByGuid(token.getId());
        if (pt == null) {
            log.warn("No token found for the message token: {}", token);
            return Collections.emptyList();
        }
        if (!pt.getExecutionId().equals(messageId)) {
            log.warn("Wrong message ID for the token {}. MessageId: {} excepted: {}", pt, messageId, pt.getExecutionId());
            return Collections.emptyList();
        }

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(token.getProcessId(), token.getProcessVersion());
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", token.getProcessInstance(), token.getProcessId(), token.getProcessVersion());
            return Collections.emptyList();
        }

        Node node = pd.nodes.get(token.getNodeName());
        if (node == null) {
            log.error("No node found in the process definition. The task will be ignored. Token: {}", token);
            return Collections.emptyList();
        }

        int nc = token.getType().nextNodeCount;
        int size = 0;
        List<String> next = node.next;
        if (next != null ) {
            size = next.size();
        }

        // next > 0
        if (nc == -1) {
            if (size == 0) {
                log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} > 0.", token.getType(), token.getNodeName(), size);
                return Collections.emptyList();
            }
        } else if (size != nc) {
            log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} == {}.", token.getType(), token.getNodeName(), nc, size);
            return Collections.emptyList();
        }

        log.info("Execute node: {} Next: {} Type: {}", node.name, next, token.getType());
        InstanceHandle<EventService> w = Arc.container().instance(EventService.class, EventServiceType.Literal.create(token.getType()));
        if (!w.isAvailable()) {
            throw new UnsupportedOperationException("Not supported token type '" + token.getType() + "'");
        }
        return w.get().execute(messageId, pt, pd, node, token);
    }
}
