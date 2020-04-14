package org.lorislab.p6.process.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Process stream tests")
public class ProcessStreamT extends AbstractTest {

    @Test
    @DisplayName("Start-end process test")
    public void startProcessTest() {
        String processId = "startEndProcess";
        String processVersion = "1.2.3";
        // start process
        String processInstanceId = startProcess(processId, processVersion, Map.of("key","value"));
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
        String processId = "serviceTaskProcess";
        String processVersion = "1.0.0";
        // start process
        String processInstanceId = startProcess(processId, processVersion, Map.of("key","value"));

        // process service task
        processServiceTask(x -> Map.of("key", "1234"));

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
    @DisplayName("Parallel gateway process test")
    public void simpleProcessTest() {
        String processId = "parallelGateway";
        String processVersion = "1.0.0";
        // start process
        String processInstanceId = startProcess(processId, processVersion, Map.of("key","value1"));

        // process service1
        processServiceTask(x -> Map.of("key", "step1"));
        // process service3
        processServiceTask(x -> Map.of("key", "step3/4"));
        // process service4
        processServiceTask(x -> Map.of("key", "step3/4"));
        // process service5
        processServiceTask(x -> Map.of("key", "step5", "newKey", "newValue"));

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

}
