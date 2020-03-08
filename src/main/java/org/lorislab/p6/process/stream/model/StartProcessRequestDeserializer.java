package org.lorislab.p6.process.stream.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class StartProcessRequestDeserializer extends ObjectMapperDeserializer<StartProcessRequest> {

    public StartProcessRequestDeserializer(){
        super(StartProcessRequest.class);
    }
}