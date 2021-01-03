package org.lorislab.p6.process.pi;

import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageListener;
import org.lorislab.p6.process.message.Queues;
import org.lorislab.quarkus.log.cdi.LogService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class ProcessCommandMessageListener extends MessageListener {

    @Inject
    ProcessInstanceService processInstanceService;

    @Override
    protected String name() {
        return Queues.PROCESS_REQUEST;
    }

    @Override
    protected Uni<Long> onMessage(Transaction tx, Message message) {
        ProcessMessageHeader header = message.header(ProcessMessageHeader.class);

        // execute start process request
        if (header.command == ProcessMessageHeader.Command.START_PROCESS) {
            StartProcessCommand request = message.data(StartProcessCommand.class);
            return processInstanceService.createProcessInstance(tx, request)
                    .onItem().transform(pi -> message.id);
        }
        return Uni.createFrom().item(message.id);
    }

}
