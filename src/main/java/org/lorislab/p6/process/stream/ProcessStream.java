package org.lorislab.p6.process.stream;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.mem.service.PersistenceInstanceService;
import org.lorislab.p6.process.service.DeploymentService;
import org.lorislab.p6.process.stream.model.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ProcessStream {

    @Inject
    Logger log;

    @Inject
    DeploymentService deploymentService;

    @Incoming("start-process-in")
    @Outgoing("process-instance-out")
    public ProcessInstance processStart(StartProcessRequest request) {

        ProcessDefinitionModel pdm = deploymentService.getProcessDefinition(request.processId, request.processVersion);

        if (pdm == null) {
            log.error("No process definition found for the {}/{}/{}", request.guid, request.processId, request.processVersion);
            return null;
        }

        List<Node> nodes = pdm.start;
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        ProcessInstance pi = new ProcessInstance();
        pi.guid = request.guid;
        pi.status = ProcessInstanceStatus.CREATED;
        pi.processId = request.processId;
        pi.processVersion = request.processVersion;
        pi.data = request.data;

         nodes.stream()
                .map(node -> {
                    ProcessTokenStream token = new ProcessTokenStream();
                    token.processId = request.processId;
                    token.processVersion = request.processVersion;
                    token.nodeName = node.name;
                    token.startNodeName = node.name;
                    token.createNodeName = node.name;
                    token.type = ProcessTokenTypeStream.valueOf(node);
                    token.status = ProcessTokenStatusStream.CREATED;
                    token.previousName = null;
                    token.processInstanceGuid = pi.guid;
                    token.data = request.data;
                    return token;
                }).forEach(pi.tokens::add);

        return pi;
    }


    @Incoming("process-instance-in")
    @Outgoing("process-token-route-out")
    public PublisherBuilder<ProcessTokenStream> processInstance(ProcessInstance pi) {
        if (pi == null) {
            return ReactiveStreams.empty();
        }
        if (pi.status == ProcessInstanceStatus.CREATED) {
            return ReactiveStreams.of(pi.tokens.toArray(new ProcessTokenStream[0]));
        }
        return ReactiveStreams.empty();
    }
}
