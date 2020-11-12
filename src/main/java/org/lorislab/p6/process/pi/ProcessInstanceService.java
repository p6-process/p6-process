package org.lorislab.p6.process.pi;

import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;

import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.deployment.DeploymentService;
import org.lorislab.p6.process.message.Message;
import org.lorislab.p6.process.message.MessageBuilder;
import org.lorislab.p6.process.message.MessageProducer;
import org.lorislab.p6.process.message.Queues;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.token.TokenMessageHeader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class ProcessInstanceService {

    @Inject
    DeploymentService deploymentService;

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    MessageProducer messageProducer;

    public Uni<ProcessInstance> createProcessInstance(Transaction tx, StartProcessRequest request) {
        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(request.processId, request.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", request.id, request.processId, request.processVersion);
            return Uni.createFrom().nullItem();
        }

        // create process instance
        ProcessInstance pi = create(request);

        // create tokens for the start node
        List<ProcessToken> tokens = pd.startNodes.values().stream().map(node -> createToken(pi, node)).collect(Collectors.toList());

        // create messages for the tokens
        List<Message> messages = tokens.stream().map(ProcessInstanceService::createMessage).collect(Collectors.toList());

        // save to database
        return Uni.combine().all()
                .unis(
                    processInstanceDAO.create(tx, pi),
                    processTokenDAO.create(tx, tokens),
                    messageProducer.send(tx, Queues.TOKEN_EXECUTE_QUEUE, messages)
                )
                .combinedWith(x -> pi);
    }

    private static ProcessInstance create(StartProcessRequest request) {
        ProcessInstance pi = new ProcessInstance();
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        if (request.data != null) {
            pi.data = request.data;
        }
        return pi;
    }

    private static ProcessToken createToken(ProcessInstance pi, Node node) {
        ProcessToken token = new ProcessToken();
        token.processId = pi.processId;
        token.processVersion = pi.processVersion;
        token.processInstance = pi.id;
        token.nodeName = node.name;
        token.type = ProcessToken.Type.valueOf(node);
        token.data = pi.data;
        return token;
    }

    private static Message createMessage(ProcessToken token) {
        TokenMessageHeader header = new TokenMessageHeader();
        header.tokenId = token.id;
        return MessageBuilder.builder()
                .header(header)
                .build();
    }
}
