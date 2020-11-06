package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.reactive.ProcessExecutor;
import org.lorislab.p6.process.reactive.SingletonExecutor;
import org.lorislab.p6.process.reactive.TokenExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class StartService {

    @Inject
    ProcessExecutor processExecutor;

    @Inject
    DeploymentService deploymentService;

    void onStart(@Observes StartupEvent ev) {
        // load processes
        deploymentService.start();
        // start process executor
        processExecutor.start();
    }

}
