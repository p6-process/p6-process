package org.lorislab.p6.process.message;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class MessageProducer {

    @Inject
    MessageRepository messageRepository;

    public Uni<Long> send(SqlClient client, String queue, List<Message> messages) {
        return messageRepository.create(client, queue, messages);
    }

    public Uni<Long> send(SqlClient client, Message message) {
        return messageRepository.create(client, message);
    }

    public Uni<Long> send(Message message) {
        return messageRepository.create(message);
    }

}
