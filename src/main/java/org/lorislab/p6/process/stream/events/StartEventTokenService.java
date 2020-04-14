package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.START_EVENT)
public class StartEventTokenService extends EventService {

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {
        moveToNexNode(messageId, token, pd, node);
        token = processTokenDAO.update(token);
        return Collections.singletonList(token);
    }

}
