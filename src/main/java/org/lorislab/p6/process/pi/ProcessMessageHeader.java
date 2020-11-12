package org.lorislab.p6.process.pi;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.util.UUID;

@ToString
@RegisterForReflection
public class ProcessMessageHeader {

    public String id = UUID.randomUUID().toString();

    public Command command;

    public enum Command {
        START_PROCESS,
    }
}
