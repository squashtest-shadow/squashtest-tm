/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import java.util.Collections;
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
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;

@Repository
public class HibernateAutomatedTestDao implements AutomatedTestDao {

	@Inject
	private SessionFactory sessionFactory;

	@Override
	public void persist(AutomatedTest newTest) {
		if (findByExample(newTest) == null) {
			sessionFactory.getCurrentSession().persist(newTest);
		} else {
			throw new NonUniqueEntityException();
		}
	}

	@Override
	public AutomatedTest uniquePersist(AutomatedTest newTest) {
		if ((newTest.getId() != null) && (findById(newTest.getId()) != null)) {
			return newTest;
		}

		// content exists ?
		AutomatedTest baseTest = findByExample(newTest);
		
		if (baseTest != null) {
			return baseTest;
		} else { // or else, persist
			sessionFactory.getCurrentSession().persist(newTest);
			return newTest;
		}
	}

	@Override
	public AutomatedTest findById(Long testId) {
		Session session = sessionFactory.getCurrentSession();
		return (AutomatedTest) session.load(AutomatedTest.class, testId);
	}

	@Override
	public AutomatedTest findByExample(AutomatedTest example) {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AutomatedTest.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("project", example.getProject()));

		List<?> res = criteria.list();

		if (res.isEmpty()) {
			return null;
		} else if (res.size() == 1) {
			return (AutomatedTest) res.get(0);
		} else {
			throw new NonUniqueEntityException();
		}
	}
	
	@Override
	public List<AutomatedTest> findAllByExtenderIds(List<Long> extenderIds) {
		
		if (extenderIds.isEmpty()){
			return Collections.emptyList();
		}
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("automatedTest.findAllByExtenderIds");
		query.setParameterList("extenderIds", extenderIds, LongType.INSTANCE);
		return (List<AutomatedTest>) query.list();
		
	}
	
	@Override
	public List<AutomatedTest> findAllByExtender(Collection<AutomatedExecutionExtender> extenders) {
		
		if (extenders.isEmpty()){
			return Collections.emptyList();
		}
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("automatedTest.findAllByExtenders");
		query.setParameterList("extenders", extenders); 
		return (List<AutomatedTest>) query.list();
		
	}

}
