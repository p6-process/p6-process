package org.lorislab.p6.process.token;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessInstanceRepository;
import org.lorislab.p6.process.model.ProcessTokenRepository;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.events.EventService;
import org.lorislab.p6.process.events.EventType;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageBuilder;
import org.lorislab.p6.process.message.MessageProducer;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class ProcessTokenService {

    @Inject
    DeploymentService deploymentService;

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Inject
    MessageProducer messageProducer;

    public Uni<Long> executeToken(Transaction tx, ProcessToken token, Long messageId) {

        if (token == null) {
            log.warn("No token found for the message id: {}", messageId);
            return Uni.createFrom().item(messageId);
        }

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(token.processId, token.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", token.processInstance, token.processId, token.processVersion);
            return Uni.createFrom().item(messageId);
        }

        RuntimeToken item = new RuntimeToken();
        item.messageId = messageId;
        item.pd = pd;
        item.tx = tx;
        item.token = token;
        item.node = pd.nodes.get(token.nodeName);

        return Uni.createFrom().item(item)
                .onItem().transformToUni(this::executeToken)
                .repeat().until(this::checkToken)
                .collectItems().last()
                .onItem().transformToUni(this::saveToken);
    }

    private boolean checkToken(RuntimeToken item) {
        log.info("Check token: {} - {} - {}", item.savePoint, item.token.type, item);
        return item.savePoint;
    }

    private Uni<RuntimeToken> executeToken(RuntimeToken item) {
        log.info("Execute token {} /  {}", item, item.token);
        Node node = item.node;
        if (node == null) {
            log.error("No node found in the process definition. The task will be ignored. Token: {}", item);
            return Uni.createFrom().nullItem();
        }

        ProcessToken.Type type = item.token.type;

        int nc = type.next;
        int size = 0;
        List<String> next = node.next;
        if (next != null ) {
            size = next.size();
        }

        // next > 0
        if (nc == -1) {
            if (size == 0) {
                log.error("The node type {} for token {} has wrong number of next tokens. Expected {} > 0.", type, node.name, size);
                return Uni.createFrom().nullItem();
            }
        } else if (size != nc) {
            log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} == {}.", type, node.name, nc, size);
            return Uni.createFrom().nullItem();
        }

        log.info("Execute node: {} Next: {} Type: {}", node.name, next, type);
        InstanceHandle<EventService> w = Arc.container().instance(EventService.class, EventType.Literal.create(type));
        if (!w.isAvailable()) {
            throw new UnsupportedOperationException("Not supported token type '" + type + "'");
        }
        return w.get().execute(item);
    }

    private Uni<Long> saveToken(RuntimeToken item) {
        log.info("Save messageId: {} token: {}", item.messageId, item.token);

        RuntimeToken.ChangeLog changeLog = item.changeLog;
        List<Uni<?>> items = new ArrayList<>();

        // update executed token
        if (item.token != null) {
            items.add(processTokenRepository.update(item.tx, item.token));

            // create message for the token
            Message message = RuntimeToken.ChangeLog.createMessage(item.token);
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token type: {} -> {}", item.token.type, message.queue);
                }
                items.add(messageProducer.send(item.tx, message));
            }
        }

        // update process instance
        if (changeLog.updateProcessInstance != null) {
            items.add(processInstanceRepository.update(item.tx, changeLog.updateProcessInstance));
        }

        // create process tokens
        if (!changeLog.tokens.isEmpty()) {
            items.add(processTokenRepository.create(item.tx, changeLog.tokens));
        }

        // create process tokens messages
        if (!changeLog.messages.isEmpty()) {
            for (Map.Entry<String, List<Message>> e : changeLog.messages.entrySet()) {
                items.add(messageProducer.send(item.tx, e.getKey(), e.getValue()));
            }
        }

        return Uni.combine().all().unis(items).combinedWith(x -> item.messageId);
    }

}
