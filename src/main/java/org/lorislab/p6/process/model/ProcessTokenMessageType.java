package org.lorislab.p6.process.model;

public enum ProcessTokenMessageType {

    TOKEN_MSG("TOKEN_EXECUTE_QUEUE", false),

    SINGLETON_MSG("TOKEN_SINGLETON_QUEUE", false),

    SERVICE_TASK_MSG("SERVICE_TASK_REQUEST_QUEUE", true);

    public final String table;

    public final boolean parameters;

    ProcessTokenMessageType(String table, boolean parameters) {
        this.table = table;
        this.parameters = parameters;
    }
}
