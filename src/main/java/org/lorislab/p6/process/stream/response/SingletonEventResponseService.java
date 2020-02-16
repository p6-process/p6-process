package org.lorislab.p6.process.stream.response;

import io.quarkus.arc.Unremovable;
import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.amqp.AmqpMessageBuilder;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Unremovable
@ApplicationScoped
@EventResponseServiceType(ProcessTokenResponse.SINGLETON)
public class SingletonEventResponseService implements EventResponseService {

    @Inject
    @Channel("token-execute-singleton-out")
    Emitter<AmqpMessage<String>> emitter;

    @Override
    public void response(ProcessToken token, String correlationId) {
        AmqpMessageBuilder output =  EventResponseService.createMessageBuilder(token, correlationId);
        emitter.send(new AmqpMessage<>(output.build()));
    }
}
