/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.cms.persistence.jpa.impl.commands;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.kie.workbench.common.forms.cms.persistence.jpa.impl.JPATransactionHandler;

public class GetAllInstancesCommand extends AbstractPersistenceCommand<List<Object>> {

    private Class<?> type;

    public GetAllInstancesCommand(Class<?> type) {
        this.type = type;
    }

    @Override
    protected List<Object> doExecute(JPATransactionHandler transactionHandler) {
        EntityManager entityManager = transactionHandler.getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(type);

        Root rootEntry = criteriaQuery.from(type);

        CriteriaQuery<Object> all = criteriaQuery.select(rootEntry);

        TypedQuery<Object> allQuery = entityManager.createQuery(all);

        return allQuery.getResultList();
    }
}
