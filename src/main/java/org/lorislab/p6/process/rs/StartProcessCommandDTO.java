package org.lorislab.p6.process.rs;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ToString
@RegisterForReflection
public class StartProcessCommandDTO {
    public String id = UUID.randomUUID().toString();
    public String processId;
    public String processVersion;
    public String reference;
    public Map<String, Object> data = new HashMap<>();
}
