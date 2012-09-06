/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationTest;
import org.squashtest.csp.tm.internal.repository.TestAutomationTestDao;

@Repository
public class HibernateTestAutomationTestDao implements TestAutomationTestDao {

	@Inject
	private SessionFactory sessionFactory;
	
	
	@Override
	public void persist(TestAutomationTest newTest) {
		if (findByExample(newTest)==null){
			sessionFactory.getCurrentSession().persist(newTest);
		}else{
			throw new NonUniqueEntityException();
		}
	}

	@Override
	public TestAutomationTest uniquePersist(TestAutomationTest newTest) {
		if ((newTest.getId() != null) && (findById(newTest.getId())!=null)){
			return newTest;
		}
		
		//content exists ?
		TestAutomationTest baseTest = findByExample(newTest);
		if (baseTest != null){
			return baseTest;
		}
		
		//or else, persist
		else{
			sessionFactory.getCurrentSession().persist(newTest);
			return newTest;
		}
	}

	@Override
	public TestAutomationTest findById(Long testId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery("testAutomationTest.findById");
		query.setParameter("testId", testId);
		return (TestAutomationTest)query.uniqueResult();
	}

	@Override
	public TestAutomationTest findByExample(TestAutomationTest example) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TestAutomationTest.class);
		criteria = criteria.add(Example.create(example));
		criteria = criteria.add(Restrictions.eq("project", example.getProject()));

		List<?> res = criteria.list();
		
		if (res.isEmpty()){
			return null;
		}
		else if (res.size()==1){
			return (TestAutomationTest)res.get(0);
		}
		else{
			throw new NonUniqueEntityException();
		}
	}

}
