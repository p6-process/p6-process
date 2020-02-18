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

import io.restassured.RestAssured;
import io.vertx.amqp.*;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lorislab.jel.testcontainers.docker.DockerComposeService;
import org.lorislab.jel.testcontainers.docker.DockerTestEnvironment;
import org.lorislab.p6.process.flow.model.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.lorislab.jel.testcontainers.InjectLoggerExtension;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * The abstract test
 */
@ExtendWith(InjectLoggerExtension.class)
public abstract class AbstractTest {

    public static DockerTestEnvironment ENVIRONMENT = new DockerTestEnvironment();

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

        ENVIRONMENT.start();
        DockerComposeService testService = ENVIRONMENT.getService("p6-executor");
        if (testService != null) {
            RestAssured.port = testService.getPort(8080);
        }
    }

    protected AmqpMessage receivedMessage(String address) {
        return receivedMessage(address, 5);
    }

    protected AmqpMessage receivedMessage(String address, long timeout) {

        AmqpClient client = null;
        try {
            client = AmqpClient.create(new AmqpClientOptions());

            CompletableFuture<AmqpReceiver> rf = new CompletableFuture<>();
            client.createReceiver(address, x -> rf.complete(x.result()));
            AmqpReceiver receiver = rf.get(timeout, TimeUnit.SECONDS);

            CompletableFuture<AmqpMessage> message = new CompletableFuture<>();
            receiver.handler(message::complete);
            return message.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            throw new IllegalStateException("Error received message", ex);
        } finally {
            close(client);
        }
    }

    protected void sendMessage(String address, AmqpMessage... messages) {
        AmqpClient client = null;
        try {
            client = AmqpClient.create(new AmqpClientOptions());

            CompletableFuture<AmqpSender> senderFuture = new CompletableFuture<>();
            client.createSender(address, x -> senderFuture.complete(x.result()));
            AmqpSender sender = senderFuture.get(5, TimeUnit.SECONDS);

            for (AmqpMessage msg : messages) {
                CompletableFuture<Void> message = new CompletableFuture<>();
                sender.sendWithAck(msg, a -> message.complete(a.result()));
                message.get(5, TimeUnit.SECONDS);
            };

        } catch (Exception ex) {
            throw new IllegalStateException("Error send message", ex);
        } finally {
            close(client);
        }
    }

    private void close(AmqpClient client) {
        if (client == null) {
            return;
        }
        try {
            CompletableFuture<Void> close = new CompletableFuture<>();
            client.close(c -> {
                close.complete(c.result());
            });
            close.get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Error send message", ex);
        }
    }

    protected String loadResource(String name) {
        try {
            return Files.readString(
                    Paths.get(AbstractTest.class.getResource(name).toURI()));
        } catch (URISyntaxException | IOException ux) {
            throw new RuntimeException(ux);
        }
    }

    protected void waitProcessFinished(String processId, String processInstanceId) {

        log.info("Wait for the process {} to finished execution guid {} ", processId, processInstanceId);
        await()
                .atMost(5, SECONDS)
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

        Map<String, Object> data = new HashMap<>();
        data.put("processId", processId);
        data.put("processInstanceId", processInstanceId);
        data.put("processVersion", processVersion);

        log.info("Start the process {}", processInstanceId);
        AmqpMessage startProcess = AmqpMessage.create()
                .applicationProperties(JsonObject.mapFrom(data))
                .withBody(JsonObject.mapFrom(body).toString())
                .id(UUID.randomUUID().toString())
                .correlationId(processInstanceId)
                .build();
        sendMessage(ADDRESS_START_PROCESS, startProcess);
        return processInstanceId;
    }

    protected void processServiceTask(ExecuteServiceTask execute) {
        AmqpMessage message = receivedMessage(ADDRESS_SERVICE_TASK);
        String tmp = message.bodyAsString();
        log.info("Service task message {} - {}", message.applicationProperties(), tmp);

        JsonObject json = message.applicationProperties();
        JsonObject body = new JsonObject(tmp);

        ServiceTaskData serviceData = new ServiceTaskData();
        serviceData.data = Collections.unmodifiableMap(body.getMap());
        serviceData.guid = json.getString("guid");
        serviceData.name = json.getString("name");
        serviceData.processId = json.getString("processId");
        serviceData.processVersion = json.getString("processVersion");

        // execute test
        Map<String, Object> data = execute.execute(serviceData);
        if (data != null) {
            body.getMap().putAll(data);
        }

        // send response message
        AmqpMessage serviceTaskComplete = AmqpMessage.create()
                .applicationProperties(message.applicationProperties())
                .withBody(body.toString())
                .id(UUID.randomUUID().toString())
                .correlationId(message.correlationId())
                .build();

        sendMessage(ADDRESS_TOKEN_EXECUTE, serviceTaskComplete);
    }

    public static class ServiceTaskData {
        public String guid;
        public String name;
        public String processId;
        public String processVersion;
        public Map<String, Object> data;
    }

    public interface ExecuteServiceTask {

        Map<String, Object> execute(ServiceTaskData data);
    }

}
