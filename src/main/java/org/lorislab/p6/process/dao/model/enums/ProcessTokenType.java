package org.lorislab.p6.process.dao.model.enums;

import org.lorislab.p6.process.model.Gateway;
import org.lorislab.p6.process.model.Node;

public enum ProcessTokenType {

    START_EVENT(ProcessTokenRoute.DEFAULT, 1),

    END_EVENT(ProcessTokenRoute.SINGLETON, 0),

    SERVICE_TASK(ProcessTokenRoute.DEFAULT, 1),

    SERVICE_TASK_COMPLETE(ProcessTokenRoute.SERVICE_TASK, 1),

    PARALLEL_GATEWAY_DIVERGING(ProcessTokenRoute.DEFAULT, -1),

    PARALLEL_GATEWAY_CONVERGING(ProcessTokenRoute.SINGLETON, 1),

    EXCLUSIVE_GATEWAY_DIVERGING(ProcessTokenRoute.DEFAULT, -1),

    EXCLUSIVE_GATEWAY_CONVERGING(ProcessTokenRoute.DEFAULT, 1),

    INCLUSIVE_GATEWAY_DIVERGING(ProcessTokenRoute.DEFAULT, -1),

    INCLUSIVE_GATEWAY_CONVERGING(ProcessTokenRoute.SINGLETON, 1),
    ;

    public final String route;

    public final int nextNodeCount;

    ProcessTokenType(String route, int nextNodeCount) {
        this.nextNodeCount = nextNodeCount;
        this.route = route;
    }

    public static ProcessTokenType valueOf(Node node) {
        String tmp = node.type.name();
        if (node instanceof Gateway) {
            Gateway pg = (Gateway) node;
            tmp = pg.type + "_" + pg.sequence;
        }
        return ProcessTokenType.valueOf(tmp);
    }

}
