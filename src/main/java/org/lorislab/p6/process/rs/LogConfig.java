package org.lorislab.p6.process.rs;

import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.SqlClient;
import org.lorislab.quarkus.log.cdi.LogParamValue;

import javax.enterprise.inject.Produces;

import static org.lorislab.quarkus.log.cdi.LogParamValue.assignable;

public class LogConfig {

    @Produces
    public LogParamValue logRow() {
        return assignable((v) -> "row:" + v.hashCode(), Row.class);
    }

    @Produces
    public LogParamValue logRoutingContext() {
        return assignable((v) -> ((RoutingContext)v).normalisedPath(), RoutingContext.class);
    }

    @Produces
    public LogParamValue logTransaction() {
        return assignable((v) -> "tx:" + v.hashCode(), SqlClient.class);
    }

}
