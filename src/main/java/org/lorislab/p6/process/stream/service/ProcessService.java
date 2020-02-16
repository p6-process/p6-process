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

import org.lorislab.p6.process.dao.*;
import org.lorislab.p6.process.dao.model.*;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.DataUtil;
import org.lorislab.quarkus.jel.jpa.exception.ConstraintDAOException;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcessService {

    @Inject
    Logger log;

    @Inject
    ProcessDefinitionDAO processDefinitionRepository;

    @Inject
    ProcessDeploymentDAO processDeploymentRepository;

    @Inject
    ProcessInstanceDAO processInstanceRepository;

    @Inject
    ProcessTokenDAO processTokenRepository;

    @Inject
    ExecutorCacheService executorCacheService;

    @Inject
    ProcessInstanceContentDAO processInstanceContentDAO;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public List<ProcessToken> createTokens(String processInstanceId, String processId, String processVersion, String data) {

        ProcessDefinition pd = null;
        if (processVersion == null || processVersion.isEmpty()) {
            ProcessDeployment deployment = processDeploymentRepository.findByProcessId(processId);
            if (deployment != null) {
                pd = processDefinitionRepository.findBy(deployment.getProcessDefinitionGuid());
            }
        } else {
            pd = processDefinitionRepository.findByProcessIdAndProcessVersion(processId, processVersion);
        }

        if (pd == null) {
            log.error("No process definition found for the {}/{}/{}", processInstanceId, processId, processVersion);
            return Collections.emptyList();
        }

        ProcessInstance pi = new ProcessInstance();
        pi.setGuid(processInstanceId);
        pi.setStatus(ProcessInstanceStatus.IN_EXECUTION);
        pi.setProcessId(pd.getProcessId());
        pi.setProcessVersion(pd.getProcessVersion());
        pi.setProcessDefinitionGuid(pd.getGuid());


        byte[] tmp;
        try {
            tmp = DataUtil.stringToJson(data);
        } catch (Exception ex) {
            log.error("Error reading the process variables");
            pi.setStatus(ProcessInstanceStatus.FAILED);
            tmp = DataUtil.stringToByte(data);
        }

        try {
            pi = processInstanceRepository.create(pi, true);

            ProcessInstanceContent content = new ProcessInstanceContent();
            content.setGuid(pi.getGuid());
            content.setData(tmp);
            processInstanceContentDAO.create(content, true);
            tmp = content.getData();

        } catch (ConstraintDAOException ce) {
            log.warn("The process instance with a id '{}' already exists!", processInstanceId);
            return processTokenRepository.findAllCreatedTokensCreate(processInstanceId);
        }

        if (pi.getStatus() == ProcessInstanceStatus.FAILED) {
            log.warn("The process instance {} failed to start.", pi.getGuid());
            return Collections.emptyList();
        }

        ProcessDefinitionModel pdm = executorCacheService.get(pd.getProcessId(), pd.getProcessVersion());
        List<Node> nodes = pdm.start;
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        final ProcessInstance ppi = pi;
        List<ProcessToken> tokens = nodes.stream()
                .map(node -> {
                    ProcessToken token = new ProcessToken();
                    token.setProcessId(ppi.getProcessId());
                    token.setProcessVersion(ppi.getProcessVersion());
                    token.setNodeName(node.name);
                    token.setStartNodeName(node.name);
                    token.setCreateNodeName(node.name);
                    token.setType(ProcessTokenType.valueOf(node));
                    token.setStatus(ProcessTokenStatus.CREATED);
                    token.setPreviousName(null);
                    token.setProcessInstanceGuid(ppi.getGuid());
                    return token;
                }).collect(Collectors.toList());

        try {
            processTokenRepository.create(tokens, true);

            final byte[] contentData = tmp;
            List<ProcessTokenContent> contents = tokens.stream()
                    .map(ProcessToken::getGuid)
                    .map(g -> {
                        ProcessTokenContent pc = new ProcessTokenContent();
                        pc.setGuid(g);
                        pc.setData(contentData);
                        return pc;
                    }).collect(Collectors.toList());

            processTokenContentDAO.create(contents, true);
        } catch (ConstraintDAOException ce) {
            log.warn("The token for the task {}/{}/{} already exists.", processInstanceId, processId, processVersion);
            return processTokenRepository.findAllCreatedTokensCreate(processInstanceId);
        }
        return tokens;
    }

}
