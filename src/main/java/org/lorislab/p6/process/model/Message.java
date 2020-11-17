package org.lorislab.p6.process.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@ToString
@RegisterForReflection
public class Message {

    public String id = UUID.randomUUID().toString();

    public Date created = new Date();

    public String ref;

    public String cmd;

    public long count = 0;

    public static Message create(String ref) {
        Message message = new Message();
        message.ref = ref;
        return message;
    }
}
