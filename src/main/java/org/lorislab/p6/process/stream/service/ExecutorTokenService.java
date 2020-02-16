package org.lorislab.p6.process.stream.service;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenResponse;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.events.EventService;
import org.lorislab.p6.process.stream.events.EventServiceType;
import org.lorislab.p6.process.stream.response.EventResponseService;
import org.lorislab.p6.process.stream.response.EventResponseServiceType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ExecutorTokenService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Inject
    ExecutorCacheService executorCacheService;

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public List<ProcessToken> executeToken(String guid, String name, String payload) {

        ProcessToken token = processTokenRepository.findBy(guid);
        if (token == null) {
            log.warn("No token found for the guid: {}", guid);
            return Collections.emptyList();
        }
        if (!name.equals(token.getNodeName())) {
            log.warn("Wrong name for the token {}. Name: {} excepted: {}", token, token.getNodeName(), name);
            return Collections.emptyList();
        }

        ProcessDefinitionModel pd = executorCacheService.get(token.getProcessId(), token.getProcessVersion());
        if (pd == null) {
            log.warn("No process definition found for the token: {}", token);
            return Collections.emptyList();
        }

        Node node = pd.getNode(name);
        if (node == null) {
            log.error("No node found in the process definition. The task will be ignored. Token: {}, Name: {}", token, name);
            return Collections.emptyList();
        }


        int nc = token.getType().nextNodeCount;
        int size = 0;
        List<String> next = null;
        if (node.sequence != null && node.sequence.to != null) {
            next = node.sequence.to;
            size = next.size();
        }

        // next > 0
        if (nc == -1) {
            if (size == 0) {
                log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} > 0.", token.getType(), token.getNodeName(), size);
                return Collections.emptyList();
            }
        } else if (size != nc) {
            log.error("The node type {} fo token {} has wrong number of next tokens. Expected {} == {}.", token.getType(), token.getNodeName(), nc, size);
            return Collections.emptyList();
        }

        log.info("Execute node: {} Next: {}", node.name, next);
        ProcessTokenType type = token.getType();
        InstanceHandle<EventService> w = Arc.container().instance(EventService.class, EventServiceType.Literal.create(type));
        if (!w.isAvailable()) {
            throw new UnsupportedOperationException("Not supported token type '" + type + "'");
        }
        return w.get().execute(token, pd, node, payload);
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public void executeResponse(String guid, String name, String correlationId, List<ProcessToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            log.info("No next steps for the message. Token:{}, Name:{}, CorrelationId:{}", guid, name, correlationId);
            return;
        }

        for (ProcessToken token : tokens) {
            ProcessTokenResponse response = token.getType().response;
            InstanceHandle<EventResponseService> w = Arc.container().instance(EventResponseService.class, EventResponseServiceType.Literal.create(response));
            if (w.isAvailable()) {
                w.get().response(token, correlationId);
            } else {
                throw new UnsupportedOperationException("Not supported token response '" + response + "'");
            }
        }
    }
}
