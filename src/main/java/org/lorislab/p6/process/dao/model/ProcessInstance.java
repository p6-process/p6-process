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

import lombok.Getter;
import lombok.Setter;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;
import org.lorislab.quarkus.jel.jpa.model.Persistent;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "PROCESS_INSTANCE")
public class ProcessInstance extends Persistent {

    @Column(name = "PROCESS_PARENT_GUID")
    private String processInstanceParentGuid;

    @Column(name = "PROCESS_DEF_GUID")
    private String processDefinitionGuid;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private ProcessInstanceStatus status;

}