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

package org.lorislab.p6.process.stream.events;

import org.lorislab.p6.process.dao.ProcessTokenContentDAO;
import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.ProcessTokenContent;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.quarkus.jel.jpa.exception.ConstraintDAOException;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Inject
    ProcessTokenContentDAO processTokenContentDAO;

    public List<ProcessToken> execute(ProcessToken token, ProcessDefinitionModel pd, Node node, String payload) {
        return Collections.emptyList();
    }

    protected static void moveToNexNode(ProcessToken token, ProcessDefinitionModel pd, Node node) {
        String next = node.sequence.to.get(0);
        token.setStatus(ProcessTokenStatus.IN_EXECUTION);
        token.setPreviousName(token.getNodeName());
        token.setNodeName(next);
        token.setMessageId(UUID.randomUUID().toString());
        token.setType(pd.getNodeProcessTokenType(next));
    }

    protected List<ProcessToken> createChildTokens(ProcessToken token, byte[] data, ProcessDefinitionModel pd, List<String> items) {
        List<ProcessToken> tokens = items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.setNodeName(item);
            child.setProcessId(token.getProcessId());
            child.setProcessVersion(token.getProcessVersion());
            child.setCreateNodeName(item);
            child.setPreviousName(token.getNodeName());
            child.setParent(token.getGuid());
            child.setType(pd.getNodeProcessTokenType(item));
            child.setProcessInstanceGuid(token.getProcessInstanceGuid());
            child.setStatus(ProcessTokenStatus.IN_EXECUTION);
            child.setMessageId(UUID.randomUUID().toString());
            return child;
        }).collect(Collectors.toList());

        // create the child tokens
        try {
            processTokenDAO.create(tokens, true);

            List<ProcessTokenContent> contents = tokens.stream()
                    .map(ProcessToken::getGuid)
                    .map(x -> {
                        ProcessTokenContent pt = new ProcessTokenContent();
                        pt.setGuid(x);
                        pt.setData(data);
                        return pt;
                    }).collect(Collectors.toList());

            processTokenContentDAO.create(contents);
        } catch (ConstraintDAOException ex) {
            log.warn("Tokens are already created. Task {}/{}", token.getGuid(), token.getNodeName());
            tokens = processTokenDAO.findAllTokensInExecution(token.getProcessInstanceGuid(), token.getGuid());
        }
        return tokens;
    }
}
