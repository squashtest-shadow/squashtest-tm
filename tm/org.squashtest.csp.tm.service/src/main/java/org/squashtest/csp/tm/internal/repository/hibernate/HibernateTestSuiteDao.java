/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.campaign.TestSuiteStatistics;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;

/* 
 * todo : make it a dynamic call
 * 
 */
@Repository
public class HibernateTestSuiteDao extends HibernateEntityDao<TestSuite> implements TestSuiteDao {

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
	public List<IterationTestPlanItem> findLaunchableTestPlan(final Long testSuiteId) {
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

	@Override
	public long countTestPlans(Long testSuiteId) {
		return (Long) executeEntityNamedQuery("testSuite.countTestPlans", idParameter(testSuiteId));
	}

	@Override
	public TestSuiteStatistics getTestSuiteStatistics(final Long testSuiteId) {

		Map<String, Integer> statusMap = new HashMap<String, Integer>();

		Long nbTestPlans = countTestPlans(testSuiteId);

		for (ExecutionStatus status : ExecutionStatus.values()) {
			final ExecutionStatus fStatus = status;

			SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

				@Override
				public void setQueryParameters(Query query) {
					query.setLong("id", testSuiteId);
					query.setLong("id2", testSuiteId);
					query.setParameter("status", fStatus);
				}
			};

			Long lResult = executeEntityNamedQuery("testSuite.countStatus", newCallBack);

			Integer result = lResult.intValue();

			statusMap.put(status.name(), result);
		}

		TestSuiteStatistics stats = new TestSuiteStatistics(nbTestPlans, statusMap.get(ExecutionStatus.BLOCKED.name()),
				statusMap.get(ExecutionStatus.FAILURE.name()), statusMap.get(ExecutionStatus.SUCCESS.name()),
				statusMap.get(ExecutionStatus.RUNNING.name()), statusMap.get(ExecutionStatus.READY.name()));

		return stats;
	}

	
	@Override
	public List<IterationTestPlanItem> findTestPlanPartition(final Long testSuiteId,
			final List<Long> testPlanItemIds) {
		
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
				query.setLong("id", id);
				query.setLong("id2", id);
			}
		};
		return newCallBack;
	}
	
}
