package org.lorislab.p6.process.stream.model;

public enum ProcessTokenResponseStream {

    DEFAULT("token-exec-default"),

    SINGLETON("token-exec-singleton"),

    SERVICE_TASK("service-task-out");

    public String topic;

    private ProcessTokenResponseStream(String topic) {
        this.topic = topic;
    }
}
