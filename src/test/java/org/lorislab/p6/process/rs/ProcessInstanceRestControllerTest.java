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
import io.vertx.core.json.JsonObject;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.HashMap;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProcessInstanceRestControllerTest extends AbstractTest {

    @Test
    public void getNotFoundTest() {
        given()
                .when()
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .pathParam("guid", "123456")
                .get("/instance/{guid}")
                .prettyPeek()
                .then()
                .statusCode(HttpResponseStatus.NOT_FOUND.code());
    }

    @Test
    public void startProcessTest() {
        StartProcessRequestDTO r = new StartProcessRequestDTO();
        r.id = UUID.randomUUID().toString();
        r.processId = "startEndProcess";
        r.processVersion = "1.2.3";
        r.data = new HashMap<>();

        given()
                .when()
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .body(r)
                .post("/instance/")
                .prettyPeek()
                .then()
                .statusCode(HttpResponseStatus.OK.code());
    }

}
