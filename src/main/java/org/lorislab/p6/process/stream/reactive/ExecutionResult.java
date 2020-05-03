package org.lorislab.p6.process.stream.reactive;

import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;

import java.util.List;
import java.util.stream.Stream;

public class ExecutionResult {

    public ExecutionItem item;

    public List<ProcessToken> createTokens;

    public List<ProcessToken> tokens;

    public ProcessInstance processInstance;

    public List<Message> messages;

    public boolean save = false;

    public ExecutionResult(ExecutionItem item) {
        this.item = item;
    }
}
