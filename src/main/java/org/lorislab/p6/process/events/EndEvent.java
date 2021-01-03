package org.lorislab.p6.process.events;

import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.lorislab.p6.process.model.ProcessInstanceRepository;
import org.lorislab.p6.process.model.ProcessInstance;
import org.lorislab.p6.process.model.ProcessToken;
import org.lorislab.p6.process.model.ProcessTokenRepository;
import org.lorislab.p6.process.token.RuntimeToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@Slf4j
@Unremovable
@ApplicationScoped
@EventType(ProcessToken.Type.END_EVENT)
public class EndEvent implements EventService {

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    @Inject
    ProcessTokenRepository processTokenRepository;

    @Override
    public Uni<RuntimeToken> execute(RuntimeToken item) {
        return Uni.combine().all()
                .unis(
                    processInstanceRepository.findById(item.tx,item.token.processInstance),
                    processTokenRepository.countActive(item.tx,item.token.processInstance, item.token.id))
                .combinedWith((processInstance, count) -> update(item, processInstance, count));
    }

    private RuntimeToken update(RuntimeToken item, ProcessInstance processInstance, Long count) {
        if (processInstance == null) {
            log.warn("No process instance {} fount token {}", item.token.processInstance, item.token.id);
            item.savePoint = true;
            return item;
        }
        log.info("MessageId: {}, ProcessInstance: {}, Active token: {}", item.messageId, processInstance.id, count);

        processInstance.data.getMap().putAll(item.token.data.getMap());
        item.token.status = ProcessToken.Status.FINISHED;
        item.token.finished = LocalDateTime.now();

        item.changeLog.updateProcessInstance = processInstance;

        if (count == 0) {
            processInstance.status = ProcessInstance.Status.FINISHED;
            processInstance.finished = item.token.finished;
        }

        item.savePoint = true;
        item.moveToNull();
        return item;
    }
}
