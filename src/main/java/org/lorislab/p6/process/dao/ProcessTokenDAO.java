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

package org.lorislab.p6.process.dao;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProcessTokenDAO implements PanacheMongoRepositoryBase<ProcessToken, String> {

    public ProcessToken findByGuid(String id) {
        return findById(id);
    }

    public void create(ProcessToken token) {
        persist(token);
    }

    public ProcessToken findByReferenceAndNodeName(String reference, String nodeName) {
        List<ProcessToken> tmp = find("reference = ?1 and nodeName = ?2", reference, nodeName).list();
        if (tmp != null && !tmp.isEmpty()) {
            if (tmp.size() == 1) {
                return tmp.get(0);
            }
            throw new IllegalStateException("To many results fro the findByReferenceAndNodeName. Size: " +  tmp.size());
        }
        return null;
    }

}
