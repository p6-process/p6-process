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

package org.lorislab.p6.process.rs;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.mapper.ObjectMapperType;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.lorislab.p6.process.rs.Application.APPLICATION_JSON;

@QuarkusTest
public class ProcessInstanceRestControllerTest extends AbstractTest {

    @Test
    public void test() {
        System.out.println("###############TEST##############");
    }
//    @Test
//    public void getNotFoundTest() {
//        given()
//                .when()
//                .contentType(APPLICATION_JSON)
//                .pathParam("id", "123456")
//                .get("/instances/{id}")
//                .prettyPeek()
//                .then()
//                .statusCode(HttpResponseStatus.NOT_FOUND.code());
//    }
//
//    @Test
//    public void startProcessTest() throws Exception {
//        StartProcessRequestDTO r = new StartProcessRequestDTO();
//        r.id = UUID.randomUUID().toString();
//        r.processId = "startEndProcess";
//        r.processVersion = "1.2.3";
//        r.data = new HashMap<>();
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
//    public void parallelGatewayProcessTest() throws Exception {
//        StartProcessRequestDTO r = new StartProcessRequestDTO();
//        r.id = UUID.randomUUID().toString();
//        r.processId = "parallelGateway";
//        r.processVersion = "1.0.0";
//        r.data = new HashMap<>();
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
//    protected void waitProcessFinished(String pi) {
//
//        log.info("Wait for the process instance '{}; to finished", pi);
//        await()
//                .atMost(5, SECONDS)
//                .untilAsserted(() -> given()
//                        .when()
//                        .contentType(APPLICATION_JSON)
//                        .pathParam("id", pi)
//                        .get("/instances/{id}")
//                        .then()
//                        .statusCode(HttpResponseStatus.OK.code())
//                        .body("status", equalTo("FINISHED"))
//                );
//    }

}
