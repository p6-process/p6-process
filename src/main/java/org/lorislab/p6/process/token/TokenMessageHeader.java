package org.lorislab.p6.process.token;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.util.UUID;

@ToString
@RegisterForReflection
public class TokenMessageHeader {

    public String id = UUID.randomUUID().toString();

    public String tokenId;

    public String processInstanceId;

    public String processId;

    public String processVersion;

    public String nodeId;
}
