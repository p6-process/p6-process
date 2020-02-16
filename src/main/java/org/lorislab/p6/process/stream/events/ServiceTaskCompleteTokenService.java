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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.SERVICE_TASK_COMPLETE)
public class ServiceTaskCompleteTokenService extends EventService {

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {
        String next = node.sequence.to.get(0);
        token.setStatus(ProcessTokenStatus.IN_EXECUTION);
        token.setPreviousName(token.getNodeName());
        token.setNodeName(next);
        token.setType(pd.getNodeProcessTokenType(next));
        token = processTokenRepository.update(token, true);

        ProcessTokenContent content = processTokenContentDAO.findBy(token.getGuid());
        content.setData(DataUtil.merge(content.getData(), DataUtil.stringToByte(payload)));
        processTokenContentDAO.update(content);
        return Collections.singletonList(token);
    }
}
