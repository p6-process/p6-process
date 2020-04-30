package org.lorislab.p6.process.stream.reactive;

import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;

import java.util.stream.Stream;

public class ExecutionResult {

    public ExecutionItem item;

    public Stream<ProcessToken> createTokens;

    public Stream<ProcessToken> updateTokens;

    public ProcessInstance updateProcessInstance;

    public boolean save = false;

    public ExecutionResult(ExecutionItem item) {
        this.item = item;
    }
}
