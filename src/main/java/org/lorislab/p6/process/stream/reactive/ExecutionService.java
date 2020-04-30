package org.lorislab.p6.process.stream.reactive;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ExecutionService {

    public Uni<ExecutionResult> serviceTask(ExecutionItem item) {
        return startNode(item);
    }

    public Uni<ExecutionResult> startNode(ExecutionItem item) {
        String next = item.node.next.get(0);
        ProcessToken token = item.token.copy();
        token.status = ProcessTokenStatus.IN_EXECUTION;
        token.nodeName = next;
        token.executionId = UUID.randomUUID().toString();
        token.type = ProcessTokenType.valueOf(item.pd.nodes.get(next));

        ExecutionResult result = new ExecutionResult(item);
        result.updateTokens = Stream.of(token);
        result.save = false;
        return Uni.createFrom().item(result);
    }

    public Uni<ExecutionResult> endNode(ExecutionItem item) {
        if (item.token.status != ProcessTokenStatus.FINISHED) {
            ProcessToken token = item.token.copy();

            return ProcessInstance.findById(item.tx,token.processInstance)
                    .onItem().apply(p -> {
                        p.status = ProcessInstanceStatus.FINISHED;
                        p.data.mergeIn(token.data);

                        ExecutionResult result = new ExecutionResult(item);
                        result.updateTokens = Stream.of(token);
                        result.updateProcessInstance = p;
                        result.save = true;
                        return result;
            });
        }
        return Uni.createFrom().nullItem();
    }

    public Uni<ExecutionResult> parallelGatewayDiverging(ExecutionItem item) {

        ProcessToken token = item.token.copy();
        token.status = ProcessTokenStatus.FINISHED;

        ExecutionResult result = new ExecutionResult(item);
        result.updateTokens = Stream.of(token);
        result.createTokens = createChildTokens(item.token, item.pd, item.node.next);
        result.save = true;
        return Uni.createFrom().item(result);
    }

    public Uni<ExecutionResult> parallelGatewayConverging(ExecutionItem item) {

        ExecutionResult result = new ExecutionResult(item);
        return Uni.createFrom().item(result);
    }

    protected Stream<ProcessToken> createChildTokens(ProcessToken token, ProcessDefinitionRuntime pd, List<String> items) {
        return items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.id = UUID.randomUUID().toString();
            child.nodeName = item;
            child.processId = token.processId;
            child.processVersion = token.processVersion;
            child.parent = token.id;
            child.type = ProcessTokenType.valueOf(pd.nodes.get(item));
            child.processInstance = token.processInstance;
            child.data = token.data;
            child.status= ProcessTokenStatus.IN_EXECUTION;
            child.executionId = UUID.randomUUID().toString();
            return child;
        });
    }
}
