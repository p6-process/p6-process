package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@RegisterForReflection
public class ProcessQueue {

    public Long id;

    public LocalDateTime date = LocalDateTime.now();

    public Long count = 0L;

    public JsonObject data = new JsonObject();

}
