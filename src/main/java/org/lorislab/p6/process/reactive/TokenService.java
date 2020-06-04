package org.lorislab.p6.process.reactive;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.events.EventService;
import org.lorislab.p6.process.events.EventType;
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
public class TokenService {

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    DeploymentService deploymentService;

    @Inject
    MessageDAO messageDAO;

    public Uni<Message> executeMessage(Transaction tx, Message m) {
        log.info("Token message: {}", m);
        return processTokenDAO.findById(tx, m.ref)
                .map(t -> executeToken(tx, m, t)).flatMap(x -> x);
    }

    private Uni<Message> executeToken(Transaction tx, Message m, ProcessToken pt) {
        if (pt == null) {
            log.warn("No token found for the message token: {}", m);
            return Uni.createFrom().item(m);
        }

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(pt.processId, pt.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", pt.processInstance, pt.processId, pt.processVersion);
            return Uni.createFrom().item(m);
        }

        ExecutorItem token = new ExecutorItem();
        token.pd = pd;
        token.tx = tx;
        token.msg = m;
        token.token = pt;
        token.node = token.pd.nodes.get(token.token.nodeName);

        return Uni.createFrom().item(token)
                .onItem().produceUni(this::executeToken)
                .repeat().until(this::checkToken)
                .collectItems().last()
                .onItem().produceUni(this::saveToken);
    }

    private boolean checkToken(ExecutorItem item) {
        log.info("Check token: {} - {} - {}", item.end, item.token.type, item);
        item.check = item.node == null
                || item.token.type == ProcessToken.Type.NULL
                || ProcessToken.Type.ROUTE_SINGLETON.equals(item.token.type.route);
        return item.end;
    }

    private Uni<ExecutorItem> executeToken(ExecutorItem item) {

        if (item.check) {
            item.end = true;
            return Uni.createFrom().item(item);
        }

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
                log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} > 0.", type, node.name, size);
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

    private Uni<Message> saveToken(ExecutorItem item) {
        log.info("Save token {}", item);

        List<Uni<?>> items = new ArrayList<>();
        if (item.token != null) {
            items.add(processTokenDAO.update(item.tx, item.token));
            // create message for the token
            if (item.token.type != ProcessToken.Type.NULL) {
                items.add(messageDAO.createMessage(item.tx, item.token));
            }
        }
        // update process instance
        if (item.updateProcessInstance != null) {
            items.add(processInstanceDAO.update(item.tx, item.updateProcessInstance));
        }
        // update token
        if (item.updateToken != null) {
            items.add(processTokenDAO.update(item.tx, item.updateToken));
        }
        // create process tokens
        if (!item.createTokens.isEmpty()) {
            items.add(processTokenDAO.create(item.tx, item.createTokens));
        }
        // create process tokens messages
        if (!item.messages.isEmpty()) {
            Map<ProcessToken.Type, List<ProcessToken>> tmp = item.messages.stream().collect(Collectors.groupingBy(d -> d.type));
            for (Map.Entry<ProcessToken.Type, List<ProcessToken>> e : tmp.entrySet()) {
                items.add(messageDAO.createMessages(item.tx, item.messages, e.getKey().route));
            }
        }
        return Uni.combine().all().unis(items).combinedWith(x -> item.msg);
    }

}
