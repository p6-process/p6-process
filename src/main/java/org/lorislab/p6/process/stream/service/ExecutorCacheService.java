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

package org.lorislab.p6.process.stream.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.quarkus.runtime.StartupEvent;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.lorislab.p6.process.flow.model.ProcessDefinition;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.lorislab.quarkus.jel.log.interceptor.LoggerExclude;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * The executor cache service
 */
@ApplicationScoped
public class ExecutorCacheService {

    @Inject
    Logger log;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectReader READER = MAPPER.readerFor(ProcessDefinitionModel.class);

    private static final ObjectWriter WRITER = MAPPER.writerFor(ProcessDefinition.class);

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
            getCache();
        } catch (Exception ex) {
            log.error("On start method failed to create the cache id: {}, template: {}. Error: {}", CACHE_ID, CACHE_TEMPLATE, ex.getMessage());
            log.error("Error", ex);
        }
    }

    /**
     * Puts the data to the cache.
     *
     * @param processId      the process ID.
     * @param processVersion the process version.
     * @param data           the data.
     */
    public void put(String processId, String processVersion, @LoggerExclude ProcessDefinition data) {
        try {
            String cacheId = getCacheId(processId, processVersion);
            getCache().put(cacheId, WRITER.writeValueAsString(data));
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_PUT_CACHE_PROCESS_DEFINITION_MODEL, ex, processId, processVersion);
        }
    }

    /**
     * Gets the data from the cache.
     *
     * @param processId      the process ID.
     * @param processVersion the process version.
     * @return the corresponding response.
     */
    public ProcessDefinitionModel get(String processId, String processVersion) {
        try {
            String cacheId = getCacheId(processId, processVersion);
            String data = getCache().get(cacheId);
            return READER.readValue(data);
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_CACHE_PROCESS_DEFINITION_MODEL, ex, processId, processVersion);
        }
    }

    /**
     * Gets the cache.
     *
     * @return the remote cache.
     */
    private RemoteCache<String, String> getCache() {
        return cacheManager.administration().getOrCreateCache(CACHE_ID, CACHE_TEMPLATE);
    }

    /**
     * Creates the cache ID.
     *
     * @param processId      the process ID.
     * @param processVersion the process version.
     * @return the corresponding cache ID.
     */
    private static String getCacheId(String processId, String processVersion) {
        return processId + ":" + processVersion;
    }

    public enum ErrorKeys {

        ERROR_GET_CACHE_PROCESS_DEFINITION_MODEL,

        ERROR_PUT_CACHE_PROCESS_DEFINITION_MODEL,
        ;
    }
}
