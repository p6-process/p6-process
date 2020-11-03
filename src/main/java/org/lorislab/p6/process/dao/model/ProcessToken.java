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

package org.lorislab.p6.process.dao.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.ToString;
import org.lorislab.p6.process.model.Gateway;
import org.lorislab.p6.process.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ToString
@RegisterForReflection
public class ProcessToken {

    public String id;

    public String processInstance;

    public String processId;

    public String processVersion;

    public String nodeName;

    public Status status;

    public Type type;

    public String parent;

    public String reference;

    public Set<String> createdFrom = new HashSet<>();

    public Map<String, Object> data = new HashMap<>();

    public enum Status {

        CREATED,

        IN_EXECUTION,

        FAILED,

        FINISHED;

    }

    public enum Type {

        NULL(null, 9),

        START_EVENT(MessageType.TOKEN_MSG, 1),

        END_EVENT(MessageType.SINGLETON_MSG, 0),

        SERVICE_TASK(MessageType.TOKEN_MSG, 1),

        SERVICE_TASK_COMPLETE(MessageType.SERVICE_TASK_MSG, 1),

        PARALLEL_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        PARALLEL_GATEWAY_CONVERGING(MessageType.SINGLETON_MSG, 1),

        EXCLUSIVE_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        EXCLUSIVE_GATEWAY_CONVERGING(MessageType.TOKEN_MSG, 1),

        INCLUSIVE_GATEWAY_DIVERGING(MessageType.TOKEN_MSG, -1),

        INCLUSIVE_GATEWAY_CONVERGING(MessageType.SINGLETON_MSG, 1),
        ;

        public final MessageType message;

        public final int next;

        Type(MessageType message, int next) {
            this.next = next;
            this.message = message;
        }

        public static Type valueOf(Node node) {
            String tmp = node.type.name();
            if (node instanceof Gateway) {
                Gateway pg = (Gateway) node;
                tmp = pg.type + "_" + pg.sequence;
            }
            return Type.valueOf(tmp);
        }

    }

}
