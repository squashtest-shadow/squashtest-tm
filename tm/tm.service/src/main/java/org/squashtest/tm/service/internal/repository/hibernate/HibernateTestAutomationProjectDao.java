/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;

@Repository
public class HibernateTestAutomationProjectDao implements TestAutomationProjectDao {

	@Inject
	private SessionFactory sessionFactory;
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#persist(TestAutomationProject)
	 */
	@Override
	public void persist(TestAutomationProject newProject) {
		sessionFactory.getCurrentSession().persist(newProject);
	}
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findById(Long)
	 */
	@Override
	public TestAutomationProject findById(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationProject.findById");
		query.setParameter("projectId", id);
		return (TestAutomationProject) query.uniqueResult();
	}
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#findByExample(TestAutomationProject)
	 */
	@Override
	public TestAutomationProject findByExample(TestAutomationProject example) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TestAutomationProject.class);
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
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#haveExecutedTests(Collection)
	 */
	@Override
	public Collection<TestAutomationProject> findAllByTMProject(long tmProjectId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationProject.findAllByTMPRoject");
		query.setParameter("tmProjectId", tmProjectId);
		return query.list();
	}

	@Override
	public boolean haveExecutedTests(Collection<TestAutomationProject> projects) {
		if (projects.isEmpty()) {
			return false;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.haveExecutedTests");
		q.setParameterList("projects", projects, LongType.INSTANCE);
		int count = ((Integer) q.iterate().next()).intValue();
		return (count > 0);
	}
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#haveExecutedTestsByIds(Collection)
	 */
	@Override
	public boolean haveExecutedTestsByIds(Collection<Long> projectIds) {
		if (projectIds.isEmpty()) {
			return false;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.haveExecutedTestsByIds");
		q.setParameterList("projectIds", projectIds);
		int count = ((Integer) q.iterate().next()).intValue();
		return (count > 0);
	}
	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteProjectsByIds(Collection)
	 */
	@Override
	public void deleteProjectsByIds(Collection<Long> projectIds) {
		dereferenceAutomatedExecutionExtender(projectIds);
		dereferenceTestCases(projectIds);
		sessionFactory.getCurrentSession().flush();
		deleteAutomatedTests(projectIds);
		deleteTestAutomationProjects(projectIds);
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationProjectDao#deleteAllHostedProjects(long)
	 */
	@Override
	public void deleteAllHostedProjects(long serverId) {
		List<Long> hostedProjectIds = findHostedProjectIds(serverId);
		deleteProjectsByIds(hostedProjectIds);
	}

	@SuppressWarnings("unchecked")
	private List<Long> findHostedProjectIds(long serverId) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.findHostedProjectIds");
		q.setParameter("serverId", serverId);
		return q.list();
	}

	// ************************ private stuffs **********************************

	private void dereferenceAutomatedExecutionExtender(Collection<Long> projectIds) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"testAutomationProject.dereferenceAutomatedExecutionExtender");
		q.setParameterList("projectIds", projectIds);
		q.executeUpdate();
	}

	private void dereferenceTestCases(Collection<Long> projectIds) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.dereferenceTestCases");
		q.setParameterList("projectIds", projectIds);
		q.executeUpdate();
	}

	private void deleteAutomatedTests(Collection<Long> projectIds) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.deleteAutomatedTests");
		q.setParameterList("projectIds", projectIds);
		q.executeUpdate();
	}

	private void deleteTestAutomationProjects(Collection<Long> projectIds) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutmationProject.delete");
		q.setParameterList("projectIds", projectIds);
		q.executeUpdate();
	}

}
