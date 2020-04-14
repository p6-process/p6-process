/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.test;

import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.stream.ProcessStream;
import org.lorislab.quarkus.testcontainers.DockerComposeService;
import org.lorislab.quarkus.testcontainers.DockerComposeTestResource;
import org.lorislab.quarkus.testcontainers.DockerService;
import org.lorislab.quarkus.testcontainers.InjectLoggerExtension;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.jms.*;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.IllegalStateException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.lorislab.quarkus.reactive.jms.tx.IncomingJmsTxMessage.createJmsMessage;

/**
 * The abstract test
 */
@ExtendWith(InjectLoggerExtension.class)
@QuarkusTestResource(DockerComposeTestResource.class)
public abstract class AbstractTest {

    public static String ADDRESS_DEPLOYMENT = "deployment";

    static String ADDRESS_START_PROCESS = "process-start";

    static String ADDRESS_SERVICE_TASK = "service-task";

    static String ADDRESS_TOKEN_EXECUTE = "token-execute";

    @Inject
    protected Logger log;

    /**
     * Starts the containers before the tests
     */
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DockerService("p6-executor")
    protected DockerComposeService app;

    @BeforeEach
    public void init() {
        if (app != null) {
            RestAssured.port = app.getPort(8080);
        }
    }

    protected ConnectionFactory createConnectionFactory() {
        String username = System.getProperty("quarkus.artemis.username");
        String password = System.getProperty("quarkus.artemis.password");
        String url = System.getProperty("quarkus.artemis.url");
        return new ActiveMQConnectionFactory(url, username, password);
    }

    protected ProcessToken receivedMessage(String address) {
        return receivedMessage(address, 3000);
    }

    protected ProcessToken receivedMessage(String address, long timeout) {
        try {
            ConnectionFactory cf = createConnectionFactory();
            try (JMSContext context = cf.createContext(Session.AUTO_ACKNOWLEDGE);) {
                Destination destination = context.createQueue(address);
                JMSConsumer consumer = context.createConsumer(destination);
                Message message = consumer.receive(timeout);

                String content = message.getBody(String.class);
                if (content != null && !content.isBlank()) {
                    Jsonb jsonb = JsonbBuilder.create();
                    return jsonb.fromJson(content, ProcessToken.class);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Error received message", ex);
        }
        return null;
    }

    protected <T> void sendMessage(String address, MessageBeforeSendListener<T> listener, T... items) {
        try {
            ConnectionFactory cf = createConnectionFactory();
            try (JMSContext context = cf.createContext(Session.SESSION_TRANSACTED);) {
                Destination destination = context.createQueue(address);
                JMSProducer producer = context.createProducer();


                Jsonb jsonb = JsonbBuilder.create();
                for (T item : items) {
                    Message message = createJmsMessage(context, jsonb, item);
                    if (listener != null) {
                        listener.update(message, item);
                    }
                    producer.send(destination, message);
                }
                context.commit();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error received message", ex);
        }
    }

    protected void waitProcessFinished(String processId, String processInstanceId) {

        log.info("Wait for the process {} to finished execution guid {} ", processId, processInstanceId);
        await()
                .atMost(7, SECONDS)
                .untilAsserted(() -> given()
                        .when()
                        .contentType(MediaType.APPLICATION_JSON)
                        .pathParam("guid", processInstanceId)
                        .get("/v1/instance/{guid}")
                        .prettyPeek()
                        .then()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .body("status", equalTo("FINISHED"))
                );
    }

    protected String startProcess(String processId, String processVersion, Map<String, Object> body) {
        log.info("Test {}:{}", processId, processVersion);
        String processInstanceId = UUID.randomUUID().toString();


        ProcessStream.StartProcessRequest request = new ProcessStream.StartProcessRequest();
        request.processId = processId;
        request.processInstanceId = processInstanceId;
        request.processVersion = processVersion;
        request.data = body;

        log.info("Start the process {}", processInstanceId);
        sendMessage(ADDRESS_START_PROCESS, null, request);
        return processInstanceId;
    }

    protected void processServiceTask(ExecuteServiceTask execute) {
        ProcessToken token = receivedMessage(ADDRESS_SERVICE_TASK);

        log.info("Service task message {} ", token);

        ServiceTaskData serviceData = new ServiceTaskData();
        serviceData.data = Collections.unmodifiableMap(token.getData());
        serviceData.guid = token.getId();
        serviceData.name = token.getNodeName();
        serviceData.processId = token.getProcessId();
        serviceData.processVersion = token.getProcessVersion();

        // execute test
        Map<String, Object> data = execute.execute(serviceData);
        if (data != null) {
            token.getData().putAll(data);
        }
        sendMessage(ADDRESS_TOKEN_EXECUTE, this::setCorrelationId, token);
    }

    public static class ServiceTaskData {
        public String guid;
        public String name;
        public String processId;
        public String processVersion;
        public Map<String, Object> data;
    }

    protected  void setCorrelationId(Message m, ProcessToken t) {
        try {
            m.setJMSCorrelationID(t.getExecutionId());
        } catch (JMSException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public interface MessageBeforeSendListener<T> {

        void update(Message message, T item);
    }

    public interface ExecuteServiceTask {

        Map<String, Object> execute(ServiceTaskData data);
    }

}
