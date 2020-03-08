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

package org.lorislab.p6.process.mem.model;

import org.infinispan.protostream.annotations.ProtoField;
import org.lorislab.p6.process.stream.model.ProcessTokenStatusStream;
import org.lorislab.p6.process.stream.model.ProcessTokenTypeStream;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessToken {

    @ProtoField(number = 1)
    public String guid;

    @ProtoField(number = 2)
    public ProcessToken parent;

    @ProtoField(number = 3)
    public String referenceTokenGuid;

    @ProtoField(number = 4)
    public String startNodeName;

    @ProtoField(number = 5)
    public String createNodeName;

    @ProtoField(number = 6)
    public String nodeName;

    @ProtoField(number = 7)
    public String previousName;

    @ProtoField(number = 8)
    public ProcessTokenStatusStream status;

    @ProtoField(number = 9)
    public ProcessTokenTypeStream type;

    @ProtoField(number = 10)
    public Date finishedDate;

    @ProtoField(number = 11)
    public String processInstanceGuid;

    @ProtoField(number = 12)
    public String processId;

    @ProtoField(number = 13)
    public String processVersion;

    @ProtoField(number = 14)
    public Set<String> createdFrom = new HashSet<>();

    @ProtoField(number = 15)
    public Map<String, Object> data;
}
