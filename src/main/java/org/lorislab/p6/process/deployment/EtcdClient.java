package org.lorislab.p6.process.deployment;

import io.etcd.jetcd.*;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class EtcdClient {

    static Client CLIENT = Client.builder()
            .endpoints("http://localhost:2379").build();

    @Produces
    @Singleton
    public KV kv() {
        return CLIENT.getKVClient();
    }

    @Produces
    @Singleton
    public Lock lock() {
        return CLIENT.getLockClient();
    }

    @Produces
    @Singleton
    public Watch watch() {
        return CLIENT.getWatchClient();
    }

    @Produces
    @Singleton
    public Lease lease() {
        return CLIENT.getLeaseClient();
    }
}
