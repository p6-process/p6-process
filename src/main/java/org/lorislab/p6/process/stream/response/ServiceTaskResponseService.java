package org.lorislab.p6.process.stream.response;

import io.quarkus.arc.Unremovable;
import io.smallrye.reactive.messaging.amqp.AmqpMessage;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.amqp.AmqpMessageBuilder;
import org.lorislab.p6.process.dao.ProcessTokenContentDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenResponse;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Unremovable
@ApplicationScoped
@EventResponseServiceType(ProcessTokenResponse.SERVICE_TASK)
public class ServiceTaskResponseService implements EventResponseService {

    @Inject
    @Channel("service-task")
    Emitter<AmqpMessage<String>> emitter;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void response(ProcessToken token, String correlationId) {
        AmqpMessageBuilder output = EventResponseService.createMessageBuilder(token, correlationId);
        ProcessTokenContent content = processTokenContentDAO.findBy(token.getGuid());
        output.withBody(DataUtil.byteToString(content.getData()));
        emitter.send(new AmqpMessage<>(output.build()));
    }
}
