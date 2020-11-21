package org.lorislab.p6.process.events;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.Queues;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.rs.StartProcessCommandDTO;
import org.lorislab.p6.process.test.AbstractTest;
import org.lorislab.p6.process.token.TokenMessageHeader;

import java.util.Map;
import java.util.UUID;

@QuarkusTest
@DisplayName("Service task process test")
public class ServiceTaskProcessTest extends AbstractTest {

    @Test
    public void serviceTaskProcessTest() {
        StartProcessCommandDTO r = new StartProcessCommandDTO();
        r.processId = "events.serviceTaskProcess";
        r.processVersion = "1.0.0";
        r.data.put("key", UUID.randomUUID().toString());

        startProcess(r);

        String value = UUID.randomUUID().toString();
        processServiceTask((h,d) -> Map.of("key1", value));
//        Message message = waitForNextMessage(Queues.SERVICE_TASK_REQUEST_QUEUE);
//        TokenMessageHeader header = message.header(TokenMessageHeader.class);
//
//        String value = UUID.randomUUID().toString();
//        message.data.put("key1", value);
//        message.queue = Queues.SERVICE_TASK_RESPONSE_QUEUE;
//        Long mi = sendMessage(message);
//        System.out.println("Response: " + mi);

        ProcessInstance pi = waitProcessFinished(r.id);

        Assertions.assertNotNull(pi);
        Assertions.assertNotNull(pi.data);
        Assertions.assertEquals(r.data.get("key"), pi.data.getString("key"));
        Assertions.assertEquals(value, pi.data.getString("key1"));
    }

}
