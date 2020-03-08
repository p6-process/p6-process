package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.START_EVENT)
public class StartEventTokenService extends EventService {

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        return Collections.singletonList(moveToNextNode(token, pd, node));
    }

}
