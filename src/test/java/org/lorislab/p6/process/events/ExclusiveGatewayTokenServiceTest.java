package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.Map;

@QuarkusTest
@DisplayName("Process stream tests")
public class ExclusiveGatewayTokenServiceTest extends AbstractTest {
//
//    @Test
//    @DisplayName("Exclusive gateway process test first option")
//    public void exclusiveGatewayProcessFirstTest() {
//        String processId = "exclusiveGatewayProcess";
//        String processVersion = "1.0.0";
//
//        // start process
//        String processInstanceId = startProcess(processId, processVersion, Map.of("count", 9, "result", true));
//
//        // process test1
//        processServiceTask(x -> {
//            Assertions.assertEquals("service1", x.name);
//            return Map.of("step", "test1");
//        });
//
//        // wait to finished process
//        waitProcessFinished(processId, processInstanceId);
//    }
//
//    @Test
//    @DisplayName("Exclusive gateway process test second option")
//    public void exclusiveGatewayProcessSecondTest() {
//        String processId = "exclusiveGatewayProcess";
//        String processVersion = "1.0.0";
//
//        // start process
//        String processInstanceId = startProcess(processId, processVersion, Map.of("count", 20, "result", true));
//
//        // process test1
//        processServiceTask(x -> {
//            Assertions.assertEquals("service2", x.name);
//            return Map.of("step", "test2");
//        });
//
//        // wait to finished process
//        waitProcessFinished(processId, processInstanceId);
//    }
//
//    @Test
//    @DisplayName("Exclusive gateway process test default option")
//    public void exclusiveGatewayProcessDefaultTest() {
//        String processId = "exclusiveGatewayProcess";
//        String processVersion = "1.0.0";
//
//        // start process
//        String processInstanceId = startProcess(processId, processVersion, Map.of("count", 0, "result", false));
//
//        // process test1
//        processServiceTask(x -> {
//            Assertions.assertEquals("service2", x.name);
//            return Map.of("step", "default");
//        });
//
//        // wait to finished process
//        waitProcessFinished(processId, processInstanceId);
//    }
}
