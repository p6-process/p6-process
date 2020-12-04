package org.lorislab.p6.process.deployment;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionLoader;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.lorislab.quarkus.log.cdi.LogService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class DeploymentService extends AbstractTest {

    @ConfigProperty(name = "p6.deployment.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "p6.deployment.dir", defaultValue = "p6")
    String dir;

    private final Map<String, ProcessDefinitionRuntime> DEFINITIONS = new HashMap<>();

    @LogService(stacktrace = false)
    public void start() {
        if (!enabled) {
            log.info("Deployment is disabled");
            return;
        }
        loadProcesses();
    }

    private void loadProcesses() {
        if (Files.exists(Paths.get(dir))) {
            try (Stream<Path> files = Files.find(Paths.get(dir), 3, (filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(".yaml"))) {
                files.forEach(path -> {
                    log.info("Deploy process {}.", path);

                    ProcessDefinitionRuntime pd = ProcessDefinitionLoader.loadRuntime(path.toFile());
                    String id = getCacheId(pd.id, pd.version);
                    DEFINITIONS.put(id, pd);
                });
            } catch (IOException ex) {
                throw new UncheckedIOException("Error deploy the process", ex);
            }
        } else {
            log.warn("P6 process directory '{}' does not exists.", dir);
        }
    }

    public ProcessDefinitionRuntime getProcessDefinition(String processId, String processVersion) {
        return DEFINITIONS.get(getCacheId(processId, processVersion));
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


    private static void search ()  {


    }


}
