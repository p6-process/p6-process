package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.END_EVENT)
public class EndEvent implements EventService {

    @Inject
    ProcessInstanceDAO processInstanceDAO;

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        if (item.token.status != ProcessToken.Status.FINISHED) {

            // load process instance
            return processInstanceDAO.findById(item.tx,item.token.processInstance)
                    .onItem().transform(p -> {

                        // FINISHED process instance
                        p.status = ProcessInstance.Status.FINISHED;
                        p.data.getMap().putAll(item.token.data.getMap());
                        item.token.status = ProcessToken.Status.FINISHED;
                        item.moveTo(null);

                        item.changeLog.updateProcessInstance = p;
                        item.changeLog.updateToken = item.token;

                        return item;
                    });
        } else {
            log.warn("Token {} is already finished.", item.token);
        }
        return Uni.createFrom().item(item);
    }
}
