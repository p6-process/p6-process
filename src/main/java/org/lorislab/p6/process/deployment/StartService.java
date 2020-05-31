package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.pgclient.pubsub.PgSubscriber;
import io.vertx.pgclient.PgConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.stream.ProcessExecutor;
import org.lorislab.p6.process.stream.SingletonExecutor;
import org.lorislab.p6.process.stream.TokenExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class StartService {

    @Inject
    ProcessExecutor processExecutor;

    @Inject
    DeploymentService deploymentService;

    @Inject
    TokenExecutor tokenExecutor;

    @Inject
    SingletonExecutor singletonExecutor;

    void onStart(@Observes StartupEvent ev) {
        // load processes
        deploymentService.start();
        // start process executor
        processExecutor.start();
        // start token executor
        tokenExecutor.start();
        // start singleton executor
        singletonExecutor.start();
    }

}
