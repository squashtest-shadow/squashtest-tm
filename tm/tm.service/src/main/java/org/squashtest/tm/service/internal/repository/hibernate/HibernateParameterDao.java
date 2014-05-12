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

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.CustomParameterDao;

@Repository("CustomParameterDao")
public class HibernateParameterDao implements CustomParameterDao {

	@Inject
	private SessionFactory sessionFactory;
	

	public List<Parameter> findAllByTestCase(Long testcaseId) {
		
		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findAllByTestCase");
		query.setParameter("testCaseId", testcaseId);
		return (List<Parameter>) query.list();
	}


	@Override
	public List<Parameter> findAllByTestCases(List<Long> testcaseIds) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findAllByTestCases");
		query.setParameterList("testCaseIds", testcaseIds);
		return (List<Parameter>) query.list();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Parameter> findAllByNameAndTestCases(String name, List<Long> testcaseIds){

			Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findAllByNameAndTestCases");
			query.setParameter("name", name);
			query.setParameterList("testCaseIds", testcaseIds);
			return (List<Parameter>) query.list();
	}


	@Override
	public Parameter findParameterByNameAndTestCase(String name, Long testcaseId){
			
		Query query = sessionFactory.getCurrentSession().getNamedQuery("parameter.findParameterByNameAndTestCase");
		query.setParameter("name", name);
		query.setParameter("testCaseId", testcaseId);
		return (Parameter) query.uniqueResult();
	}
}
