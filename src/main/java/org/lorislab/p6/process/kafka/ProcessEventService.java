package org.lorislab.p6.process.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessEventService {

    @Incoming("event-in")
    @Outgoing("event-out")
    public KafkaMessage<String, ProcessEvent> execute(KafkaMessage<String, ProcessEvent> event) {
        return null;
    }
}
