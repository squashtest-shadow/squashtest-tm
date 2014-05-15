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

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;


@Repository
public class HibernateTestAutomationProjectDao implements
TestAutomationProjectDao {

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
		return (TestAutomationProject)query.uniqueResult();
	}



	@Override
	public TestAutomationProject findByExample(TestAutomationProject example) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TestAutomationProject.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("server", example.getServer()));

		List<?> res = criteria.list();

		if (res.isEmpty()){
			return null;
		}
		else if (res.size()==1){
			return (TestAutomationProject)res.get(0);
		}
		else{
			throw new NonUniqueEntityException();
		}
	}




}
