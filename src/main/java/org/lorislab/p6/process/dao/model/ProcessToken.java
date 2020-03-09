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

import org.infinispan.protostream.annotations.ProtoField;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessToken {

    @ProtoField(number = 1)
    public String guid;

    @ProtoField(number = 2)
    public String messageId;

    @ProtoField(number = 3)
    public String processInstance;

    @ProtoField(number = 4)
    public String processId;

    @ProtoField(number = 5)
    public String processVersion;

    @ProtoField(number = 6)
    public String nodeName;

    @ProtoField(number = 7)
    public ProcessTokenStatus status;

    @ProtoField(number = 8)
    public ProcessTokenType type;

    @ProtoField(number = 9)
    public String executionId;

    @ProtoField(number = 10)
    public String parent;

    @ProtoField(number = 11)
    public Set<String> createdFrom = new HashSet<>();

    @ProtoField(number = 12)
    public Map<String, Object> data = new HashMap<>();


//    private String referenceTokenGuid;
//
//    private String startNodeName;
//
//    private String createNodeName;

//
//    private String previousName;

//
//    private Date finishedDate;


//    private Set<String> createdFrom = new HashSet<>();

}
