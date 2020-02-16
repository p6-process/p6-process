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

import org.lorislab.p6.process.dao.ProcessContentDAO;
import org.lorislab.p6.process.dao.ProcessDefinitionDAO;
import org.lorislab.p6.process.dao.ProcessDeploymentDAO;
import org.lorislab.p6.process.dao.model.ProcessContent;
import org.lorislab.p6.process.dao.model.ProcessDefinition;
import org.lorislab.p6.process.dao.model.ProcessDeployment;
import org.lorislab.p6.process.flow.model.ProcessDefinitionLoader;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.ProcessMetadata;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.ConstraintDAOException;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.lorislab.quarkus.jel.log.interceptor.LoggerExclude;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
public class DeploymentService {

    @Inject
    Logger log;

    @Inject
    ProcessDefinitionDAO processDefinitionRepository;

    @Inject
    ProcessDeploymentDAO processDeploymentRepository;

    @Inject
    ExecutorCacheService executorCacheService;

    @Inject
    ProcessContentDAO processContentDAO;

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deploy(String deploymentId, @LoggerExclude String data) {

        ProcessDefinitionModel pd = ProcessDefinitionLoader.load(data);
        ProcessMetadata metadata = pd.metadata;
        ProcessDeployment deployment = processDeploymentRepository.findByProcessId(metadata.processId);
        if (deployment != null && deployment.getProcessVersion().equals(metadata.processVersion)) {
            log.info("Process definition for the process {} is deployed.", metadata);
            executorCacheService.put(metadata.processId, metadata.processVersion, pd);
        }

        if (deploymentId == null) {
            deploymentId = UUID.randomUUID().toString();
        }

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGuid(deploymentId);
        processDefinition.setApplication(metadata.application);
        processDefinition.setModule(metadata.module);
        processDefinition.setProcessId(metadata.processId);
        processDefinition.setProcessVersion(metadata.processVersion);
        processDefinition.setProcessUrl(metadata.processUrl);

        // create new process definition and content
        try {
            processDefinitionRepository.create(processDefinition, true);

            ProcessContent content = new ProcessContent();
            content.setGuid(processDefinition.getGuid());
            content.setData(DataUtil.serialize(pd));
            processContentDAO.create(content);

        } catch (ConstraintDAOException ce) {
            log.warn("The process definition for the task {} already exists.", metadata);
            processDefinition = processDefinitionRepository.findBy(deploymentId);
            executorCacheService.put(metadata.processId, metadata.processVersion, pd);
        }

        // saveProcessFlow or update the deployment information
        if (deployment == null) {
            deployment = new ProcessDeployment();
            deployment.setProcessDefinitionGuid(processDefinition.getGuid());
            deployment.setProcessId(processDefinition.getProcessId());
            deployment.setProcessVersion(processDefinition.getProcessVersion());
            processDeploymentRepository.create(deployment);
        } else {

            // check the version.
            if (VersionUtil.versionUpdateNeeded(processDefinition.getProcessVersion(), deployment.getProcessVersion())) {
                deployment.setProcessVersion(metadata.processVersion);
                deployment.setProcessDefinitionGuid(processDefinition.getGuid());
                processDeploymentRepository.update(deployment);
            }
        }

        executorCacheService.put(metadata.processId, metadata.processVersion, pd);
    }

}
