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
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ProcessStream {

    public Uni<ProcessInstance> startProcess(PgPool client, StartProcessRequestDTO request) {
        return createProcessInstance(request).flatMap(pi -> client.begin()
                .flatMap(tx -> ProcessInstanceDAO.create(tx, pi).onItem().apply(id ->
                        MessageDAO.create(tx, id).onItem().apply(mi -> tx.commit().onItem().apply(x -> pi)
                        ).flatMap(x -> x)
                    ).flatMap(x -> x)
            ));
    }

    public Uni<ProcessInstance> createProcessInstance(StartProcessRequestDTO request) {

        ProcessDefinitionRuntime pd = DeploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.id, request.processId, request.processVersion);
            return Uni.createFrom().nullItem();
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
