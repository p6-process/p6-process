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

    public <T> T header(Class<T> clazz) {
        return this.header.mapTo(clazz);
    }

    public <T> T data(Class<T> clazz) {
        return this.data.mapTo(clazz);
    }

}
