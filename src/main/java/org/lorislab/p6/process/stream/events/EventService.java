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

import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.p6.process.model.Node;
import org.lorislab.p6.process.model.runtime.ProcessDefinitionRuntime;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenDAO;

    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {
        return Collections.emptyList();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node, ProcessToken newToken) {
        return execute(messageId, token, pd, node);
    }

    protected static void moveToNexNode(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, Node node) {
        String next = node.next.get(0);
        token.setStatus(ProcessTokenStatus.IN_EXECUTION);
//        token.setPreviousName(token.getNodeName());
        token.setNodeName(next);
        token.setMessageId(messageId);
        token.setExecutionId(UUID.randomUUID().toString());
        token.setType(ProcessTokenType.valueOf(pd.nodes.get(next)));
    }

    protected List<ProcessToken> createChildTokens(String messageId, ProcessToken token, ProcessDefinitionRuntime pd, List<String> items) {
        List<ProcessToken> tokens = items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.setId(UUID.randomUUID().toString());
            child.setNodeName(item);
            child.setProcessId(token.getProcessId());
            child.setProcessVersion(token.getProcessVersion());
//            child.setCreateNodeName(item);
//            child.setPreviousName(token.getNodeName());
            child.setParent(token.getId());
            child.setType(ProcessTokenType.valueOf(pd.nodes.get(item)));
            child.setProcessInstance(token.getProcessInstance());
            child.setData(token.getData());
            child.setStatus(ProcessTokenStatus.IN_EXECUTION);
            child.setMessageId(messageId);
            child.setExecutionId(UUID.randomUUID().toString());
            return child;
        }).collect(Collectors.toList());

        processTokenDAO.createTokens(tokens);

        return tokens;
    }
}
