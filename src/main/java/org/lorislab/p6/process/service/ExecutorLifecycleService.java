package org.lorislab.p6.process.service;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.stream.service.DeploymentService;
import org.lorislab.quarkus.jel.log.interceptor.LoggerService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class ExecutorLifecycleService {

    @ConfigProperty(name = "p6.deployment.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "p6.deployment.dir", defaultValue = "p6")
    String dir;

    @Inject
    Logger log;

    @Inject
    DeploymentService deploymentService;

    @LoggerService(log = false)
    void onStart(@Observes StartupEvent ev) {
        log.info("P6 process engine is starting...");
        if (enabled) {
            log.info("Start deploy processes from {}", dir);
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dir), path -> path.toString().endsWith(".json"))) {
                for (Path path : directoryStream) {
                    log.info("Deploy process {}.", path);
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    deploymentService.deploy(UUID.randomUUID().toString(), content);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException("Error deploy the process", ex);
            }
        } else {
            log.info("Deployment is disabled");
        }
        log.info("P6 process engine started.");
    }

    @LoggerService(log = false)
    void onStop(@Observes ShutdownEvent ev) {
        log.info("P6 process engine is stopping...");
    }
}
