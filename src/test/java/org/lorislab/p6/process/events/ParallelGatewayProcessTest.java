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
@DisplayName("Parallel gateway process test")
public class ParallelGatewayProcessTest extends AbstractTest {

    @Test
    public void parallelGatewayProcessTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.parallelGateway";
        r.processVersion = "1.0.0";
        r.data.put("key", UUID.randomUUID().toString());

        startProcess(r);

        // process service1
        processServiceTask((h,d) -> Map.of("key", "step1", "service1", "1"));
        // process service3
        processServiceTask((h,d) -> Map.of("key", "step3/4", "service3", "1"));
        // process service4
        processServiceTask((h,d) -> Map.of("key", "step3/4", "service4", "1"));
        // process service5
        processServiceTask((h,d) -> Map.of("key", "step5", "service5", "1"));

        ProcessInstance pi = waitProcessFinished(r.id);

        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals("step5", pi.data.getString("key"));
        Assertions.assertEquals("1", pi.data.getString("service1"));
        Assertions.assertEquals("1", pi.data.getString("service3"));
        Assertions.assertEquals("1", pi.data.getString("service4"));
        Assertions.assertEquals("1", pi.data.getString("service5"));
    }
}
