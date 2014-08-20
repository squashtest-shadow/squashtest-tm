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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.CustomParameterDao;

@Repository("CustomParameterDao")
public class HibernateParameterDao implements CustomParameterDao {

	@Inject
	private SessionFactory sessionFactory;


	public List<Parameter> findOwnParametersByTestCase(Long testcaseId) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findOwnParameters");
		query.setParameter("testCaseId", testcaseId);
		return (List<Parameter>) query.list();
	}


	@Override
	public List<Parameter> findOwnParametersByTestCases(List<Long> testcaseIds) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findOwnParametersForList");
		query.setParameterList("testCaseIds", testcaseIds);
		return (List<Parameter>) query.list();
	}

	@Override
	public List<Parameter> findAllParametersByTestCase(Long testcaseId) {

		List<Parameter> allParameters = new LinkedList<Parameter>();

		Set<Long> exploredTc = new HashSet<Long>();
		List<Long> srcTc = new LinkedList<Long>();
		List<Long> destTc;

		Query next = sessionFactory.getCurrentSession().getNamedQuery("parameter.findTestCasesThatDelegatesParameters");

		srcTc.add(testcaseId);

		while(! srcTc.isEmpty()){

			allParameters.addAll( findOwnParametersByTestCases(srcTc));

			next.setParameterList("srcIds", srcTc, LongType.INSTANCE);
			destTc = next.list();

			exploredTc.addAll(srcTc);
			srcTc = destTc;
			srcTc.removeAll(exploredTc);

		}

		return allParameters;

	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Parameter> findOwnParametersByNameAndTestCases(String name, List<Long> testcaseIds){

		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findOwnParametersByNameAndTestCases");
		query.setParameter("name", name);
		query.setParameterList("testCaseIds", testcaseIds);
		return (List<Parameter>) query.list();
	}


	@Override
	public Parameter findOwnParameterByNameAndTestCase(String name, Long testcaseId){

		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findOwnParametersByNameAndTestCase");
		query.setParameter("name", name);
		query.setParameter("testCaseId", testcaseId);
		return (Parameter) query.uniqueResult();
	}
}
