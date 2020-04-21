package org.lorislab.p6.process.stream;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.reactive.messaging.jms.IncomingJmsMessageMetadata;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.quarkus.reactive.jms.tx.IncomingJmsTxMessage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ProcessStream {

    @Inject
    Logger log;

    @Inject
    TokenExecutor executor;

    @Incoming("process-start-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> processStart(IncomingJmsTxMessage<StartProcessRequest> message) {
        try {
            log.info("Start process {}", message.getPayload());
            IncomingJmsMessageMetadata metadata = message.getJmsMetadata();
            List<ProcessToken> tokens = executor.createTokens(metadata.getCorrelationId(), message.getPayload());
            message.send(tokens.stream().map(TokenExecutor::createMessage));
            return message.ack();
        } catch (Exception wex) {
            log.error("Error start process", wex);
            log.error("Error process start message. Message {}", message);
            return message.rollback();
        }
    }

    @RegisterForReflection
    public static class StartProcessRequest {
        public String processId;
        public String processInstanceId;
        public String processVersion;
        public Map<String, Object> data;
    }
}
