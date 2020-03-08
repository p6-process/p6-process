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


import org.lorislab.p6.process.flow.model.Node;
import org.lorislab.p6.process.flow.model.ProcessDefinitionModel;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EventService {

    @Inject
    Logger log;

    public List<ProcessTokenStream> execute(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        log.warn("Default execution for the token: {}", token);
        return Collections.emptyList();
    }

    protected static ProcessTokenStream moveToNextNode(ProcessTokenStream token, ProcessDefinitionModel pd, Node node) {
        String next = node.sequence.to.get(0);
        token.status = ProcessTokenStatusStream.IN_EXECUTION;
        token.previousName = token.nodeName;
        token.nodeName = next;
        token.type = ProcessTokenTypeStream.valueOf(pd.get(next));
        return token;
    }

    protected static List<ProcessTokenStream> createChildTokens(ProcessTokenStream token, ProcessDefinitionModel pd, List<String> items) {
        return items.stream().map(item -> {
            ProcessTokenStream child = new ProcessTokenStream();
            child.nodeName = item;
            child.processId = token.processId;
            child.processVersion = token.processVersion;
            child.createNodeName = item;
            child.previousName = token.nodeName;
            child.parent = token;
            child.type = ProcessTokenTypeStream.valueOf(pd.get(item));
            child.processInstanceGuid = token.processInstanceGuid;
            child.status = ProcessTokenStatusStream.IN_EXECUTION;
            child.data = token.data;
            return child;
        }).collect(Collectors.toList());
    }
}
