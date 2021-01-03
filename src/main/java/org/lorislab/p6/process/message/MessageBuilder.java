package org.lorislab.p6.process.message;

import io.vertx.core.json.JsonObject;

public class MessageBuilder {

    private Message message = new Message();

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public Message build() {
        return message;
    }

    public MessageBuilder queue(String queue) {
        message.queue = queue;
        return this;
    }

    public MessageBuilder data(JsonObject data) {
        message.data = data;
        return this;
    }

    public MessageBuilder data(Object header) {
        message.data = JsonObject.mapFrom(header);
        return this;
    }

    public MessageBuilder header(Object header) {
        message.header = JsonObject.mapFrom(header);
        return this;
    }

    public MessageBuilder header(JsonObject header) {
        message.header = header;
        return this;
    }

    public MessageBuilder header(String key, Object data) {
        message.header.put(key, data);
        return this;
    }

    private MessageBuilder() {};
}
