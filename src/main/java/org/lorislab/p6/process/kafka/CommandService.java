package org.lorislab.p6.process.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class CommandService {

    @Incoming("command-in")
    @Outgoing("command-out")
    public KafkaMessage<String, ProcessEvent> execute(KafkaMessage<String, CommandRequest> event) {
        CommandRequest command = event.getPayload();
        CommandType type = command.type;
        if (type == CommandType.START_PROCESS) {

            ProcessEvent pe = new ProcessEvent();
            pe.type = ProcessEventType.PROCESS_INSTANCE_CREATED;
            pe.processInstanceId = command.processInstanceId;
            if (pe.processInstanceId == null) {
                pe.processInstanceId = UUID.randomUUID().toString();
            }
            pe.processName = command.processName;
            pe.processVersion = command.processVersion;
            pe.data = command.data;
            return KafkaMessage.of(event.getKey(), pe);
        }
        return null;
    }
}
