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

	@Override
	public void persist(TestAutomationProject newProject) {
		sessionFactory.getCurrentSession().persist(newProject);
	}

	@Override
	public TestAutomationProject findById(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationProject.findById");
		query.setParameter("projectId", id);
		return (TestAutomationProject) query.uniqueResult();
	}

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

	@Override
	public void deleteProjects(Collection<TestAutomationProject> projects) {

		dereferenceAutomatedExecutionExtender(projects);
		dereferenceTestCases(projects);

		deleteAutomatedTests(projects);
		deleteAutomatedTests(projects);
		deleteTestAutomationProjects(projects);

	}

	@Override
	public void deleteProjectsByIds(Collection<Long> projectIds) {
		deleteProjects(findAllByIds(projectIds));
	}

	// ************************ private stuffs **********************************

	private void dereferenceAutomatedExecutionExtender(Collection<TestAutomationProject> projects) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery(
				"testAutomationProject.dereferenceAutomatedExecutionExtender");
		q.setParameterList("projects", projects);
		q.executeUpdate();
	}

	private void dereferenceTestCases(Collection<TestAutomationProject> projects) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.dereferenceTestCases");
		q.setParameterList("projects", projects);
		q.executeUpdate();
	}

	private void deleteAutomatedTests(Collection<TestAutomationProject> projects) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.deleteAutomatedTests");
		q.setParameterList("projects", projects);
		q.executeUpdate();
	}

	private void deleteTestAutomationProjects(Collection<TestAutomationProject> projects) {
		for (TestAutomationProject entity : projects) {
			sessionFactory.getCurrentSession().delete(entity);
		}
	}

	private Collection<TestAutomationProject> findAllByIds(Collection<Long> ids) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("testAutomationProject.findAllByIds");
		q.setParameterList("projectIds", ids, LongType.INSTANCE);
		return q.list();
	}
}
