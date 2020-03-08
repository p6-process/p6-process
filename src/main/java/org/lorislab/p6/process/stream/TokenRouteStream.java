package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.lorislab.p6.process.stream.model.ProcessTokenResponseStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenRouteStream {

    @Incoming("process-token-route-in")
    @Outgoing("process-token-route-out")
    public KafkaMessage<String, ProcessTokenStream> routeToken(KafkaMessage<String, ProcessTokenStream> event) {
        ProcessTokenStream token = event.getPayload();
        if (token.status == ProcessTokenStatusStream.FINISHED) {
            return null;
        }
        ProcessTokenResponseStream response = token.type.response;
        return KafkaMessage.of(response.topic, event.getKey(), event.getPayload());
    }

}
