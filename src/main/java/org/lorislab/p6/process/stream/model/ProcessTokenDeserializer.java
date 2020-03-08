package org.lorislab.p6.process.stream.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ProcessTokenDeserializer extends ObjectMapperDeserializer<ProcessTokenStream> {

    public ProcessTokenDeserializer(){
        super(ProcessTokenStream.class);
    }
}