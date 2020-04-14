package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionLoader;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.quarkus.jel.log.interceptor.LoggerExclude;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DeploymentService {

    @ConfigProperty(name = "p6.deployment.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "p6.deployment.dir", defaultValue = "p6")
    String dir;

    @Inject
    Logger log;

    private Map<String, ProcessDefinitionRuntime> definitions = new HashMap<>();

    void onStart(@Observes @LoggerExclude StartupEvent ev) {
        log.info("P6 process engine is starting...");
        if (enabled) {
            log.info("Start deploy processes from {}", dir);
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dir), path -> path.toString().endsWith(".yaml"))) {
                for (Path path : directoryStream) {
                    log.info("Deploy process {}.", path);

                    ProcessDefinitionRuntime pd = ProcessDefinitionLoader.loadRuntime(path.toFile());
                    String id = getCacheId(pd.id, pd.version);
                    definitions.put(id, pd);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException("Error deploy the process", ex);
            }
        } else {
            log.info("Deployment is disabled");
        }
        log.info("P6 process engine started.");
    }

    void onStop(@Observes @LoggerExclude ShutdownEvent ev) {
        log.info("P6 process engine is stopping...");
    }


    public ProcessDefinitionRuntime getProcessDefinition(String processId, String processVersion) {
        return definitions.get(getCacheId(processId, processVersion));
    }

    /**
     * Creates the cache ID.
     *
     * @param processId      the process ID.
     * @param processVersion the process version.
     * @return the corresponding cache ID.
     */
    private static String getCacheId(String processId, String processVersion) {
        return processId + ":" + processVersion;
    }

}
