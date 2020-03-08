package org.lorislab.p6.process.stream;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.service.DeploymentService;
import org.lorislab.p6.process.stream.events.EventService;
import org.lorislab.p6.process.stream.events.EventServiceType;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ExecutorTokenService {

    @Inject
    Logger log;

    @Inject
    DeploymentService deploymentService;

    public List<ProcessTokenStream> executeToken(ProcessTokenStream token) {

        ProcessDefinitionModel pd = deploymentService.getProcessDefinition(token.processId, token.processVersion);
        if (pd == null) {
            log.warn("No process definition found for the token: {}", token);
            return Collections.emptyList();
        }

        Node node = pd.get(token.nodeName);
        if (node == null) {
            log.error("No node found in the process definition. The task will be ignored. Token: {}", token);
            return Collections.emptyList();
        }


        int nc = token.type.nextNodeCount;
        int size = 0;
        List<String> next = null;
        if (node.sequence != null && node.sequence.to != null) {
            next = node.sequence.to;
            size = next.size();
        }

        // next > 0
        if (nc == -1) {
            if (size == 0) {
                log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} > 0.", token.type, token.nodeName, size);
                return Collections.emptyList();
            }
        } else if (size != nc) {
            log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} == {}.", token.type, token.nodeName, nc, size);
            return Collections.emptyList();
        }

        log.info("Execute node: {} Next: {}", node.name, next);
        ProcessTokenTypeStream type = token.type;
        InstanceHandle<EventService> w = Arc.container().instance(EventService.class, EventServiceType.Literal.create(type));
        if (!w.isAvailable()) {
            throw new UnsupportedOperationException("Not supported token type '" + type + "'");
        }
        return w.get().execute(token, pd, node);
    }

}
