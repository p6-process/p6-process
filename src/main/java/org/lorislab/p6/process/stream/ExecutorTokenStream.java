package org.lorislab.p6.process.stream;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ExecutorTokenStream {

    @Inject
    ExecutorTokenService executorTokenService;

    @Incoming("process-token-in")
    @Outgoing("process-token-route-out")
    public PublisherBuilder<ProcessTokenStream> message(ProcessTokenStream token) {
        return execute(token);
    }

    @Incoming("process-token-singleton-in")
    @Outgoing("process-token-route-out")
    public PublisherBuilder<ProcessTokenStream> singleton(ProcessTokenStream token) {
        return execute(token);
    }

    private PublisherBuilder<ProcessTokenStream> execute(ProcessTokenStream token) {
        List<ProcessTokenStream> tokens = executorTokenService.executeToken(token);
        if (tokens == null) {
            return ReactiveStreams.empty();
        }
        return ReactiveStreams.of(tokens.toArray(new ProcessTokenStream[0]));

    }

}
