package org.lorislab.p6.process.rs;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.util.Map;

@ToString
@RegisterForReflection
public class StartProcessRequestDTO {
    public String id;
    public String processId;
    public String processVersion;
    public String reference;
    public Map<String, Object> data;
}
