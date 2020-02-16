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
import org.lorislab.quarkus.jel.jpa.model.Persistent;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "PROCESS_DEF",
        indexes = @Index(name = "PROCESS_DEF_IDX", columnList = "PROCESS_ID,PROCESS_VERSION", unique = true)
)
public class ProcessDefinition extends Persistent {

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @Column(name = "PROCESS_APP")
    private String application;

    @Column(name = "PROCESS_MODULE")
    private String module;

    @Column(name = "PROCESS_RESOURCE")
    private String resource;

    @Column(name = "PROCESS_URL")
    private String processUrl;

}
