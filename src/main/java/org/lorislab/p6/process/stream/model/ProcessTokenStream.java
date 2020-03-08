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

package org.lorislab.p6.process.stream.model;

import java.util.*;

public class ProcessTokenStream {

    public String guid = UUID.randomUUID().toString();

    public ProcessTokenStream parent;

    public String referenceTokenGuid;

    public String startNodeName;

    public String createNodeName;

    public String nodeName;

    public String previousName;

    public ProcessTokenStatusStream status;

    public ProcessTokenTypeStream type;

    public Date finishedDate;

    public String processInstanceGuid;

    public String processId;

    public String processVersion;

    public Set<String> createdFrom = new HashSet<>();

    public Map<String, Object> data;
}
