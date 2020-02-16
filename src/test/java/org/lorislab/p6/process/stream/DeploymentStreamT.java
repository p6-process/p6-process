package org.lorislab.p6.process.stream;

import io.vertx.amqp.AmqpMessage;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class DeploymentStreamT extends AbstractTest {

    @Test
    public void deploymentTest() throws Exception {
        String deploymentId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();

        log.info("Start the deployment {} with message {}", deploymentId, messageId);
        AmqpMessage msg = AmqpMessage
                .create()
                .withBody(loadResource("/test/DeploymentProcess.json"))
                .id(messageId)
                .correlationId(deploymentId)
                .build();
        sendMessage(ADDRESS_DEPLOYMENT, msg);

        log.info("Wait for the definition to be stored in the database with guid {} ", deploymentId);
        await()
            .atMost(5, SECONDS)
            .untilAsserted(() -> given()
                    .when()
                        .contentType(MediaType.APPLICATION_JSON)
                        .pathParam("guid", deploymentId)
                        .get("/v1/definition/{guid}")
                    .prettyPeek()
                        .then()
                    .statusCode(Response.Status.OK.getStatusCode()));
    }
}
