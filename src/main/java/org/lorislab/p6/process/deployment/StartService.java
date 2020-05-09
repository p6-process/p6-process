package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import org.lorislab.p6.process.stream.ProcessExecutor;
import org.lorislab.p6.process.stream.TokenExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class StartService {

    @Inject
    PgPool client;

    @Inject
    ProcessExecutor processExecutor;

    @Inject
    TokenExecutor tokenExecutor;

    @Inject
    DeploymentService deploymentService;

    void onStart(@Observes StartupEvent ev) {
        // load processes and migrate database
        deploymentService.deploy().and(
        client.query("CREATE TABLE IF NOT EXISTS PROCESS_MSG (id SERIAL PRIMARY KEY, created timestamp DEFAULT now(), ref varchar(255))")
                .and(client.query("CREATE TABLE IF NOT EXISTS TOKEN_MSG (id SERIAL PRIMARY KEY, created timestamp DEFAULT now(), ref varchar(255))"))
                .and(client.query("CREATE TABLE IF NOT EXISTS PROCESS_INSTANCE (id varchar(255) NOT NULL PRIMARY KEY,data jsonb,parent varchar(255), processId varchar(255), processVersion varchar(255),status varchar(255))"))
                .and(client.query("CREATE TABLE IF NOT EXISTS PROCESS_TOKEN (id varchar(255) NOT NULL PRIMARY KEY,data jsonb,executionId varchar(255),nodeName varchar(255),parent varchar(255),processId varchar(255),processInstance varchar(255),processVersion varchar(255),reference varchar(255),status varchar(255),type varchar(255), createdFrom varchar(255)[])"))
        )
        .await().indefinitely();
        // start timers
        processExecutor.startTimer().and(tokenExecutor.startTimer()).await().indefinitely();
    }
}
