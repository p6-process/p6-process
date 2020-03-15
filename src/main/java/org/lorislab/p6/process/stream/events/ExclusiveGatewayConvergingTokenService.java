package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.EXCLUSIVE_GATEWAY_CONVERGING)
public class ExclusiveGatewayConvergingTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {
        moveToNexNode(messageId, token, pd, node);
        processTokenDAO.update(token);
        return Collections.singletonList(token);
    }
}
