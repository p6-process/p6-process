package org.lorislab.p6.process.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lorislab.quarkus.testcontainers.InjectLoggerExtension;

import java.util.HashMap;
import java.util.Map;

@DisplayName("Expression tests")
@ExtendWith(InjectLoggerExtension.class)
public class ProcessExpressionHelperTest {

    @Test
    @DisplayName("If expression test")
    public void booleanExpressionTest() {

        Map<String, Object> child = new HashMap<>();
        child.put("name", "world");
        child.put("result", Boolean.TRUE);
        child.put("count", 10);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "world");
        data.put("test", child);
        data.put("result", Boolean.TRUE);
        Assertions.assertTrue(ProcessExpressionHelper.ifExpression("test.count == 10 && test.result", data));
        Assertions.assertFalse(ProcessExpressionHelper.ifExpression("test.count != 10", data));
        Assertions.assertFalse(ProcessExpressionHelper.ifExpression("test.count == world", data));
    }

    @Test
    @DisplayName("Value expression test")
    public void expressionTest() {

        Map<String, Object> child = new HashMap<>();
        child.put("name", "world");
        child.put("result", Boolean.TRUE);
        child.put("count", 10);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "world");
        data.put("test", child);
        data.put("result", Boolean.TRUE);

        Assertions.assertEquals(10, ProcessExpressionHelper.valueExpression("test.count", data));
        Assertions.assertEquals(true, ProcessExpressionHelper.valueExpression("test.result", data));
        Assertions.assertEquals("world", ProcessExpressionHelper.valueExpression("test.name", data));
    }
}
