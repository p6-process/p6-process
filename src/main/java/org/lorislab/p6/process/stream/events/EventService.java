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
import org.lorislab.p6.process.deployment.ProcessDefinitionModel;
import org.lorislab.p6.process.flow.model.Node;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public abstract class EventService {

    @Inject
    Logger log;

    @Inject
    ProcessTokenDAO processTokenDAO;

    public List<ProcessToken> execute(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {
        return Collections.emptyList();
    }

    protected static void moveToNexNode(String messageId, ProcessToken token, ProcessDefinitionModel pd, Node node) {
        String next = node.sequence.to.get(0);
        token.status = ProcessTokenStatus.IN_EXECUTION;
//        token.setPreviousName(token.getNodeName());
        token.nodeName = next;
        token.messageId = messageId;
        token.executionId = UUID.randomUUID().toString();
        token.type = ProcessTokenType.valueOf(pd.nodes.get(next));
    }

    protected List<ProcessToken> createChildTokens(String messageId, ProcessToken token, ProcessDefinitionModel pd, List<String> items) {
        Map<String, ProcessToken> tokens = items.stream().map(item -> {
            ProcessToken child = new ProcessToken();
            child.guid = UUID.randomUUID().toString();
            child.nodeName = item;
            child.processId = token.processId;
            child.processVersion = token.processVersion;
//            child.setCreateNodeName(item);
//            child.setPreviousName(token.getNodeName());
            child.parent = token.guid;
            child.type = ProcessTokenType.valueOf(pd.nodes.get(item));
            child.processInstance = token.processInstance;
            child.data = token.data;
            child.status = ProcessTokenStatus.IN_EXECUTION;
            child.messageId = messageId;
            child.executionId = UUID.randomUUID().toString();
            return child;
        }).collect(Collectors.toMap(t -> t.guid, t -> t));

        processTokenDAO.createAll(tokens);

        return new ArrayList<>(tokens.values());
    }
}
