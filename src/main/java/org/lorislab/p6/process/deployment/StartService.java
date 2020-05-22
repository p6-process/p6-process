package org.lorislab.p6.process.deployment;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.pgclient.pubsub.PgSubscriber;
import io.vertx.pgclient.PgConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.lorislab.p6.process.dao.MessageDAO;
import org.lorislab.p6.process.stream.ProcessExecutor;
import org.lorislab.p6.process.stream.TokenExecutor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class StartService {

//    @Inject
//    PgPool client;

    @Inject
    ProcessExecutor processExecutor;

//    @Inject
//    TokenExecutor tokenExecutor;
//
    @Inject
    DeploymentService deploymentService;

    @Inject
    TokenExecutor tokenExecutor;

//    @Inject
//    MessageDAO messageDAO;

    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    String url;

    @ConfigProperty(name = "quarkus.datasource.username")
    String username;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    void onStart(@Observes StartupEvent ev) {
        // load processes and migrate database
//        deploymentService.deploy().and(
//                client.query("CREATE TABLE IF NOT EXISTS PROCESS_MSG (id SERIAL PRIMARY KEY, created timestamp DEFAULT now(), ref varchar(255))")
//                        .and(client.query("CREATE TABLE IF NOT EXISTS TOKEN_MSG (id SERIAL PRIMARY KEY, created timestamp DEFAULT now(), ref varchar(255))"))
//                        .and(client.query("CREATE TABLE IF NOT EXISTS PROCESS_INSTANCE (id varchar(255) NOT NULL PRIMARY KEY,data jsonb,parent varchar(255), processId varchar(255), processVersion varchar(255),status varchar(255))"))
//                        .and(client.query("CREATE TABLE IF NOT EXISTS PROCESS_TOKEN (id varchar(255) NOT NULL PRIMARY KEY,data jsonb,executionId varchar(255),nodeName varchar(255),parent varchar(255),processId varchar(255),processInstance varchar(255),processVersion varchar(255),reference varchar(255),status varchar(255),type varchar(255), createdFrom varchar(255)[])"))
//                .and(client.query(PROCESS_MSG_PUB))
//        )
//                .await().indefinitely();
//
//        client.query(PROCESS_MSG_TRIGGER).await().indefinitely();

        deploymentService.start();

        processExecutor.start();

        tokenExecutor.start();

//        Multi.createBy().merging().streams(messageDAO.findAllProcessMessages(), subscriber())
//                .onItem().produceUni(x -> processExecutor.execute())
//                .concatenate()
//                .subscribe().with(m -> log.info("Executed message {} ", m), Throwable::printStackTrace);

    }

//    private static final String PROCESS_MSG_TRIGGER =
//            "CREATE TRIGGER process_msg_trigger\n" +
//                    "    AFTER INSERT\n" +
//                    "    ON process_msg\n" +
//                    "    FOR EACH ROW\n" +
//                    "EXECUTE PROCEDURE process_msg_pub();";
//
//    private static final String PROCESS_MSG_PUB =
//            "CREATE OR REPLACE FUNCTION process_msg_pub()\n" +
//                    "    RETURNS trigger AS\n" +
//                    "$$\n" +
//                    "BEGIN\n" +
//                    "    PERFORM pg_notify('process_msg', NEW.id::text);\n" +
//                    "    RETURN NEW;\n" +
//                    "END;\n" +
//                    "$$ LANGUAGE plpgsql;";

//    @Inject
//    Vertx vertx;

//    public Multi<String> subscriber() {
//        return Multi.createFrom().emitter(emitter -> {
//            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, toPgConnectOptions(url, username, password));
//            subscriber.connect().subscribe().with(c -> {
//                System.out.println("##############-################## -> process_msg");
//                subscriber.channel("process_msg").handler(emitter::emit);
//            }, Throwable::printStackTrace);
//        });
//    }

    @Produces
    public PgConnectOptions pgConnectOptions() {
        PgConnectOptions pgConnectOptions = PgConnectOptions.fromUri(url);
        pgConnectOptions.setUser(username);
        pgConnectOptions.setPassword(password);
        return pgConnectOptions;
    }
}
