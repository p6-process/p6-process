package org.lorislab.p6.process.stream;

import io.quarkus.test.junit.NativeImageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@NativeImageTest
public class ProcessStreamTestIT extends ProcessStreamTest {

}
