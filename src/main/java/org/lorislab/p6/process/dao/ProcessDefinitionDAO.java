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

import org.lorislab.p6.process.dao.model.ProcessDefinition;
import org.lorislab.p6.process.dao.model.ProcessDefinition_;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.lorislab.quarkus.jel.jpa.service.AbstractEntityDAO;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class ProcessDefinitionDAO extends AbstractEntityDAO<ProcessDefinition> {

    @Transactional(value = Transactional.TxType.SUPPORTS, rollbackOn = DAOException.class)
    public ProcessDefinition findByProcessIdAndProcessVersion(String processId, String processVersion) {
        try {
            CriteriaQuery<ProcessDefinition> cq = em.getCriteriaBuilder().createQuery(this.entityClass);
            Root<ProcessDefinition> root = cq.from(ProcessDefinition.class);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessDefinition_.PROCESS_ID), processId),
                    cb.equal(root.get(ProcessDefinition_.PROCESS_VERSION), processVersion)
            );
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException no) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_PROCESS_DEFINITION_BY_PROCESS_AND_VERSION, ex, processId, processVersion);
        }
    }

    public enum ErrorKeys {

        ERROR_FIND_PROCESS_DEFINITION_BY_PROCESS_AND_VERSION;
    }
}
