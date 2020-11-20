package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@DisplayName("Process stream tests")
public class ProcessStreamTest extends AbstractTest {

//    @Test
//    @DisplayName("Service task process test")
//    public void serviceTaskProcessTest() {
//        String processId = "serviceTaskProcess";
//        String processVersion = "1.0.0";
//        // start process
//        String processInstanceId = startProcess(processId, processVersion, Map.of("key","value"));
//
//        // process service task
//        processServiceTask(x -> Map.of("key", "1234"));
//
//        // wait to finished process
//        waitProcessFinished(processId, processInstanceId);
//        // check the process parameters
//        given()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON)
//                .pathParam("guid", processInstanceId)
//                .get("/v1/instance/{guid}/parameters")
//                .prettyPeek()
//                .then()
//                .statusCode(Response.Status.OK.getStatusCode())
//                .body("key", equalTo("1234"));
//    }
//
//    @Test
//    @DisplayName("Parallel gateway process test")
//    public void simpleProcessTest() {
//        String processId = "parallelGateway";
//        String processVersion = "1.0.0";
//        // start process
//        String processInstanceId = startProcess(processId, processVersion, Map.of("key","value1"));
//
//        // process service1
//        processServiceTask(x -> Map.of("key", "step1"));
//        // process service3
//        processServiceTask(x -> Map.of("key", "step3/4"));
//        // process service4
//        processServiceTask(x -> Map.of("key", "step3/4"));
//        // process service5
//        processServiceTask(x -> Map.of("key", "step5", "newKey", "newValue"));
//
//        // wait to finished process
//        waitProcessFinished(processId, processInstanceId);
//
//        // check the process parameters
//        given()
//                .when()
//                .contentType(MediaType.APPLICATION_JSON)
//                .pathParam("guid", processInstanceId)
//                .get("/v1/instance/{guid}/parameters")
//                .prettyPeek()
//                .then()
//                .statusCode(Response.Status.OK.getStatusCode())
//                .body("key", equalTo("step5"));
//    }

}
