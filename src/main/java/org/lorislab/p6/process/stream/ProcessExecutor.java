package org.lorislab.p6.process.stream;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class ProcessExecutor {

    @ConfigProperty(name = "process.token.executor.scheduler", defaultValue = "2")
    Long scheduler;

    @Inject
    Vertx vertx;

    @Inject
    PgPool client;

    @Inject
    DeploymentService deploymentService;

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    MessageDAO messageDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    public Uni<Long> startTimer() {
        return Uni.createFrom().item(createTimer());
    }

    private Long createTimer() {
        return vertx.setTimer(TimeUnit.SECONDS.toMillis(scheduler), this::action);
    }

    void action(Long id) {
        log.info("Process timer execution {}", id);
        Uni.createFrom().nullItem()
                .onItem().produceUni(i -> execute())
                .repeat().until(m -> m.message == null)
                .onCompletion().invoke(this::createTimer)
                .subscribe().with(m -> log.debug("Execute timer" + m), Throwable::printStackTrace);
    }

    private Uni<MessageWrapper> execute() {
        return client.begin().flatMap(tx -> messageDAO.nextProcessMessage(tx)
                .onItem().apply(m -> {
                    if (m == null) {
                        tx.close();
                        return Uni.createFrom().item((Message) null);
                    }
                    return saveTokens(tx, m)
                            .onItem().apply(u -> tx.commit().onItem().apply(x -> u))
                            .flatMap(x -> x);
                }).flatMap(x -> x)
        ).onItem().apply(MessageWrapper::new);
    }

    public static class MessageWrapper {
        Message message;
        public MessageWrapper(Message message) {
            this.message = message;
        }
    }

    private Uni<Message> saveTokens(Transaction tx, Message m) {
        return createTokens(tx, m.ref).onItem()
                .apply(t -> processTokenDAO.create(tx, t).and(messageDAO.createMessages(tx, t)).map(x -> m))
                .flatMap(x -> x);
    }

    private Uni<List<ProcessToken>> createTokens(Transaction tx, String ref) {
        return processInstanceDAO.findById(tx, ref)
                .onItem().produceUni(pi -> {
                    ProcessDefinitionRuntime pdr = deploymentService.getProcessDefinition(pi.processId, pi.processVersion);
                    List<ProcessToken> items = pdr.startNodes.values().stream()
                            .map(node -> {
                                ProcessToken token = new ProcessToken();
                                token.id = UUID.randomUUID().toString();
                                token.processId = pi.processId;
                                token.processVersion = pi.processVersion;
                                token.processInstance = pi.id;
                                token.nodeName = node.name;
                                token.status = ProcessToken.Status.CREATED;
                                token.type = ProcessToken.Type.valueOf(node);
                                token.data = pi.data;
                                return token;
                            }).collect(Collectors.toList());
                    return Uni.createFrom().item(items);
                });
    }

}
