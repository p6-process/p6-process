package org.lorislab.p6.process.dao.model;

public enum MessageType {

    PROCESS_MSG("PROCESS_MSG", "process_msg"),

    TOKEN_MSG("TOKEN_MSG", "token_msg"),

    SINGLETON_MSG("SINGLETON_MSG","singleton_msg"),

    SERVICE_TASK_MSG("SERVICE_TASK_MSG", "service_task_msg");

    public final String table;

    public final String channel;

    MessageType(String table, String channel) {
        this.table = table;
        this.channel = channel;
    }
}
