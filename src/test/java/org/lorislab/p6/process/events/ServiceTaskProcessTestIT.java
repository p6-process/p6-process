package org.lorislab.p6.process.events;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.rs.StartProcessCommandDTO;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.HashMap;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.lorislab.p6.process.rs.Application.APPLICATION_JSON;

@NativeImageTest
public class ServiceTaskProcessTestIT extends ServiceTaskProcessTest {

}
