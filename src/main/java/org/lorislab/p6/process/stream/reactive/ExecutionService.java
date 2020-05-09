package org.lorislab.p6.process.stream.reactive;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.model.ExclusiveGateway;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class ExecutionService {

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    public Uni<ExecutionResult> serviceTask(ExecutionItem item) {
        return startNode(item);
    }

    public Uni<ExecutionResult> startNode(ExecutionItem item) {
        String next = item.node.next.get(0);
        ProcessToken token = item.token.copy();
        token.status = ProcessToken.Status.IN_EXECUTION;
        token.nodeName = next;
        token.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));

        ExecutionResult result = new ExecutionResult(item);
        result.tokens = List.of(token);
        return Uni.createFrom().item(result);
    }

    public Uni<ExecutionResult> endNode(ExecutionItem item) {
        if (item.token.status != ProcessToken.Status.FINISHED) {
            ProcessToken token = item.token.copy();
            // load process instance
            return processInstanceDAO.findById(item.tx,token.processInstance)
                    .onItem().apply(p -> {
                        p.status = ProcessInstance.Status.FINISHED;
                        p.data.mergeIn(token.data);

                        ExecutionResult result = new ExecutionResult(item);
                        result.tokens = List.of(token);
                        result.processInstance = p;
                        result.save = true;
                        return result;
            });
        }
        return Uni.createFrom().nullItem();
    }

    public Uni<ExecutionResult> parallelGatewayDiverging(ExecutionItem item) {

        ProcessToken token = item.token.copy();
        token.status = ProcessToken.Status.FINISHED;

        ExecutionResult result = new ExecutionResult(item);
        result.tokens = List.of(token);
        result.createTokens = createChildTokens(item.token, item.pd, item.node.next);
        result.messages = result.createTokens.stream().map(x -> Message.create(x.id)).collect(Collectors.toList());
        result.save = true;
        return Uni.createFrom().item(result);
    }

    public Uni<ExecutionResult> parallelGatewayConverging(ExecutionItem item) {
        String next = item.node.next.get(0);

        Uni<ProcessToken> uni = processTokenDAO.findByReferenceAndNodeName(item.tx, item.token.parent, next)
                .ifNoItem().after(Duration.ZERO)
                .recoverWithUni(Uni.createFrom().item(() -> createParallelGatewayConvergingToken(item)));

        return  uni.onItem().apply(gt -> {
            // update existing token
            ProcessToken token = item.token.copy();
            token.status = ProcessToken.Status.FINISHED;

            // copy all data from existing token to gateway token
            gt.createdFrom.add(item.token.id);
            gt.data.mergeIn(item.token.data);

            // create execution result
            ExecutionResult result = new ExecutionResult(item);
            result.save = true;
            if (gt.created) {
                result.createTokens = List.of(gt);
                result.tokens = List.of(token);
            } else {
                result.tokens = List.of(token, gt);
            }

            // create message if we finished the gateway
            int size1 = gt.createdFrom.size();
            int size2 = item.node.previous.size();
            log.info("Token finished {} node parents {}. Result {}>={}, {}", gt.createdFrom, item.node.previous, size1, size2, size1 >= size2);
            if (size1 >= size2) {
                log.info("Parallel gateway finished Token:{}", gt);
                result.messages = List.of(Message.create(gt.id));
            }
            return result;
        });
    }

    public Uni<ExecutionResult> exclusiveGatewayDiverging(ExecutionItem item) {

        String next = null;
        ExclusiveGateway gateway = (ExclusiveGateway) item.node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (next == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), item.token.data);
            if (tmp) {
                next = key;
            }
        }
        log.info("ExclusiveGateway node: {} next: {} default: {}", item.node.name, next, gateway.defaultNext);
        if (next == null) {
            next = gateway.defaultNext;
        }

        ProcessToken token = item.token.copy();
        token.nodeName = next;
        token.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));

        ExecutionResult result = new ExecutionResult(item);
        result.tokens = List.of(token);
        return Uni.createFrom().item(result);
    }

    public Uni<ExecutionResult> exclusiveGatewayConverging(ExecutionItem item) {
        ProcessToken token = item.token.copy();
        token.status = ProcessToken.Status.IN_EXECUTION;
        token.nodeName = item.node.next.get(0);
        token.type = ProcessToken.Type.valueOf(item.pd.nodes.get(token.nodeName));

        ExecutionResult result = new ExecutionResult(item);
        result.tokens = List.of(token);
        return Uni.createFrom().item(result);
    }

    protected ProcessToken createParallelGatewayConvergingToken(ExecutionItem item) {
        ProcessToken token = item.token;
        String next = item.node.next.get(0);

        ProcessToken result = new ProcessToken();
        result.created = true;
        result.id = UUID.randomUUID().toString();
        result.status = ProcessToken.Status.CREATED;
        result.processId = token.processId;
        result.processVersion = token.processVersion;
        result.nodeName = token.nodeName;
        result.type = ProcessToken.Type.valueOf(item.pd.nodes.get(next));
        result.parent = token.parent;
        // FIXME
//        if (token.getParent() != null) {
//            ProcessToken parent = processTokenDAO.findByGuid(token.getParent());
//            if (parent != null && parent.getParent() != null) {
//                gt.setParent(parent.getParent());
//            }
//        }
        result.processInstance = token.processInstance;
        result.reference = token.parent;
        result.createdFrom.add(token.id);
//        result.messageId = token.messageId;
        result.executionId = UUID.randomUUID().toString();
        return result;
    }

    protected List<ProcessToken> createChildTokens(ProcessToken token, ProcessDefinitionRuntime pd, List<String> items) {
        return items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.created = true;
            child.id = UUID.randomUUID().toString();
            child.nodeName = item;
            child.processId = token.processId;
            child.processVersion = token.processVersion;
            child.parent = token.id;
            child.type = ProcessToken.Type.valueOf(pd.nodes.get(item));
            child.processInstance = token.processInstance;
            child.data = token.data;
            child.status= ProcessToken.Status.IN_EXECUTION;
            child.executionId = UUID.randomUUID().toString();
            return child;
        }).collect(Collectors.toList());
    }
}
