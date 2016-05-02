/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository.hibernate;

import static org.squashtest.tm.domain.execution.ExecutionStatus.READY;
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;

@Repository
public class HibernateAutomatedSuiteDao implements AutomatedSuiteDao {

	@PersistenceContext
	private EntityManager em;

	protected Session currentSession() {
		return em.unwrap(Session.class);
	}


	@Override
	public void delete(String id) {
		AutomatedSuite suite = findById(id);
		currentSession().delete(suite);
	}


	@Override
	public void delete(AutomatedSuite suite) {
		currentSession().delete(suite);
	}

	@Override
	public AutomatedSuite createNewSuite() {
		AutomatedSuite suite = new AutomatedSuite();
		currentSession().persist(suite);
		return suite;
	}

	@Override
	public AutomatedSuite findById(String id) {
		return em.getReference(AutomatedSuite.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutomatedSuite> findAll() {
		Query query = currentSession().getNamedQuery("automatedSuite.findAll");
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutomatedSuite> findAllByIds(Collection<String> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			Query query = currentSession().getNamedQuery("automatedSuite.findAllById");
			query.setParameterList("suiteIds", ids, StringType.INSTANCE);
			return query.list();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtenders(String suiteId) {
		Query query = currentSession().getNamedQuery("automatedSuite.findAllExtenders");
		query.setString("suiteId", suiteId);
		return query.list();
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllWaitingExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, READY);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllRunningExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, RUNNING);
	}

	@Override
	public Collection<AutomatedExecutionExtender> findAllCompletedExtenders(String suiteId) {
		return findAllExtendersByStatus(suiteId, ExecutionStatus.getTerminatedStatusSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(final String suiteId,
			final Collection<ExecutionStatus> statusList) {

		Query query = currentSession().getNamedQuery("automatedSuite.findAllExtendersHavingStatus");

		query.setString("suiteId", suiteId);

		query.setParameterList("statusList", statusList);

		return query.list();
	}

	public Collection<AutomatedExecutionExtender> findAllExtendersByStatus(String suiteId,
			ExecutionStatus... statusArray) {
		Collection<ExecutionStatus> statusList = Arrays.asList(statusArray);
		return findAllExtendersByStatus(suiteId, statusList);

	}

}
