package org.lorislab.p6.process.rs;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.HashMap;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.lorislab.p6.process.rs.Application.APPLICATION_JSON;

@QuarkusTest
public class ProcessInstanceRestControllerTest extends AbstractTest {


//    @Test
//    public void exclusiveGatewayProcessTest() throws Exception {
//        StartProcessRequestDTO r = new StartProcessRequestDTO();
//        r.id = UUID.randomUUID().toString();
//        r.processId = "exclusiveGatewayProcess";
//        r.processVersion = "1.0.0";
//        r.data =  Map.of("count", 9, "result", true);
//
//        ProcessInstance pi = given()
//                .when()
//                .contentType(APPLICATION_JSON)
//                .body(r)
//                .post("/instances/")
//                .prettyPeek()
//                .then()
//                .statusCode(HttpResponseStatus.ACCEPTED.code())
//                .extract().body().as(ProcessInstance.class);
//
//        waitProcessFinished(pi.id);
//    }
//
//    @Test
//    public void exclusiveGatewayProcessTest2() throws Exception {
//        StartProcessRequestDTO r = new StartProcessRequestDTO();
//        r.id = UUID.randomUUID().toString();
//        r.processId = "exclusiveGatewayProcess";
//        r.processVersion = "1.0.0";
//        r.data =  Map.of("count", 20, "result", true);
//
//        ProcessInstance pi = given()
//                .when()
//                .contentType(APPLICATION_JSON)
//                .body(r)
//                .post("/instances/")
//                .prettyPeek()
//                .then()
//                .statusCode(HttpResponseStatus.ACCEPTED.code())
//                .extract().body().as(ProcessInstance.class);
//
//        waitProcessFinished(pi.id);
//    }
//
//    @Test
//    public void exclusiveGatewayProcessTest3() throws Exception {
//        StartProcessRequestDTO r = new StartProcessRequestDTO();
//        r.id = UUID.randomUUID().toString();
//        r.processId = "exclusiveGatewayProcess";
//        r.processVersion = "1.0.0";
//        r.data =  Map.of("count", 0, "result", false);
//
//        ProcessInstance pi = given()
//                .when()
//                .contentType(APPLICATION_JSON)
//                .body(r)
//                .post("/instances/")
//                .prettyPeek()
//                .then()
//                .statusCode(HttpResponseStatus.ACCEPTED.code())
//                .extract().body().as(ProcessInstance.class);
//
//        waitProcessFinished(pi.id);
//    }
//


}
