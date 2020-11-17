package org.lorislab.p6.process.reactive;

import org.lorislab.p6.process.model.Message;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import java.util.ArrayList;
import java.util.List;

public class ExecutorItem {

//    public Message msg;

    public ProcessDefinitionRuntime pd;

    public ProcessToken token;

    public Node node;

    public ProcessInstance updateProcessInstance;

    public boolean end;

    public boolean check;

    public List<ProcessToken> createTokens = new ArrayList<>();

    public ProcessToken updateToken;

    public List<ProcessToken> messages = new ArrayList<>();

    public ExecutorItem copy() {
        ExecutorItem result = new ExecutorItem();
        result.end = end;
        result.check = check;
//        result.msg = msg;
        result.node = node;
        result.pd = pd;
        result.token = token;
        result.updateProcessInstance = updateProcessInstance;
        result.messages = messages;
        return result;
    }

    public void moveToNextItem(String next) {
        token.nodeName = next;
        if (token.nodeName != null) {
            node = pd.nodes.get(token.nodeName);
            token.type = ProcessToken.Type.valueOf(pd.nodes.get(token.nodeName));
        } else {
            token.type = ProcessToken.Type.NULL;
            node = null;
        }
    }

    @Override
    public String toString() {
        return "ExecutorItem{" +
                "token=" + token +
                ",node=" + node +
                '}';
    }
}
