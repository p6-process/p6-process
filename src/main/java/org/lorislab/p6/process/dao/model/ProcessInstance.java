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
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;

import java.util.HashMap;
import java.util.Map;

public class ProcessInstance {

    @ProtoField(number = 1)
    public String guid;

    @ProtoField(number = 2)
    public String parentGuid;

    @ProtoField(number = 3)
    public String messageId;

    @ProtoField(number = 4)
    public String processId;

    @ProtoField(number = 5)
    public String processVersion;

    @ProtoField(number = 6)
    public ProcessInstanceStatus status;

    @ProtoField(number = 7)
    public Map<String, Object> data = new HashMap<>();
}
