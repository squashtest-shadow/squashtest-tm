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
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;

@Repository
public class HibernateAutomatedTestDao implements AutomatedTestDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {

		if (newTest.getId() != null && findById(newTest.getId()) != null) {
			return newTest;
		}

		AutomatedTest persisted = findByExample(newTest);
		if (persisted != null){
			return persisted;
		}
		else{
			em.unwrap(Session.class).persist(newTest);
			return newTest;
		}
	}




	@Override
	public void removeIfUnused(AutomatedTest test) {

		AutomatedTest persisted;

		if (test == null){
			return;
		}
		else if (test.getId() != null){
			persisted = test;
		}
		else{
			persisted = findByExample(test);
		}

		if (countReferences(persisted.getId()) == 0L){
			em.unwrap(Session.class).delete(persisted);
		}

	}


	@Override
	public void pruneOrphans(){
		Session session = em.unwrap(Session.class);

		Collection<AutomatedTest> orphans = session.getNamedQuery("automatedTest.findOrphans").list();

		if (orphans.isEmpty()){
			return;
		}

		Query q = session.getNamedQuery("automatedTest.bulkDelete");
		q.setParameterList("tests", orphans);
		q.executeUpdate();

	}


	@Override
	public long countReferences(long testId) {
		Session session = em.unwrap(Session.class);

		Query qCountTC = session.getNamedQuery("automatedTest.countReferencesByTestCases");
		qCountTC.setParameter("autoTestId", testId);
		long countTC = (Long)qCountTC.uniqueResult();

		Query qCountExt = session.getNamedQuery("automatedTest.countReferencesByExecutions");
		qCountExt.setParameter("autoTestId", testId);
		long countExt = (Long)qCountExt.uniqueResult();

		return countTC + countExt;
	}


	@Override
	public AutomatedTest findById(Long testId) {
		Session session = em.unwrap(Session.class);
		return (AutomatedTest) session.load(AutomatedTest.class, testId);
	}

	@Override
	public List<AutomatedTest> findByTestCases(Collection<Long> testCaseIds) {
		if (testCaseIds.isEmpty()){
			return Collections.emptyList();
		}

		Query query = em.unwrap(Session.class).getNamedQuery("automatedTest.findByTestCase");
		query.setParameter("testCaseIds", testCaseIds);
		return query.list();
	}

	@Override
	public AutomatedTest findByExample(AutomatedTest example) {

		Criteria criteria = em.unwrap(Session.class).createCriteria(AutomatedTest.class);
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

		Query query = em.unwrap(Session.class).getNamedQuery("automatedTest.findAllByExtenderIds");
		query.setParameterList("extenderIds", extenderIds, LongType.INSTANCE);
		return query.list();

	}

	@Override
	public List<AutomatedTest> findAllByExtender(Collection<AutomatedExecutionExtender> extenders) {

		if (extenders.isEmpty()){
			return Collections.emptyList();
		}

		Query query = em.unwrap(Session.class).getNamedQuery("automatedTest.findAllByExtenders");
		query.setParameterList("extenders", extenders);
		return query.list();

	}


}
