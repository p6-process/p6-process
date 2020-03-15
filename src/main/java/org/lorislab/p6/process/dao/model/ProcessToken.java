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

import lombok.ToString;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessToken {

    public String guid;

    public String messageId;

    public String processInstance;

    public String processId;

    public String processVersion;

    public String nodeName;

    public ProcessTokenStatus status;

    public ProcessTokenType type;

    public String executionId;

    public String parent;

    public String reference;

    public Set<String> createdFrom = new HashSet<>();

    public Map<String, Object> data = new HashMap<>();

}
