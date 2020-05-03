package org.lorislab.p6.process.stream;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import org.lorislab.p6.process.dao.model.Message;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.rs.ProcessInstanceRestController;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
public class ProcessStream {

    @Inject
    Logger log;

    @Inject
    PgPool client;

    @Inject
    DeploymentService deploymentService;

    public Uni<ProcessInstance> startProcess(ProcessInstanceRestController.StartProcessRequest request) {
        return createProcessInstance(request).onItem().apply(pi ->
            client.begin().onItem()
                .apply(tx -> pi.create(tx).onItem().apply(id ->
                            Message.create(tx, id).onItem().apply(mi ->
                                    tx.commit().onItem().apply(x -> pi)
                            ).flatMap(x -> x)
                        ).flatMap(x -> x)
                ).flatMap(x -> x)
        ).flatMap(x -> x);
    }

    public Uni<ProcessInstance> createProcessInstance(ProcessInstanceRestController.StartProcessRequest request) {

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.id, request.processId, request.processVersion);
            return Uni.createFrom().nullItem();
        }

        ProcessInstance pi = new ProcessInstance();
        pi.id = UUID.randomUUID().toString();
        pi.status = ProcessInstanceStatus.CREATED;
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        if (request.data != null) {
            pi.data = new JsonObject(request.data);
        } else {
            pi.data = new JsonObject();
        }
        return Uni.createFrom().item(pi);
    }

//
//    @Inject
//    TokenExecutor executor;
//
//    @Incoming("process-start-in")
//    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
//    public CompletionStage<Void> processStart(IncomingJmsTxMessage<StartProcessRequest> message) {
//        try {
//            log.info("Start process {}", message.getPayload());
//            IncomingJmsMessageMetadata metadata = message.getJmsMetadata();
//            List<ProcessToken> tokens = executor.createTokens(metadata.getCorrelationId(), message.getPayload());
//            message.send(tokens.stream().map(TokenExecutor::createMessage));
//            return message.ack();
//        } catch (Exception wex) {
//            log.error("Error start process", wex);
//            log.error("Error process start message. Message {}", message);
//            return message.rollback();
//        }
//    }
//
//    @RegisterForReflection
//    public static class StartProcessRequest {
//        public String processId;
//        public String processInstanceId;
//        public String processVersion;
//        public Map<String, Object> data;
//    }
}
