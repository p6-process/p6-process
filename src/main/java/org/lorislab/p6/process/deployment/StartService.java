package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.pi.ProcessCommandMessageListener;
import org.lorislab.p6.process.servicetask.ServiceTaskResponseMessageListener;
import org.lorislab.p6.process.token.TokenExecutionMessageListener;
import org.lorislab.p6.process.token.TokenSingletonMessageListener;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class StartService {

    @Inject
    ProcessCommandMessageListener processRequestMessageListener;

    @Inject
    DeploymentService deploymentService;

    @Inject
    TokenExecutionMessageListener tokenExecutionMessageListener;

    @Inject
    TokenSingletonMessageListener tokenSingletonMessageListener;

    @Inject
    ServiceTaskResponseMessageListener serviceTaskResponseMessageListener;

    void onStart(@Observes StartupEvent ev) {
        // load processes
        deploymentService.start();

        // start process request message listener
        processRequestMessageListener.start();

        // start token message listener
        tokenExecutionMessageListener.start();

        // start token singleton message listener
        tokenSingletonMessageListener.start();

        // start service task response message listener
        serviceTaskResponseMessageListener.start();
    }

}
