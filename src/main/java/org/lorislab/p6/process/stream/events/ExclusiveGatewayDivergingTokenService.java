package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.ExclusiveGateway;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.p6.process.stream.reactive.ProcessExpressionHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.*;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.EXCLUSIVE_GATEWAY_DIVERGING)
public class ExclusiveGatewayDivergingTokenService extends EventService {

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {

        String item = null;
        ExclusiveGateway gateway = (ExclusiveGateway) node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (item == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), token.getData());
            if (tmp) {
                item = key;
            }
        }
        log.info("ExclusiveGateway node: {} next: {} default: {}", node.name, item, gateway.defaultNext);
        if (item == null) {
            item = gateway.defaultNext;
        }

        token.setNodeName(item);
        token.setMessageId(messageId);
        token.setExecutionId(UUID.randomUUID().toString());
        token.setType(ProcessTokenType.valueOf(pd.nodes.get(item)));
        token = processTokenDAO.update(token);

        return Collections.singletonList(token);
    }
}
