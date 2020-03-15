package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.ExclusiveGateway;
import org.lorislab.p6.process.flow.model.Node;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.EXCLUSIVE_GATEWAY_DIVERGING)
public class ExclusiveGatewayDivergingTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {

        String item = null;
        ExclusiveGateway gateway = (ExclusiveGateway) node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (item == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), token.data);
            if (tmp) {
                item = key;
            }
        }
        log.info("ExclusiveGateway node: {} next: {} default: {}", node.name, item, gateway.defaultSequence);
        if (item == null) {
            item = gateway.defaultSequence;
        }

        return createChildTokens(messageId, token, pd, Collections.singletonList(item));
    }
}
