package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.ProcessInstanceContentDAO;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceContent;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
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
import java.util.Date;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.END_EVENT)
public class EndEventTokenService extends EventService {

    @Inject
    ProcessInstanceDAO processInstanceRepository;

    @Inject
    ProcessInstanceContentDAO processInstanceContentDAO;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {
        if (token.getStatus() != ProcessTokenStatus.FINISHED) {
            token.setStatus(ProcessTokenStatus.FINISHED);
            token.setFinishedDate(new Date());
            token = processTokenDAO.update(token, true);

            // update the process instance
            // TODO: check if all tokens finished!
            ProcessInstance pi = processInstanceRepository.findBy(token.getProcessInstanceGuid());
            pi.setStatus(ProcessInstanceStatus.FINISHED);
            processInstanceRepository.update(pi);

            // update the process variables
            ProcessTokenContent tokenContent = processTokenContentDAO.findBy(token.getGuid());
            ProcessInstanceContent content = processInstanceContentDAO.findBy(pi.getGuid());
            content.setData(DataUtil.merge(content.getData(), tokenContent.getData()));
            processInstanceContentDAO.update(content);
        }
        return Collections.emptyList();
    }
}
