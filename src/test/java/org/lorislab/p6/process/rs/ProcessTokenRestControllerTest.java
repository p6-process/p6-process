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
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProcessTokenRestControllerTest extends AbstractTest {

//    @Test
//    public void getNotFoundTest() {
//        given()
//                .when()
//                .contentType(ContentType.APPLICATION_JSON.getMimeType())
//                .pathParam("id", "123456")
//                .get("/tokens/{id}")
//                .prettyPeek()
//                .then()
//                .statusCode(HttpResponseStatus.NOT_FOUND.code());
//    }

}
