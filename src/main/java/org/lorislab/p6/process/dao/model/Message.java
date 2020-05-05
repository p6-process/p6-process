package org.lorislab.p6.process.dao.model;

import java.time.Instant;

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
