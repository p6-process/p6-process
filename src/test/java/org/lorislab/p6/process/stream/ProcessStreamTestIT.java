package org.lorislab.p6.process.stream;

import io.quarkus.test.junit.NativeImageTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@NativeImageTest
public class ProcessStreamTestIT extends ProcessStreamTest {

}
