package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import lombok.ToString;
import org.lorislab.p6.process.model.Gateway;
import org.lorislab.p6.process.model.Node;

import java.util.*;

@ToString
@RegisterForReflection
public class ProcessToken {

    public String id = UUID.randomUUID().toString();

    public String processInstance;

    public String processId;

    public String processVersion;

    public String nodeName;

    public Status status = ProcessToken.Status.CREATED;

    public Type type;

    public String parent;

    public String reference;

    public Set<String> createdFrom = new HashSet<>();

    public JsonObject data = new JsonObject();

    public enum Status {

        CREATED,

        IN_EXECUTION,

        FAILED,

        FINISHED;

    }

    public enum Type {

        NULL(null, 9),

        START_EVENT(MessageType.TOKEN_MSG, 1),

        END_EVENT(MessageType.SINGLETON_MSG, 0),

        SERVICE_TASK(MessageType.TOKEN_MSG, 1),

        SERVICE_TASK_COMPLETE(MessageType.SERVICE_TASK_MSG, 1),

        PARALLEL_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        PARALLEL_GATEWAY_CONVERGING(MessageType.SINGLETON_MSG, 1),

        EXCLUSIVE_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        EXCLUSIVE_GATEWAY_CONVERGING(MessageType.TOKEN_MSG, 1),

        INCLUSIVE_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        INCLUSIVE_GATEWAY_CONVERGING(MessageType.SINGLETON_MSG, 1),
        ;

        public final MessageType message;

        public final int next;

        Type(MessageType message, int next) {
            this.next = next;
            this.message = message;
        }

        public static Type valueOf(Node node) {
            String tmp = node.type.name();
            if (node instanceof Gateway) {
                Gateway pg = (Gateway) node;
                tmp = pg.type + "_" + pg.sequence;
            }
            return Type.valueOf(tmp);
        }

    }

}
