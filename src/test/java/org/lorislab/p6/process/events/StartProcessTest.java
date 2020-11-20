package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.rs.StartProcessCommandDTO;
import org.lorislab.p6.process.test.AbstractTest;

import java.util.UUID;

@QuarkusTest
@DisplayName("Start-end process test")
public class StartProcessTest extends AbstractTest {

    @Test
    public void startProcessTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.startEndProcess";
        r.processVersion = "1.0.0";
        r.data.put("key", UUID.randomUUID().toString());

        startProcess(r);
        ProcessInstance pi = waitProcessFinished(r.id);

        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals(r.data.get("key"), pi.data.getString("key"));
    }
}
