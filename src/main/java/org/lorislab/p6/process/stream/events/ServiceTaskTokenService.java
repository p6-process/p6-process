package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.SERVICE_TASK)
public class ServiceTaskTokenService extends EventService {

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {
        token.setType(ProcessTokenType.SERVICE_TASK_COMPLETE);
        token.setMessageId(UUID.randomUUID().toString());
        token = processTokenDAO.update(token, true);
        return Collections.singletonList(token);
    }
}
