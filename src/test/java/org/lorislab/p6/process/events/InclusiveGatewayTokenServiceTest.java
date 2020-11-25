package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.rs.StartProcessCommandDTO;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
@DisplayName("Process inclusive gateway tests")
public class InclusiveGatewayTokenServiceTest extends AbstractTest {

    @Test
    @DisplayName("Exclusive gateway process test all options")
    public void exclusiveGatewayProcessFirstTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.inclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 9, "result", true));
        startProcess(r);

        Map<String, Integer> options = new HashMap<>();
        options.put("service1", 0);
        options.put("service2", 0);
        options.put("service3", 0);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step1", "test1");
        });

        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step2", "test2");
        });

        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step3", "test3");
        });

        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("test1", pi.data.getString("step1"));
        Assertions.assertEquals("test2", pi.data.getString("step2"));
        Assertions.assertEquals("test3", pi.data.getString("step3"));
        Assertions.assertEquals(1, options.get("service1"));
        Assertions.assertEquals(1, options.get("service2"));
        Assertions.assertEquals(1, options.get("service3"));
    }

    @Test
    @DisplayName("Inclusive gateway process test two options")
    public void exclusiveGatewayProcessSecondTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.inclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 20, "result", true));
        startProcess(r);

        Map<String, Integer> options = new HashMap<>();
        options.put("service1", 0);
        options.put("service2", 0);
        options.put("service3", 0);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step2", "test2");
        });

        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step3", "test3");
        });

        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("test2", pi.data.getString("step2"));
        Assertions.assertEquals("test3", pi.data.getString("step3"));
        Assertions.assertNull(pi.data.getString("step1"));
        Assertions.assertEquals(0, options.get("service1"));
        Assertions.assertEquals(1, options.get("service2"));
        Assertions.assertEquals(1, options.get("service3"));
    }

    @Test
    @DisplayName("Exclusive gateway process test default option")
    public void exclusiveGatewayProcessDefaultTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.exclusiveGatewayProcess";
        r.processVersion = "1.0.0";
        r.data.putAll(Map.of("count", 0, "result", false));
        startProcess(r);

        Map<String, Integer> options = new HashMap<>();
        options.put("service1", 0);
        options.put("service2", 0);
        options.put("service3", 0);

        // process test1
        processServiceTask((h,d) -> {
            Assertions.assertTrue(options.containsKey(h.nodeName));
            options.put(h.nodeName, options.get(h.nodeName) + 1);
            return Map.of("step1", "test1");
        });


        // wait to finished process
        ProcessInstance pi = waitProcessFinished(r.id);
        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("test1", pi.data.getString("step1"));
        Assertions.assertNull(pi.data.getString("step2"));
        Assertions.assertNull(pi.data.getString("step3"));
        Assertions.assertEquals(0, options.get("service1"));
        Assertions.assertEquals(1, options.get("service2"));
        Assertions.assertEquals(0, options.get("service3"));
    }
}
