package org.lorislab.p6.process.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.lorislab.jel.testcontainers.InjectLoggerExtension;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Random;
import java.util.stream.Stream;

@DisplayName("Data utility tests")
@ExtendWith(InjectLoggerExtension.class)
public class DataUtilTest {

    @Inject
    Logger log;


    @DisplayName("Convert string to bytes test")
    @ParameterizedTest
    @MethodSource("createStringInputs")
    public void stringTest() {
        String input = generateString(100);
        log.info("Test string: {}" ,input);
        String output = DataUtil.byteToString(DataUtil.stringToByte(input));
        Assertions.assertEquals(output, input);
    }

    private static Stream<String> createStringInputs() {
        return Stream.of(
                generateString(40),
                generateString(10),
                generateString(60),
                generateString(100)
        );
    }

    private static String generateString(int length) {
        int leftLimit = 10;
        int rightLimit = 255;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
