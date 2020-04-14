package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.SERVICE_TASK)
public class ServiceTaskTokenService extends EventService {

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {
        token.type = ProcessTokenType.SERVICE_TASK_COMPLETE;
        token.messageId = messageId;
        token.executionId = UUID.randomUUID().toString();
        processTokenDAO.update(token);
        return Collections.singletonList(token);
    }
}
