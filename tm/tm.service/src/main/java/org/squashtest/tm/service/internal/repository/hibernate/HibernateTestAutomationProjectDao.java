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

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;

@Repository
public class HibernateTestAutomationProjectDao implements TestAutomationProjectDao {

	@PersistenceContext
	private EntityManager em;

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#persist(TestAutomationProject)
	 */
	@Override
	public void persist(TestAutomationProject newProject) {
		em.unwrap(Session.class).persist(newProject);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findById(Long)
	 */
	@Override
	public TestAutomationProject findById(Long id) {
		return em.getReference(TestAutomationProject.class, id);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findByExample(TestAutomationProject)
	 */
	@Override
	public TestAutomationProject findByExample(TestAutomationProject example) {
		Criteria criteria = em.unwrap(Session.class).createCriteria(TestAutomationProject.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("server", example.getServer()));

		List<?> res = criteria.list();

		if (res.isEmpty()) {
			return null;
		} else if (res.size() == 1) {
			return (TestAutomationProject) res.get(0);
		} else {
			throw new NonUniqueEntityException();
		}
	}
	@Override
	public Collection<Long> findAllByTMProject(long tmProjectId) {
		Session session = em.unwrap(Session.class);
		Query query = session.getNamedQuery("testAutomationProject.findAllByTMPRoject");
		query.setParameter("tmProjectId", tmProjectId);
		return query.list();
	}





	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#haveExecutedTestsByIds(Collection)
	 */
	@Override
	public boolean haveExecutedTestsByIds(Collection<Long> projectIds) {
		if (projectIds.isEmpty()) {
			return false;
		}

		Query q = em.unwrap(Session.class).getNamedQuery("testAutomationProject.haveExecutedTestsByIds");
		q.setParameterList(ParameterNames.PROJECT_IDS, projectIds);
		int count = ((Long) q.iterate().next()).intValue();
		return count > 0;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteProjectsByIds(Collection)
	 */
	@Override
	public void deleteProjectsByIds(Collection<Long> projectIds) {
		if (! projectIds.isEmpty()){
			dereferenceAutomatedExecutionExtender(projectIds);
			dereferenceTestCases(projectIds);
			em.unwrap(Session.class).flush();
			deleteAutomatedTests(projectIds);
			deleteTestAutomationProjects(projectIds);
			em.unwrap(Session.class).flush();
		}
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteAllHostedProjects(long)
	 */
	@Override
	public void deleteAllHostedProjects(long serverId) {
		List<Long> hostedProjectIds = findHostedProjectIds(serverId);
		deleteProjectsByIds(hostedProjectIds);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findAllHostedProjects(long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TestAutomationProject> findAllHostedProjects(long serverId) {
		Session session = em.unwrap(Session.class);
		Query query = session.getNamedQuery("testAutomationServer.findAllHostedProjects");
		query.setParameter(ParameterNames.SERVER_ID, serverId);
		return query.list();
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findHostedProjectIds(long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findHostedProjectIds(long serverId) {
		Query q = em.unwrap(Session.class).getNamedQuery("testAutomationProject.findHostedProjectIds");
		q.setParameter(ParameterNames.SERVER_ID, serverId);
		return q.list();
	}

	// ************************ private stuffs **********************************

	private void dereferenceAutomatedExecutionExtender(Collection<Long> projectIds) {
		Query q = em.unwrap(Session.class).getNamedQuery(
				"testAutomationProject.dereferenceAutomatedExecutionExtender");
		q.setParameterList(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void dereferenceTestCases(Collection<Long> projectIds) {
		Query q = em.unwrap(Session.class).getNamedQuery("testAutomationProject.dereferenceTestCases");
		q.setParameterList(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void deleteAutomatedTests(Collection<Long> projectIds) {
		Query q = em.unwrap(Session.class).getNamedQuery("testAutomationProject.deleteAutomatedTests");
		q.setParameterList(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

	private void deleteTestAutomationProjects(Collection<Long> projectIds) {
		Query q = em.unwrap(Session.class).getNamedQuery("testAutmationProject.delete");
		q.setParameterList(ParameterNames.PROJECT_IDS, projectIds);
		q.executeUpdate();
	}

}
