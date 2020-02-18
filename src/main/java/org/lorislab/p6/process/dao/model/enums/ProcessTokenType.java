package org.lorislab.p6.process.dao.model.enums;

import org.lorislab.p6.process.flow.model.ExclusiveGateway;
import org.lorislab.p6.process.flow.model.Gateway;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ParallelGateway;

public enum ProcessTokenType {

    START_EVENT(ProcessTokenResponse.DEFAULT, 1),

    END_EVENT(ProcessTokenResponse.SINGLETON, 0),

    SERVICE_TASK( ProcessTokenResponse.DEFAULT, 1),

    SERVICE_TASK_COMPLETE( ProcessTokenResponse.SERVICE_TASK, 1),

    PARALLEL_GATEWAY_DIVERGING(ProcessTokenResponse.DEFAULT, -1),

    PARALLEL_GATEWAY_CONVERGING(ProcessTokenResponse.SINGLETON, 1),

    EXCLUSIVE_GATEWAY_DIVERGING(ProcessTokenResponse.DEFAULT, -1),

    EXCLUSIVE_GATEWAY_CONVERGING(ProcessTokenResponse.DEFAULT, 1),

    ;

    public final ProcessTokenResponse response;

    public final int nextNodeCount;

    ProcessTokenType(ProcessTokenResponse response, int nextNodeCount) {
        this.nextNodeCount = nextNodeCount;
        this.response = response;
    }

    public static ProcessTokenType valueOf(Node node) {
        String tmp = node.nodeType.name();
        if (node instanceof Gateway) {
            Gateway pg = (Gateway) node;
            tmp = pg.nodeType + "_" + pg.sequenceFlow;
        }
        return ProcessTokenType.valueOf(tmp);
    }
}
