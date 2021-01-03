package org.lorislab.p6.process.token;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessTokenRepository;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageListener;
import org.lorislab.p6.process.message.Queues;
import org.lorislab.quarkus.log.cdi.LogService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class TokenExecutionMessageListener extends MessageListener {

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Inject
    ProcessTokenService processTokenService;

    @Override
    protected String name() {
        return Queues.TOKEN_EXECUTE_QUEUE;
    }

    @Override
    protected Uni<Long> onMessage(Transaction tx, Message message) {
        TokenMessageHeader header = message.header(TokenMessageHeader.class);
        return processTokenRepository.findById(tx, header.tokenId)
                .onItem().transformToUni(token -> processTokenService.executeToken(tx, token, message.id));
    }

}
