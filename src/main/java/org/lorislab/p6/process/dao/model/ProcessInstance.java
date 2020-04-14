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

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.lorislab.p6.process.dao.model.enums.ProcessInstanceStatus;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Entity
@Table(name = "PROCESS_INSTANCE")
@Getter
@Setter
public class ProcessInstance {

    @Id
    private String id;

    @Version
    @Column(name="OPTLOCK")
    private Integer version;

    private String messageId;

    private String parent;

    private String processId;

    private String processVersion;

    @Enumerated(EnumType.STRING)
    private ProcessInstanceStatus status;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data = new HashMap<>();

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + id;
    }
}
