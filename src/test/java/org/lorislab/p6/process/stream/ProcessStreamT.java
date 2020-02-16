package org.lorislab.p6.process.stream;

import io.vertx.amqp.AmqpMessage;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Process stream tests")
public class ProcessStreamT extends AbstractTest {

    @Test
    @DisplayName("Start process test")
    public void startProcessTest() {
        String processId = "StartProcess";
        String processVersion = "1.0.0";
        // start process
        String processInstanceId = startProcess(processId, processVersion, "{\"key\":\"value\"}");
        // wait to finished process
        waitProcessFinished(processId, processInstanceId);
        // check the process parameters
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("guid", processInstanceId)
                .get("/v1/instance/{guid}/parameters")
                .prettyPeek()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("key", equalTo("value"));
    }

    @Test
    @DisplayName("Service task process test")
    public void serviceTaskProcessTest() {
        String processId = "ServiceTaskProcess";
        String processVersion = "1.0.0";
        // start process
        String processInstanceId = startProcess(processId, processVersion, "{\"key\":\"value\"}");

        // process service task
        processServiceTask(Map.of("key", "1234"));

        // wait to finished process
        waitProcessFinished(processId, processInstanceId);
        // check the process parameters
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("guid", processInstanceId)
                .get("/v1/instance/{guid}/parameters")
                .prettyPeek()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("key", equalTo("1234"));
    }

    @Test
    @DisplayName("Simple process test")
    public void simpleProcessTest() {
        String processId = "SimpleProcess";
        String processVersion = "1.0.0";
        // start process
        String processInstanceId = startProcess(processId, processVersion, "{\"key\":\"value1\"}");

        // process service1
        processServiceTask(Map.of("key", "step1"));
        // process service3
        processServiceTask(Map.of("key", "step3/4"));
        // process service4
        processServiceTask(Map.of("key", "step3/4"));
        // process service5
        processServiceTask(Map.of("key", "step5", "newKey", "newValue"));

        // wait to finished process
        waitProcessFinished(processId, processInstanceId);

        // check the process parameters
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("guid", processInstanceId)
                .get("/v1/instance/{guid}/parameters")
                .prettyPeek()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("key", equalTo("step5"));
    }

    private String startProcess(String processId, String processVersion, String body) {
        log.info("Test {}:{}", processId, processVersion);
        String processInstanceId = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("processId", processId);
        data.put("processInstanceId", processInstanceId);
        data.put("processVersion", processVersion);

        log.info("Start the process {}", processInstanceId);
        AmqpMessage startProcess = AmqpMessage.create()
                .applicationProperties(JsonObject.mapFrom(data))
                .withBody(body)
                .id(UUID.randomUUID().toString())
                .correlationId(processInstanceId)
                .build();
        sendMessage(ADDRESS_START_PROCESS, startProcess);
        return processInstanceId;
    }

    private void waitProcessFinished(String processId, String processInstanceId) {

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

    private void processServiceTask(Map<String, Object> data) {
        log.info("Execute process service task {} ", data);
        AmqpMessage message = receivedMessage(ADDRESS_SERVICE_TASK);
        String tmp = message.bodyAsString();
        log.info("Service task message {} - {}", message.applicationProperties(), tmp);
        JsonObject body = new JsonObject(tmp);
        body.getMap().putAll(data);

        AmqpMessage serviceTaskComplete = AmqpMessage.create()
                .applicationProperties(message.applicationProperties())
                .withBody(body.toString())
                .id(UUID.randomUUID().toString())
                .correlationId(message.correlationId())
                .build();

        sendMessage(ADDRESS_TOKEN_EXECUTE, serviceTaskComplete);
    }
}
