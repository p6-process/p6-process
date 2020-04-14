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

import org.lorislab.p6.process.dao.model.ProcessInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class ProcessInstanceDAO {

    @Inject
    EntityManager em;

    public ProcessInstance findByGuid(String id) {
        return em.find(ProcessInstance.class, id);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void create(ProcessInstance pi) {
        em.persist(pi);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProcessInstance update(ProcessInstance pi) {
        ProcessInstance tmp = em.merge(pi);
        em.flush();
        return tmp;
    }
}
