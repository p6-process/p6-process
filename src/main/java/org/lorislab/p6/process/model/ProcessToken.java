package org.lorislab.p6.process.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import org.lorislab.p6.process.rs.JsonObjectDeserializer;

import java.util.*;

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

    @JsonDeserialize(using = JsonObjectDeserializer.class)
    public JsonObject data = new JsonObject();

    @Override
    public String toString() {
        return "ProcessToken:" + id;
    }

    public enum Status {

        CREATED,

        IN_EXECUTION,

        FAILED,

        FINISHED;

    }

    public enum Type {

        NULL(null, 9),

        START_EVENT(ProcessTokenMessageType.TOKEN_MSG, 1),

        END_EVENT(ProcessTokenMessageType.SINGLETON_MSG, 0),

        SERVICE_TASK(ProcessTokenMessageType.SERVICE_TASK_MSG, 1),

        SERVICE_TASK_COMPLETE(ProcessTokenMessageType.TOKEN_MSG, 1),

        PARALLEL_GATEWAY_DIVERGING(ProcessTokenMessageType.TOKEN_MSG, -1),

        PARALLEL_GATEWAY_CONVERGING(ProcessTokenMessageType.SINGLETON_MSG, 1),

        EXCLUSIVE_GATEWAY_DIVERGING(ProcessTokenMessageType.TOKEN_MSG, -1),

        EXCLUSIVE_GATEWAY_CONVERGING(ProcessTokenMessageType.TOKEN_MSG, 1),

        INCLUSIVE_GATEWAY_DIVERGING(ProcessTokenMessageType.TOKEN_MSG, -1),

        INCLUSIVE_GATEWAY_CONVERGING(ProcessTokenMessageType.SINGLETON_MSG, 1),
        ;

        public final ProcessTokenMessageType message;

        public final int next;

        Type(ProcessTokenMessageType message, int next) {
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
