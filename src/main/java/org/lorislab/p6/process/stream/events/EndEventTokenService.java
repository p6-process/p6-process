package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.mem.service.PersistenceInstanceService;
import org.lorislab.p6.process.stream.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenTypeStream.END_EVENT)
public class EndEventTokenService extends EventService {

    @Inject
    PersistenceInstanceService persistenceInstanceCache;

    @Override
    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        if (token.status != ProcessTokenStatusStream.FINISHED) {
            token.status = ProcessTokenStatusStream.FINISHED;
            token.finishedDate = new Date();

            // update the process instance
            // TODO: check if all tokens finished!
            ProcessInstance pi = persistenceInstanceCache.findByGuid(token.processInstanceGuid);
            pi.status = ProcessInstanceStatus.FINISHED;

            // merge data to process instance
            if (pi.data == null) {
                pi.data = new HashMap<>();
            }
            pi.data.putAll(token.data);
            persistenceInstanceCache.update(pi);
        }
        return Collections.emptyList();
    }
}
