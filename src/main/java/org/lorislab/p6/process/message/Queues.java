package org.lorislab.p6.process.message;

public interface Queues {
    String PROCESS_REQUEST = "REQUEST_PROCESS_QUEUE";
    String TOKEN_EXECUTE_QUEUE = "TOKEN_EXECUTE_QUEUE";
    String TOKEN_SINGLETON_QUEUE = "TOKEN_SINGLETON_QUEUE";
    String SERVICE_TASK_REQUEST_QUEUE = "SERVICE_TASK_REQUEST_QUEUE";
    String SERVICE_TASK_RESPONSE_QUEUE = "SERVICE_TASK_RESPONSE_QUEUE";
}
