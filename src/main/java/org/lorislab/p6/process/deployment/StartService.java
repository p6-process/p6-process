package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.pi.ProcessRequestMessageListener;
import org.lorislab.p6.process.token.TokenExecutionMessageListener;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class StartService {

    @Inject
    ProcessRequestMessageListener processRequestMessageListener;

    @Inject
    DeploymentService deploymentService;

    @Inject
    TokenExecutionMessageListener tokenExecutionMessageListener;

    void onStart(@Observes StartupEvent ev) {
        // load processes
        deploymentService.start();

        // start process request message listener
        processRequestMessageListener.start();

        // start token message listener
        tokenExecutionMessageListener.start();
    }

}
