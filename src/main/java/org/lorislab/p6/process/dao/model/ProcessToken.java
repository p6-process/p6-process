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
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenType;
import org.lorislab.quarkus.jel.jpa.model.Persistent;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "PROCESS_TOKEN",
        uniqueConstraints = {
                @UniqueConstraint(name = "TOKEN_UC_1", columnNames = {"PROCESS_INSTANCE_GUID", "START_NODE_NAME"}),
                @UniqueConstraint(name = "TOKEN_UC_REF", columnNames = {"PROCESS_INSTANCE_GUID", "REF_TOKEN_GUID", "CREATE_NODE_NAME"}),
                @UniqueConstraint(name = "TOKEN_UC_CHILD", columnNames = {"PROCESS_INSTANCE_GUID", "PARENT_TOKEN_GUID", "CREATE_NODE_NAME"})
        })
public class ProcessToken extends Persistent {

    @Column(name = "PARENT_TOKEN_GUID")
    private String parent;

    @Column(name = "REF_TOKEN_GUID")
    private String referenceTokenGuid;

    @Column(name = "START_NODE_NAME")
    private String startNodeName;

    @Column(name = "CREATE_NODE_NAME")
    private String createNodeName;

    @Column(name = "NODE_NAME")
    private String nodeName;

    @Column(name = "NODE_PREVIOUS")
    private String previousName;

    @Column(name = "TOKEN_STATUS")
    @Enumerated(EnumType.STRING)
    private ProcessTokenStatus status;

    @Column(name = "TOKEN_TYPE")
    @Enumerated(EnumType.STRING)
    private ProcessTokenType type;

    @Column(name = "FINISHED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishedDate;

    @Column(name = "PROCESS_INSTANCE_GUID")
    private String processInstanceGuid;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "CREATED_FROM",
            joinColumns = @JoinColumn(name = "TOKEN_GUID", foreignKey = @ForeignKey(name = "FK_TOKEN_GUID"))
    )
    private Set<String> createdFrom = new HashSet<>();

}
