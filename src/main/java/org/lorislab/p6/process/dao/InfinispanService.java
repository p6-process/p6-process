/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.p6.process.dao;

import io.quarkus.runtime.StartupEvent;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.lorislab.quarkus.jel.log.interceptor.LoggerExclude;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * The executor cache service
 */
@ApplicationScoped
public class InfinispanService {

    @Inject
    Logger log;

    /**
     * The cache id.
     */
    private static final String CACHE_ID = "p6-cache";

    /**
     * The cache template.
     */
    private static final String CACHE_TEMPLATE = "default";

    /**
     * Remote cache manager.
     */
    @Inject
    RemoteCacheManager cacheManager;

    /**
     * On start application method.
     *
     * @param ev the start up event.
     */
    void onStart(@LoggerExclude @Observes StartupEvent ev) {
        try {
            log.info("Create cache id: {}, template: {}", CACHE_ID, CACHE_TEMPLATE);
            cacheManager.administration().getOrCreateCache(CACHE_ID, CACHE_TEMPLATE);
        } catch (Exception ex) {
            log.error("On start method failed to create the cache id: {}, template: {}. Error: {}", CACHE_ID, CACHE_TEMPLATE, ex.getMessage());
            log.error("Error", ex);
        }
    }
}
