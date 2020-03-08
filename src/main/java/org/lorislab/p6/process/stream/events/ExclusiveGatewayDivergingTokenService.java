package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.ExclusiveGateway;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.EXCLUSIVE_GATEWAY_DIVERGING)
public class ExclusiveGatewayDivergingTokenService extends EventService {

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {

        Map<String, Object> data = token.data;

        String item = null;
        ExclusiveGateway gateway = (ExclusiveGateway) node;

        // find the next sequence name
        Map<String, String> condition = gateway.condition;
        Iterator<String> keys = condition.keySet().iterator();
        while (item == null && keys.hasNext()) {
            String key = keys.next();
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.get(key), data);
            if (tmp) {
                item = key;
            }
        }
        log.info("ExclusiveGateway node: {} next: {} default: {}", node.name, item, gateway.defaultSequence);
        if (item == null) {
            item = gateway.defaultSequence;
        }

        token.status = ProcessTokenStatusStream.IN_EXECUTION;
        token.previousName = token.nodeName;
        token.nodeName = item;
        token.type = ProcessTokenTypeStream.valueOf(pd.get(item));
        return Collections.singletonList(token);
    }
}
