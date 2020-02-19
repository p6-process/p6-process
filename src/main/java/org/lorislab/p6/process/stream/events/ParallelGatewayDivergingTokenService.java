package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.PARALLEL_GATEWAY_DIVERGING)
public class ParallelGatewayDivergingTokenService extends EventService {

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {

        // close the gateway diverging token
        token.setStatus(ProcessTokenStatus.FINISHED);
        token = processTokenDAO.update(token, true);

        ProcessTokenContent ptc = processTokenContentDAO.findBy(token.getGuid());

        // child tokens of the gateway node
        return createChildTokens(token, ptc.getData(), pd, node.sequence.to);
    }
}
