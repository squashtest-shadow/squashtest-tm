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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.internal.repository.CustomDatasetDao;

@Repository("CustomDatasetDao")
public class HibernateDatasetDao extends HibernateEntityDao<Dataset> implements CustomDatasetDao {

	@Inject
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findAllDatasetsByTestCase(Long testCaseId) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findAllDatasetsByTestCase");
		query.setParameter("testCaseId", testCaseId);
		return (List<Dataset>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findAllDatasetsByTestCases(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findAllDatasetsByTestCases");
			query.setParameterList("testCaseIds", testCaseIds);
			return (List<Dataset>) query.list();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Dataset findDatasetByTestCaseAndByName(Long testCaseId, String name) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findAllDatasetsByTestCaseAndByName");
		query.setParameter("testCaseId", testCaseId);
		query.setParameter("name", name);
		return (Dataset) query.uniqueResult();
	}

	@Override
	public void removeDatasetFromIterationTestPlanItems(Long datasetId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.removeDatasetFromItsIterationTestPlanItems");
		query.setParameter("datasetId", datasetId);
		query.executeUpdate();
	}
}
