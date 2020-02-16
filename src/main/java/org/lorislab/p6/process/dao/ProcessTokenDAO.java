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
import org.lorislab.p6.process.dao.model.ProcessToken_;
import org.lorislab.p6.process.dao.model.enums.ProcessTokenStatus;
import org.lorislab.quarkus.jel.jpa.exception.DAOException;
import org.lorislab.quarkus.jel.jpa.service.AbstractEntityDAO;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = DAOException.class)
public class ProcessTokenDAO extends AbstractEntityDAO<ProcessToken> {

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    public List<ProcessToken> findAllTokensInExecution(String processInstanceId, String parent) throws DAOException {
        try {
            CriteriaQuery<ProcessToken> cq = em.getCriteriaBuilder().createQuery(entityClass);
            Root<ProcessToken> root = cq.from(entityClass);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessToken_.PROCESS_INSTANCE_GUID), processInstanceId),
                    cb.equal(root.get(ProcessToken_.PARENT), parent),
                    cb.equal(root.get(ProcessToken_.STATUS), ProcessTokenStatus.IN_EXECUTION)
            );
            return em.createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ALL_TOKENS_IN_EXECUTION_AND_PREVIOUS, ex, processInstanceId, parent);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    public List<ProcessToken> findAllCreatedTokensCreate(String processInstanceId) throws DAOException {
        try {
            CriteriaQuery<ProcessToken> cq = em.getCriteriaBuilder().createQuery(entityClass);
            Root<ProcessToken> root = cq.from(entityClass);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessToken_.PROCESS_INSTANCE_GUID), processInstanceId),
                    cb.equal(root.get(ProcessToken_.STATUS), ProcessTokenStatus.CREATED)
            );
            return em.createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ALL_CREATED_TOKENS, ex, processInstanceId);
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public ProcessToken findByProcessInstanceIdAndNodeName(String processInstanceId, String nodeName) throws DAOException {
        try {
            CriteriaQuery<ProcessToken> cq = em.getCriteriaBuilder().createQuery(entityClass);
            Root<ProcessToken> root = cq.from(entityClass);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessToken_.PROCESS_INSTANCE_GUID), processInstanceId),
                    cb.equal(root.get(ProcessToken_.NODE_NAME), nodeName)
            );
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException no) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_PROCESS_INSTANCE_ID_AND_NODE_NAME, ex, processInstanceId, nodeName);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public ProcessToken findByReferenceAndCreateNodeName(String referenceTokenGuid, String createNodeName) throws DAOException {
        try {
            CriteriaQuery<ProcessToken> cq = em.getCriteriaBuilder().createQuery(entityClass);
            Root<ProcessToken> root = cq.from(entityClass);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessToken_.REFERENCE_TOKEN_GUID), referenceTokenGuid),
                    cb.equal(root.get(ProcessToken_.CREATE_NODE_NAME), createNodeName)
            );
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException no) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_REFERENCE_TOKEN, ex, referenceTokenGuid);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = DAOException.class)
    public ProcessToken findByProcessIdAndStartNodeName(String processInstanceId, String startNodeName) throws DAOException {
        try {
            CriteriaQuery<ProcessToken> cq = em.getCriteriaBuilder().createQuery(entityClass);
            Root<ProcessToken> root = cq.from(entityClass);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            cq.where(
                    cb.equal(root.get(ProcessToken_.PROCESS_INSTANCE_GUID), processInstanceId),
                    cb.equal(root.get(ProcessToken_.START_NODE_NAME), startNodeName)
            );
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException no) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_PROCESS_INSTANCE_ID_AND_START_NODE_NAME, ex, processInstanceId, startNodeName);
        }
    }

    enum ErrorKeys {

        ERROR_FIND_BY_REFERENCE_TOKEN,

        ERROR_FIND_BY_PROCESS_INSTANCE_ID_AND_NODE_NAME,

        ERROR_FIND_BY_PROCESS_INSTANCE_ID_AND_START_NODE_NAME,

        ERROR_FIND_ALL_TOKENS_IN_EXECUTION_AND_PREVIOUS,

        ERROR_FIND_ALL_CREATED_TOKENS,

        ;
    }
}
