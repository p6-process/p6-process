package org.lorislab.p6.process.token;

import io.vertx.mutiny.sqlclient.Transaction;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;

public class RuntimeToken {

    public Long messageId;

    public ProcessToken token;

    public ProcessDefinition pd;

    public Transaction tx;

    public Node node;

    public boolean savePoint;

    public ChangeLog changeLog = new ChangeLog();

    public void moveToNext() {
        moveTo(node.next.get(0));
    }

    public void moveToNull() {
        token.nodeName = null;
        token.type = ProcessToken.Type.NULL;
        node = null;
    }

    private void moveTo(String nodeName) {
        token.nodeName = nodeName;
        node = pd.nodes.get(token.nodeName);
        token.type = ProcessToken.Type.valueOf(pd.nodes.get(token.nodeName));
    }

    @Override
    public String toString() {
        return "RuntimeToken:" + messageId;
    }

    public static class ChangeLog {

        public List<ProcessToken> tokens = new ArrayList<>();

        public List<ProcessToken> messages = new ArrayList<>();

        public ProcessToken updateToken;

        public ProcessInstance updateProcessInstance;

    }
}
