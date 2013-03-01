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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.service.internal.repository.CustomTestSuiteDao;

/*
 * todo : make it a dynamic call
 *
 */
@Repository("CustomTestSuiteDao")
public class HibernateTestSuiteDao extends HibernateEntityDao<TestSuite> implements CustomTestSuiteDao {

	@Override
	public List<TestSuite> findAllByIterationId(final long iterationId) {

		return executeListNamedQuery("testSuite.findAllByIterationId", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(0, iterationId);
			}

		});
	}

	@Override
	public List<IterationTestPlanItem> findLaunchableTestPlan(final long testSuiteId) {
		return executeListNamedQuery("testSuite.findLaunchableTestPlan", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(0, testSuiteId);
				query.setParameter(1, testSuiteId);
			}

		});
	}

	public List<IterationTestPlanItem> findTestPlanPaged(final long testSuiteId, final Paging paging) {
		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("id", testSuiteId);
				query.setParameter("id2", testSuiteId);
				query.setFirstResult(paging.getFirstItemIndex());
				query.setMaxResults(paging.getPageSize());
			}

		};

		return executeListNamedQuery("testSuite.findTestPlanPaged", callback);
	}

	private Long countTestPlanItems(long testSuiteId) {
		return (Long) executeEntityNamedQuery("TestSuite.countTestPlanItems", idParameter(testSuiteId));
	}

	@Override
	public TestPlanStatistics getTestSuiteStatistics(final long testSuiteId) {

		Map<String, Integer> statusMap = new HashMap<String, Integer>();

		fillStatusMapWithQueryResult(testSuiteId, statusMap);

		return new TestPlanStatistics(statusMap);
	}

	
	private void fillStatusMapWithQueryResult(final long testSuiteId, Map<String, Integer> statusMap) {
		//Add Total number of TestCases
		Integer nbTestPlans = countTestPlanItems(testSuiteId).intValue();
		statusMap.put(TestPlanStatistics.TOTAL_NUMBER_OF_TEST_CASE_KEY, nbTestPlans);
		
		//Add number of testCase for each ExecutionStatus
		SetQueryParametersCallback newCallBack = new IdId2ParameterCallback(testSuiteId);
		List<Object[]> result = executeListNamedQuery("testSuite.countStatuses", newCallBack);
		for (Object[] objTab : result) {
			statusMap.put(((ExecutionStatus) objTab[0]).name(), ((Long) objTab[1]).intValue());
		}
	}
	
	private static class IdId2ParameterCallback implements SetQueryParametersCallback {
		private long id;
		public IdId2ParameterCallback(long id) {
			this.id = id;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setLong("id", id);
			query.setLong("id2", id);
		}
	}

	@Override
	public List<IterationTestPlanItem> findTestPlanPartition(final long testSuiteId, final List<Long> testPlanItemIds) {

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("suiteId", testSuiteId);
				query.setParameterList("itemIds", testPlanItemIds, LongType.INSTANCE);
			}
		};

		return executeListNamedQuery("testSuite.findTestPlanPartition", callback);
	}

	private SetQueryParametersCallback idParameter(final long id) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong(0, id);
			}
		};
		return newCallBack;
	}

	@Override
	public List<Execution> findAllExecutionByTestSuite(long testSuiteId) {
		SetQueryParametersCallback callback = idParameter(testSuiteId);
		return executeListNamedQuery("testSuite.findAllExecutions", callback);
	}

}
