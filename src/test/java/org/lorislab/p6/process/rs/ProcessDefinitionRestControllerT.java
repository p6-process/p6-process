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

import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;


public class ProcessDefinitionRestControllerT extends AbstractTest {

    @Test
    public void getNotFoundTest() {
        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .pathParam("guid", "123456")
                .get("/v1/definition/{guid}")
                .prettyPeek()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

}
