package org.lorislab.p6.process.stream;

import io.smallrye.reactive.messaging.jms.IncomingJmsMessageMetadata;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.quarkus.reactive.jms.tx.IncomingJmsTxMessage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TokenStream {

    @Inject
    Logger log;

    @Inject
    TokenExecutor executor;

    @Incoming("token-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> message(IncomingJmsTxMessage<ProcessToken> message) {
        return execute(message);
    }

    @Incoming("token-singleton-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> singleton(IncomingJmsTxMessage<ProcessToken> message) {
        return execute(message);
    }

    private CompletionStage<Void> execute(IncomingJmsTxMessage<ProcessToken> message) {
        try {
            IncomingJmsMessageMetadata metadata = message.getJmsMetadata();
            List<ProcessToken> tokens = executor.executeToken(metadata.getCorrelationId(), message.getPayload());
            message.send(tokens.stream().map(TokenExecutor::createMessage));
            return message.ack();
        } catch (Exception wex) {
            log.error("Error token message. Message {}", message);
            log.error("Error token message.", wex);
            return message.rollback();
        }
    }


}
