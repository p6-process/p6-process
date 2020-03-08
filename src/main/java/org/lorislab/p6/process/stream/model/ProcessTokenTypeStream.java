package org.lorislab.p6.process.stream.model;

import org.lorislab.p6.process.flow.model.Gateway;
import org.lorislab.p6.process.flow.model.Node;

public enum ProcessTokenTypeStream {

    START_EVENT(ProcessTokenResponseStream.DEFAULT, 1),

    END_EVENT(ProcessTokenResponseStream.SINGLETON, 0),

    SERVICE_TASK( ProcessTokenResponseStream.DEFAULT, 1),

    SERVICE_TASK_COMPLETE( ProcessTokenResponseStream.SERVICE_TASK, 1),

    PARALLEL_GATEWAY_DIVERGING(ProcessTokenResponseStream.DEFAULT, -1),

    PARALLEL_GATEWAY_CONVERGING(ProcessTokenResponseStream.SINGLETON, 1),

    EXCLUSIVE_GATEWAY_DIVERGING(ProcessTokenResponseStream.DEFAULT, -1),

    EXCLUSIVE_GATEWAY_CONVERGING(ProcessTokenResponseStream.DEFAULT, 1),

    INCLUSIVE_GATEWAY_DIVERGING(ProcessTokenResponseStream.DEFAULT, -1),

    INCLUSIVE_GATEWAY_CONVERGING(ProcessTokenResponseStream.SINGLETON, 1),
    ;

    public final ProcessTokenResponseStream response;

    public final int nextNodeCount;

    ProcessTokenTypeStream(ProcessTokenResponseStream response, int nextNodeCount) {
        this.nextNodeCount = nextNodeCount;
        this.response = response;
    }

    public static ProcessTokenTypeStream valueOf(Node node) {
        String tmp = node.nodeType.name();
        if (node instanceof Gateway) {
            Gateway pg = (Gateway) node;
            tmp = pg.nodeType + "_" + pg.sequenceFlow;
        }
        return ProcessTokenTypeStream.valueOf(tmp);
    }
}
