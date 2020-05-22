package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;

@ToString
@RegisterForReflection
public class Message {

    public Long id;

    public Long created;

    public String ref;

    public String cmd;

    public static Message create(String ref) {
        Message message = new Message();
        message.ref = ref;
        return message;
    }
}
