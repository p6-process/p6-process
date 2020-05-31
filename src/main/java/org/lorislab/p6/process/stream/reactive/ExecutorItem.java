package org.lorislab.p6.process.stream.reactive;

import io.vertx.mutiny.sqlclient.Transaction;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

public class ExecutorItem {

    public Message msg;

    public ProcessDefinitionRuntime pd;

    public ProcessToken token;

    public Transaction tx;

    public Node node;

    public ProcessInstance updateProcessInstance;

    public boolean end;

    public void moveToNextItem(String next) {
        token.nodeName = next;
        if (token.nodeName != null) {
            node = pd.nodes.get(token.nodeName);
            token.type = ProcessToken.Type.valueOf(pd.nodes.get(token.nodeName));
        } else {
            token.type = null;
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
