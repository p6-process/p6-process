package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.ExclusiveGateway;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.INCLUSIVE_GATEWAY_DIVERGING)
public class InclusiveGatewayDivergingTokenService extends EventService {

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        // close the gateway diverging token
        token.status = ProcessTokenStatusStream.FINISHED;

        Map<String, Object> data = token.data;

        List<String> items = new ArrayList<>();
        ExclusiveGateway gateway = (ExclusiveGateway) node;

        // find the next sequence name
        Map<String, String> conditions = gateway.condition;
        for (Map.Entry<String, String> condition : conditions.entrySet()) {
            boolean tmp = ProcessExpressionHelper.ifExpression(condition.getValue(), data);
            if (tmp) {
                items.add(condition.getKey());
            }
        }

        log.info("ExclusiveGateway node: {} next: {} default: {}", node.name, items, gateway.defaultSequence);
        if (items.isEmpty()) {
            items.add(gateway.defaultSequence);
        }

        return createChildTokens(token, pd, items);
    }
}
