package org.lorislab.p6.process.pi;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.JsonObject;
import lombok.ToString;

@ToString
@RegisterForReflection
public class StartProcessCommand {
    public String id;
    public String processId;
    public String processVersion;
    public String reference;
    public JsonObject data;
}
