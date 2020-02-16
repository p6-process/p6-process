/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.test;

import io.restassured.RestAssured;
import io.vertx.amqp.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lorislab.jel.testcontainers.docker.DockerComposeService;
import org.lorislab.jel.testcontainers.docker.DockerTestEnvironment;
import org.lorislab.p6.process.flow.model.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.lorislab.jel.testcontainers.InjectLoggerExtension;

/**
 * The abstract test
 */
@ExtendWith(InjectLoggerExtension.class)
public abstract class AbstractTest {

    public static DockerTestEnvironment ENVIRONMENT = new DockerTestEnvironment();

    public static String ADDRESS_DEPLOYMENT = "deployment";

    public static String ADDRESS_START_PROCESS = "process-start";

    public static String ADDRESS_SERVICE_TASK = "service-task";

    public static String ADDRESS_TOKEN_EXECUTE = "token-execute";

    @Inject
    protected Logger log;

    /**
     * Starts the containers before the tests
     */
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        ENVIRONMENT.start();
        DockerComposeService testService = ENVIRONMENT.getService("p6-executor");
        if (testService != null) {
            RestAssured.port = testService.getPort(8080);
        }
    }

    protected AmqpMessage receivedMessage(String address) {
        return receivedMessage(address, 5);
    }

    protected AmqpMessage receivedMessage(String address, long timeout) {

        AmqpClient client = null;
        try {
            client = AmqpClient.create(new AmqpClientOptions());

            CompletableFuture<AmqpReceiver> rf = new CompletableFuture<>();
            client.createReceiver(address, x -> rf.complete(x.result()));
            AmqpReceiver receiver = rf.get(timeout, TimeUnit.SECONDS);

            CompletableFuture<AmqpMessage> message = new CompletableFuture<>();
            receiver.handler(message::complete);
            return message.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            throw new IllegalStateException("Error received message", ex);
        } finally {
            close(client);
        }
    }

    protected void sendMessage(String address, AmqpMessage... messages) {
        AmqpClient client = null;
        try {
            client = AmqpClient.create(new AmqpClientOptions());

            CompletableFuture<AmqpSender> senderFuture = new CompletableFuture<>();
            client.createSender(address, x -> senderFuture.complete(x.result()));
            AmqpSender sender = senderFuture.get(5, TimeUnit.SECONDS);

            for (AmqpMessage msg : messages) {
                CompletableFuture<Void> message = new CompletableFuture<>();
                sender.sendWithAck(msg, a -> message.complete(a.result()));
                message.get(5, TimeUnit.SECONDS);
            };

        } catch (Exception ex) {
            throw new IllegalStateException("Error send message", ex);
        } finally {
            close(client);
        }
    }

    private void close(AmqpClient client) {
        if (client == null) {
            return;
        }
        try {
            CompletableFuture<Void> close = new CompletableFuture<>();
            client.close(c -> {
                close.complete(c.result());
            });
            close.get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Error send message", ex);
        }
    }

    protected String loadResource(String name) {
        try {
            return Files.readString(
                    Paths.get(AbstractTest.class.getResource(name).toURI()));
        } catch (URISyntaxException | IOException ux) {
            throw new RuntimeException(ux);
        }
    }

    protected static ProcessDefinition createProcessDefinition() {
        ProcessDefinition pd = new ProcessDefinition();
        ProcessMetadata md = new ProcessMetadata();
        md.application = "application";
        md.module = "module";
        md.processId = "processId";
        md.processUrl = "processUrl";
        md.processVersion = "processVersion";
        pd.metadata = md;

        pd.process = new ArrayList<>();

        StartEvent start = new StartEvent();
        start.name = "start";
        start.nodeType = NodeType.START_EVENT;
        start.sequence = new Sequence();
        start.sequence.to.add("service1");
        pd.process.add(start);

        ServiceTask service1 = new ServiceTask();
        service1.name = "service1";
        service1.nodeType = NodeType.SERVICE_TASK;
        service1.sequence = new Sequence();
        service1.sequence.from.add("start");
        service1.sequence.to.add("gateway1");
        pd.process.add(service1);

        ParallelGateway gateway1 = new ParallelGateway();
        gateway1.name = "gateway1";
        gateway1.nodeType = NodeType.PARALLEL_GATEWAY;
        gateway1.sequenceFlow = SequenceFlow.DIVERGING;
        gateway1.sequence = new Sequence();
        gateway1.sequence.from.add("service1");
        gateway1.sequence.to.add("service3");
        gateway1.sequence.to.add("service4");
        pd.process.add(gateway1);

        ServiceTask service3 = new ServiceTask();
        service3.name = "service3";
        service3.nodeType = NodeType.SERVICE_TASK;
        service3.sequence = new Sequence();
        service3.sequence.from.add("gateway1");
        service3.sequence.to.add("gateway2");
        pd.process.add(service3);

        ServiceTask service4 = new ServiceTask();
        service4.name = "service4";
        service4.nodeType = NodeType.SERVICE_TASK;
        service4.sequence = new Sequence();
        service4.sequence.from.add("gateway1");
        service4.sequence.to.add("gateway2");
        pd.process.add(service4);

        ParallelGateway gateway2 = new ParallelGateway();
        gateway2.name = "gateway2";
        gateway2.nodeType = NodeType.PARALLEL_GATEWAY;
        gateway2.sequenceFlow = SequenceFlow.CONVERGING;
        gateway2.sequence = new Sequence();
        gateway2.sequence.from.add("service3");
        gateway2.sequence.from.add("service4");
        gateway2.sequence.to.add("end");
        pd.process.add(gateway2);

        EndEvent end = new EndEvent();
        end.name = "end";
        end.nodeType = NodeType.END_EVENT;
        end.sequence = new Sequence();
        end.sequence.from.add("gateway2");
        pd.process.add(end);

        return pd;
    }

}
