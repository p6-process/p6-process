package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDivergingTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {

        // close the gateway diverging token
        token.status = ProcessTokenStatus.FINISHED;
        processTokenDAO.update(token);

        // child tokens of the gateway node
        return createChildTokens(messageId, token, pd, node.next);
    }
}
