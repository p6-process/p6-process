package org.lorislab.p6.process.stream.events;

import io.quarkus.arc.Unremovable;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Unremovable
@ApplicationScoped
@EventServiceType(ProcessTokenType.END_EVENT)
public class EndEventTokenService extends EventService {

    @Inject
    ProcessInstanceDAO processInstanceRepository;

    @Override
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {
        if (token.status != ProcessTokenStatus.FINISHED) {
            token.status = ProcessTokenStatus.FINISHED;
//            token.setFinishedDate(new Date());
            token = processTokenDAO.update(token);

            // update the process instance
            // TODO: check if all tokens finished!
            ProcessInstance pi = processInstanceRepository.findByGuid(token.processInstance);
            pi.status = ProcessInstanceStatus.FINISHED;
            pi.data.putAll(token.data);
            processInstanceRepository.update(pi);
        }
        return Collections.emptyList();
    }
}
