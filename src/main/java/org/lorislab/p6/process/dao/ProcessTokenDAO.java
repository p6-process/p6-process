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

import org.lorislab.p6.process.dao.model.ProcessToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProcessTokenDAO {

    @Inject
    EntityManager em;

    public ProcessToken findByGuid(String id) {
        return em.find(ProcessToken.class, id);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void create(ProcessToken token) {
        em.persist(token);
        em.flush();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void createTokens(List<ProcessToken> tokens) {
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(em::merge);
            em.flush();
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProcessToken update(ProcessToken token) {
        ProcessToken tmp = em.merge(token);
        em.flush();
        return tmp;
    }

    public ProcessToken findByReferenceAndNodeName(String reference, String nodeName) {
        TypedQuery<ProcessToken> tq = em.createQuery("SELECT t FROM ProcessToken  t WHERE t.reference = :reference and t.nodeName = :nodeName", ProcessToken.class);
        List<ProcessToken> tmp = tq
                .setParameter("reference", reference)
                .setParameter("nodeName", nodeName)
                .getResultList();
        if (tmp != null && !tmp.isEmpty()) {
            if (tmp.size() == 1) {
                return tmp.get(0);
            }
            throw new IllegalStateException("To many results fro the findByReferenceAndNodeName. Size: " +  tmp.size());
        }
        return null;
    }

}
