package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.END_EVENT)
public class EndEventTokenService extends EventService {

    @Inject
    ProcessInstanceDAO processInstanceRepository;

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {
        if (token.getStatus() != ProcessTokenStatus.FINISHED) {
            token.setStatus(ProcessTokenStatus.FINISHED);
//            token.setFinishedDate(new Date());
            processTokenDAO.update(token);

            // update the process instance
            // TODO: check if all tokens finished!
            ProcessInstance pi = processInstanceRepository.findByGuid(token.getProcessInstance());
            pi.setStatus(ProcessInstanceStatus.FINISHED);
            pi.getData().putAll(token.getData());
            processInstanceRepository.update(pi);
        }
        return Collections.emptyList();
    }
}
