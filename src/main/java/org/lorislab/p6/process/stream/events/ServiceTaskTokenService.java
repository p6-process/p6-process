package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.SERVICE_TASK)
public class ServiceTaskTokenService extends EventService {

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        token.type = ProcessTokenTypeStream.SERVICE_TASK_COMPLETE;
        // FIXME: save token
        return Collections.singletonList(token);
    }
}
