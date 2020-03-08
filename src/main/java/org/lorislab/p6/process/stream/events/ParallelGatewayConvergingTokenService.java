package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.mem.service.ProcessTokenService;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.PARALLEL_GATEWAY_CONVERGING)
public class ParallelGatewayConvergingTokenService extends EventService {

    @Inject
    ProcessTokenService processTokenCache;

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {


        ProcessTokenStream gatewayToken = processTokenCache.findByGuid(token.parent.guid);
        if (gatewayToken == null) {

//            String next = node.sequence.to.get(0);
            gatewayToken = token.parent;
            gatewayToken = moveToNextNode(gatewayToken, pd, node);
            gatewayToken.data.putAll(token.data);

//            gatewayToken = new ProcessTokenStream();
//            gatewayToken.status = ProcessTokenStatusStream.CREATED;
//            gatewayToken.processId = token.processId;
//            gatewayToken.processVersion = token.processVersion;
//            gatewayToken.nodeName = next;
//            gatewayToken.createNodeName = next;
//            gatewayToken.type = ProcessTokenTypeStream.valueOf(pd.get(next));
//            gatewayToken.parent = token.parent;
//            gatewayToken.processInstanceGuid = token.processInstanceGuid;
//            gatewayToken.referenceTokenGuid = token.parent.guid;
//            gatewayToken.createdFrom.add(token.guid);
//            gatewayToken.data = token.data;
        } else {
            // add finished parent
            gatewayToken.createdFrom.add(token.guid);
            gatewayToken.data.putAll(token.data);
        }

        // child token finished
        token.status = ProcessTokenStatusStream.FINISHED;

        int current = gatewayToken.createdFrom.size();
        int expected = node.sequence.from.size();
        log.info("Token finished {} node parents {}. Result {}>={}, {}", gatewayToken.createdFrom, node.sequence.from, current, expected, current >= expected);
        if (current >= expected) {
            log.info("Parallel gateway finished Token:{}", gatewayToken);
            return Collections.singletonList(gatewayToken);
        }
        return Collections.emptyList();
    }
}
