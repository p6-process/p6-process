package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.ProcessTokenContentDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.EXCLUSIVE_GATEWAY_CONVERGING)
public class ExclusiveGatewayConvergingTokenService extends EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {
        String next = node.sequence.to.get(0);
        token.setStatus(ProcessTokenStatus.IN_EXECUTION);
        token.setPreviousName(token.getNodeName());
        token.setNodeName(next);
        token.setType(pd.getNodeProcessTokenType(next));
        token = processTokenRepository.update(token, true);
        return Collections.singletonList(token);
    }
}
