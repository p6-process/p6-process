package org.lorislab.p6.process.deployment;

import io.vertx.pgclient.PgConnectOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;

public class PgConnectOptionsProducer {

    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    String url;

    @ConfigProperty(name = "quarkus.datasource.username")
    String username;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    @Produces
    public PgConnectOptions pgConnectOptions() {
        PgConnectOptions pgConnectOptions = PgConnectOptions.fromUri(url);
        pgConnectOptions.setUser(username);
        pgConnectOptions.setPassword(password);
        return pgConnectOptions;
    }
}
