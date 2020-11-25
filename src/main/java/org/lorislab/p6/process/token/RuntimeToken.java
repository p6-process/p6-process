package org.lorislab.p6.process.token;

import io.vertx.mutiny.sqlclient.Transaction;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageBuilder;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.ProcessDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        token.previousNodeName = token.nodeName;
        token.nodeName = null;
        token.type = ProcessToken.Type.NULL;
        node = null;
    }

    public void moveTo(String nodeName) {
        token.previousNodeName = token.nodeName;
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

        public Map<String, List<Message>> messages = new HashMap<>();

        public ProcessInstance updateProcessInstance;

        public void addMessage(ProcessToken token) {
            Message message = createMessage(token);
            if (message != null) {
                messages.computeIfAbsent(message.queue, k -> new ArrayList<>()).add(message);
            }
        }

        static Message createMessage(ProcessToken token) {
            if (token == null) {
                return null;
            }
            if (token.type == ProcessToken.Type.NULL) {
                return null;
            }
            TokenMessageHeader header = new TokenMessageHeader();
            header.tokenId = token.id;
            if (token.type.message.parameters) {
                header.processId = token.processId;
                header.processVersion = token.processVersion;
                header.nodeName = token.nodeName;
                header.processInstanceId = token.processInstance;
            }
            MessageBuilder mb = MessageBuilder.builder()
                    .queue(token.type.message.table)
                    .header(header);
            if (token.type.message.parameters) {
                mb.data(token.data);
            }
            return mb.build();
        }

    }
}
