package org.lorislab.p6.process.pi;

import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.sqlclient.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessInstanceRepository;
import org.lorislab.p6.process.model.ProcessTokenRepository;

import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
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
    ProcessInstanceRepository processInstanceRepository;

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Inject
    MessageProducer messageProducer;

    public Uni<ProcessInstance> createProcessInstance(Transaction tx, StartProcessCommand task) {
        ProcessDefinitionRuntime pd = deploymentService.getProcessDefinition(task.processId, task.processVersion);
        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", task.id, task.processId, task.processVersion);
            return Uni.createFrom().nullItem();
        }

        // create process instance
        ProcessInstance pi = create(task);

        // create tokens for the start node
        List<ProcessToken> tokens = pd.startNodes.values().stream().map(node -> createToken(pi, node)).collect(Collectors.toList());

        // create messages for the tokens
        List<Message> messages = tokens.stream().map(ProcessInstanceService::createMessage).collect(Collectors.toList());

        // save to database
        return Uni.combine().all()
                .unis(
                    processInstanceRepository.create(tx, pi),
                    processTokenRepository.create(tx, tokens),
                    messageProducer.send(tx, Queues.TOKEN_EXECUTE_QUEUE, messages)
                )
                .combinedWith(x -> pi);
    }

    private static ProcessInstance create(StartProcessCommand cmd) {
        ProcessInstance pi = new ProcessInstance();
        pi.processId = cmd.processId;
        pi.processVersion = cmd.processVersion;
        pi.cmdId = cmd.id;
        if (cmd.data != null) {
            pi.data = cmd.data;
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
