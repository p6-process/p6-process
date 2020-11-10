package org.lorislab.p6.process.message;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@RegisterForReflection
public class Message {

    public Long id;

    public LocalDateTime date = LocalDateTime.now();

    public Long count = 0L;

    public JsonObject data = new JsonObject();

    public JsonObject header = new JsonObject();

    public String queue;

    public static Message create(String queue) {
        Message m = new Message();
        m.queue = queue;
        return m;
    }

    public Message data(JsonObject data) {
        this.data = data;
        return this;
    }

    public Message header(JsonObject header) {
        this.header = header;
        return this;
    }

    public Message header(String key, Object data) {
        this.header.put(key, data);
        return this;
    }
}
