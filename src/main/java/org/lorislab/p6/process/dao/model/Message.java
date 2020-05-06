package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

@RegisterForReflection
public class Message {

    public Long id;

    public Long created;

    public String ref;

    public static Message create(String ref) {
        Message message = new Message();
        message.created = Instant.now().getEpochSecond();
        return message;
    }
}
