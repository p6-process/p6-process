package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.rs.StartProcessCommandDTO;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.Map;
import java.util.UUID;

@QuarkusTest
@DisplayName("Process exclusive gateway tests")
public class ExclusiveGatewayTokenServiceTest extends AbstractTest {

    @Test
    @DisplayName("Exclusive gateway process test first option")
    public void exclusiveGatewayProcessFirstTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.exclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 9, "result", true));
        startProcess(r);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertEquals("service1", h.nodeName);
            return Map.of("step", "test1");
        });

        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("test1", pi.data.getString("step"));
    }

    @Test
    @DisplayName("Exclusive gateway process test second option")
    public void exclusiveGatewayProcessSecondTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.exclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 20, "result", true));
        startProcess(r);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertEquals("service2", h.nodeName);
            return Map.of("step", "test2");
        });

        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("test2", pi.data.getString("step"));
    }

    @Test
    @DisplayName("Exclusive gateway process test default option")
    public void exclusiveGatewayProcessDefaultTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.exclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 0, "result", false));
        startProcess(r);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertEquals("service2", h.nodeName);
            return Map.of("step", "default");
        });

        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("default", pi.data.getString("step"));
    }
}
