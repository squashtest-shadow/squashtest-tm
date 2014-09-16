/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.internal.repository.CustomDatasetDao;

@Repository("CustomDatasetDao")
public class HibernateDatasetDao extends HibernateEntityDao<Dataset> implements CustomDatasetDao {

	@Inject
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findOwnDatasetsByTestCase(Long testCaseId) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findOwnDatasetsByTestCase");
		query.setParameter("testCaseId", testCaseId);
		return (List<Dataset>) query.list();
	}



	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findOwnDatasetsByTestCases(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findOwnDatasetsByTestCases");
			query.setParameterList("testCaseIds", testCaseIds);
			return (List<Dataset>) query.list();
		} else {
			return Collections.emptyList();
		}
	}


	@Override
	public List<Dataset> findImmediateDelegateDatasets(Long testCaseId) {

		Query q = sessionFactory.getCurrentSession().getNamedQuery("dataset.findTestCasesThatInheritParameters");
		q.setParameter("srcIds", LongType.INSTANCE);

		List<Long> tcids = q.list();

		return findOwnDatasetsByTestCases(tcids);
	}

	@Override
	public List<Dataset> findAllDelegateDatasets(Long testCaseId) {
		List<Dataset> allDatasets = new LinkedList<Dataset>();

		Set<Long> exploredTc = new HashSet<Long>();
		List<Long> srcTc = new LinkedList<Long>();
		List<Long> destTc;

		Query next = sessionFactory.getCurrentSession().getNamedQuery("dataset.findTestCasesThatInheritParameters");

		srcTc.add(testCaseId);

		while(! srcTc.isEmpty()){

			next.setParameterList("srcIds", srcTc, LongType.INSTANCE);
			destTc = next.list();

			if (! destTc.isEmpty()){
				allDatasets.addAll( findOwnDatasetsByTestCases(destTc) );
			}

			exploredTc.addAll(srcTc);
			srcTc = destTc;
			srcTc.removeAll(exploredTc);

		}

		return allDatasets;
	}


	@Override
	public List<Dataset> findOwnAndDelegateDatasets(Long testCaseId) {
		List<Dataset> allDatasets = findOwnDatasetsByTestCase(testCaseId);
		allDatasets.addAll(findAllDelegateDatasets(testCaseId));
		return allDatasets;
	}




	@Override
	public Dataset findDatasetByTestCaseAndByName(Long testCaseId, String name) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.findDatasetsByTestCaseAndByName");
		query.setParameter("testCaseId", testCaseId);
		query.setParameter("name", name);
		return (Dataset) query.uniqueResult();
	}

	@Override
	public void removeDatasetFromTestPlanItems(Long datasetId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("dataset.removeDatasetFromItsIterationTestPlanItems");
		query.setParameter("datasetId", datasetId);
		query.executeUpdate();

		Query query2 = sessionFactory.getCurrentSession().getNamedQuery("dataset.removeDatasetFromItsCampaignTestPlanItems");
		query2.setParameter("datasetId", datasetId);
		query2.executeUpdate();
	}
}
