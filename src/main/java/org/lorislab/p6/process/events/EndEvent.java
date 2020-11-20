package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessInstanceRepository;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.END_EVENT)
public class EndEvent implements EventService {

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        if (item.token.status != ProcessToken.Status.FINISHED) {

            // load process instance
            return processInstanceRepository.findById(item.tx,item.token.processInstance)
                    .onItem().transformToUni(p -> {

                        // FINISHED process instance
                        p.status = ProcessInstance.Status.FINISHED;
                        p.data.getMap().putAll(item.token.data.getMap());
                        item.token.status = ProcessToken.Status.FINISHED;

                        item.changeLog.updateProcessInstance = p;
                        item.changeLog.updateToken = item.token;

                        item.savePoint = true;
                        item.moveToNull();
                        return uni(item);
                    });
        } else {
            log.warn("Token {} is already finished.", item.token);
        }

        item.savePoint = true;
        return uni(item);
    }
}
