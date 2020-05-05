package org.lorislab.p6.process.rs;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
public class StartProcessRequestDTO {
    public String id;
    public String processId;
    public String processVersion;
    public Map<String, Object> data;
}
