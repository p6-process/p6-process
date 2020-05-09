package org.lorislab.p6.process.stream;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.rs.StartProcessRequestDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ProcessService {

    @Inject
    DeploymentService deploymentService;

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    MessageDAO messageDAO;

    @Inject
    PgPool client;

    public Uni<ProcessInstance> startProcess(StartProcessRequestDTO request) {
        ProcessInstance pi = createProcessInstance(request);
        if (pi == null) return Uni.createFrom().nullItem();
        return client.begin()
                .flatMap(tx -> processInstanceDAO.create(tx, pi).and(messageDAO.createMessage(tx, pi))
                                .onItem().ignore().andContinueWithNull()
                                .onItem().produceUni(x -> tx.commit())
                                .onFailure().recoverWithUni(tx::rollback)
                ).map(x -> pi);
    }

    private ProcessInstance createProcessInstance(StartProcessRequestDTO request) {

        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.id, request.processId, request.processVersion);
            return null;
        }

        ProcessInstance pi = new ProcessInstance();
        pi.id = UUID.randomUUID().toString();
        pi.status = ProcessInstance.Status.CREATED;
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        if (request.data != null) {
            pi.data = new JsonObject(request.data);
        } else {
            pi.data = new JsonObject();
        }
        log.info("Create ProcessInstance {}", pi);
        return pi;
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
