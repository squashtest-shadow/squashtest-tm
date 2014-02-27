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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.ExecutionDao;

@Repository
public class HibernateExecutionDao extends HibernateEntityDao<Execution> implements ExecutionDao {

	/*
	 * as long as the ordering of a collection is managed by @OrderColumn, but you can't explicitely reference the
	 * ordering column in the join table, initialize the collection itself is the only solution
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.internal.repository.ExecutionDao#findOrderedExecutionStepsByExecutionId(long)
	 */
	@Override
	public List<ExecutionStep> findExecutionSteps(long executionId) {
		Execution execution = findById(executionId);
		List<ExecutionStep> toReturn = new LinkedList<ExecutionStep>();

		for (ExecutionStep step : execution.getSteps()) {
			Hibernate.initialize(step);
			toReturn.add(step);
		}

		return toReturn;
	}

	@Override
	public Execution findAndInit(long executionId) {
		Execution execution = findById(executionId);
		Hibernate.initialize(execution.getReferencedTestCase());
		Hibernate.initialize(execution.getSteps());
		return execution;
	}

	@Override
	public int findExecutionRank(long executionId) {
		IterationTestPlanItem testPlan = (IterationTestPlanItem) currentSession()
				.createCriteria(IterationTestPlanItem.class).createCriteria("executions")
				.add(Restrictions.eq("id", executionId)).uniqueResult();

		int index = 0;
		for (Execution execution : testPlan.getExecutions()) {
			if (execution.getId().equals(executionId)) {
				return index;
			}
			index++;
		}
		return index;
	}

	@Override
	public ExecutionStatusReport getStatusReport(final long executionId) {

		ExecutionStatusReport report = new ExecutionStatusReport();

		Map<String, Integer> statusMap = new HashMap<String, Integer>();

		for (ExecutionStatus status : ExecutionStatus.values()) {
			final ExecutionStatus fStatus = status;

			SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId, fStatus);

			Long lResult = executeEntityNamedQuery("execution.countStatus", newCallBack);

			Integer result = lResult.intValue();

			statusMap.put(status.name(), result);
		}

		report.setUntestable(statusMap.get(ExecutionStatus.UNTESTABLE.name()));
		report.setBloqued(statusMap.get(ExecutionStatus.BLOCKED.name()));
		report.setFailure(statusMap.get(ExecutionStatus.FAILURE.name()));
		report.setSuccess(statusMap.get(ExecutionStatus.SUCCESS.name()));
		report.setRunning(statusMap.get(ExecutionStatus.RUNNING.name()));
		report.setReady(statusMap.get(ExecutionStatus.READY.name()));
		report.setWarning(statusMap.get(ExecutionStatus.WARNING.name()));
		report.setError(statusMap.get(ExecutionStatus.ERROR.name()));

		return report;
	}

	@Override
	public long countSuccess(final long executionId) {
		SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId, ExecutionStatus.SUCCESS);
		
		return (Long)executeEntityNamedQuery("execution.countStatus", newCallBack); 
	}

	@Override
	public long countReady(final long executionId) {
		SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId, ExecutionStatus.READY); 
			
		return (Long)executeEntityNamedQuery("execution.countStatus", newCallBack);
	}

	/*
	 * same than for HibernateTestCaseDao#findStepsByIdFiltered :
	 * 
	 * because we need to get the ordered list and we can't access the join table to sort them (again), we can't use the
	 * Criteria API. So we're playing it old good java here.
	 */

	@Override
	public List<ExecutionStep> findStepsFiltered(final Long executionId, final Paging filter) {

		Execution execution = findById(executionId);
		int listSize = execution.getSteps().size();

		int startIndex = filter.getFirstItemIndex();
		int lastIndex = filter.getFirstItemIndex() + filter.getPageSize();

		// prevent IndexOutOfBoundException :
		if (startIndex >= listSize) {
			return new LinkedList<ExecutionStep>(); // ie resultset is empty
		}
		if (lastIndex >= listSize) {
			lastIndex = listSize;
		}

		return execution.getSteps().subList(startIndex, lastIndex);

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ExecutionStep> findAllExecutionStepsWithStatus(Long projectId, ExecutionStatus executionStatus) {

		Criteria crit = currentSession().createCriteria(ExecutionStep.class, "ExecutionStep");
		crit.createAlias("execution", "Execution", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Execution.testPlan.iteration", "Iteration", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Iteration.campaign", "Campaign", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Campaign.project", "Project", JoinType.LEFT_OUTER_JOIN);
		crit.add(Restrictions.eq("Project.id", Long.valueOf(projectId)));
		crit.add(Restrictions.eq("executionStatus", executionStatus));
		
		return crit.list();
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IterationTestPlanItem> findAllIterationTestPlanItemsWithStatus(Long projectId, ExecutionStatus executionStatus) {

		Criteria crit = currentSession().createCriteria(IterationTestPlanItem.class, "TestPlan");
		crit.createAlias("iteration", "Iteration", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Iteration.campaign", "Campaign", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Campaign.project", "Project", JoinType.LEFT_OUTER_JOIN);
		crit.add(Restrictions.eq("Project.id", Long.valueOf(projectId)));
		crit.add(Restrictions.eq("executionStatus", executionStatus));
		
		return crit.list();
	};
	
	
	@Override
	public boolean hasStepOrExecutionWithStatus(long projectId, ExecutionStatus executionStatus) {
		Criteria crit = currentSession().createCriteria(ExecutionStep.class, "ExecutionStep");
		crit.createAlias("execution", "Execution", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Execution.testPlan.iteration", "Iteration", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Iteration.campaign", "Campaign", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Campaign.project", "Project", JoinType.LEFT_OUTER_JOIN);
		crit.add(Restrictions.eq("Project.id", Long.valueOf(projectId)));
		crit.add(Restrictions.eq("executionStatus", executionStatus));
		int stepResult = crit.list().size();
		
		Criteria execs = currentSession().createCriteria(Execution.class, "Execution");
		execs.createAlias("testPlan", "TestPlan", JoinType.LEFT_OUTER_JOIN);
		execs.createAlias("TestPlan.iteration", "Iteration", JoinType.LEFT_OUTER_JOIN);
		execs.createAlias("Iteration.campaign", "Campaign", JoinType.LEFT_OUTER_JOIN);
		execs.createAlias("Campaign.project", "Project", JoinType.LEFT_OUTER_JOIN);
		execs.add(Restrictions.eq("Project.id", Long.valueOf(projectId)));
		execs.add(Restrictions.eq("TestPlan.executionStatus", executionStatus));
		
		int execResult = execs.list().size();
		
		return  stepResult + execResult > 0;
	}
	

	@Override
	public List<IssueDetector> findAllIssueDetectorsForExecution(Long execId) {
		Execution execution = findById(execId);
		List<ExecutionStep> steps = execution.getSteps();
		List<IssueDetector> issueDetectors = new ArrayList<IssueDetector>(steps.size() + 1);
		issueDetectors.add(execution);
		issueDetectors.addAll(steps);
		return issueDetectors;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.service.internal.repository.ExecutionDao#countExecutionSteps(long)
	 */
	@Override
	public long countExecutionSteps(long executionId) {
		return (Long) executeEntityNamedQuery("execution.countSteps", "executionId", executionId);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.ExecutionDao#findAllByTestCaseIdOrderByRunDate(long, org.squashtest.csp.core.infrastructure.collection.Paging)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging) {
		Query query = currentSession().getNamedQuery("execution.findAllByTestCaseIdOrderByRunDate");
		PagingUtils.addPaging(query, paging);
		query.setParameter("testCaseId", testCaseId);
		
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Execution> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		Criteria crit = currentSession().createCriteria(Execution.class, "Execution");
		crit.createAlias("testPlan.iteration", "Iteration", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Iteration.campaign", "Campaign", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("Campaign.project", "Project", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("referencedTestCase", "TestCase", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("testPlan.testSuites", "TestSuite", JoinType.LEFT_OUTER_JOIN);

		crit.add(Restrictions.eq("TestCase.id", Long.valueOf(testCaseId)));
		
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		PagingUtils.addPaging(crit, pas);
		SortingUtils.addOrder(crit, pas);

		return crit.list();
	}

	@Override
	public long countByTestCaseId(long testCaseId) {
		return (Long) executeEntityNamedQuery("execution.countByTestCaseId", "testCaseId", testCaseId);
	}
	
	@Override
	public boolean wasNeverRan(Long executionId) {
		return ((countExecutionSteps(executionId) - countReady(executionId)) == 0);
	}
	
	
	
	private static class CountStepStatusByExecutionParamSetter implements SetQueryParametersCallback{

		private Long executionId;
		private ExecutionStatus status;
		
		
		public CountStepStatusByExecutionParamSetter(Long executionId,
				ExecutionStatus status) {
			super();
			this.executionId = executionId;
			this.status = status;
		}



		@Override
		public void setQueryParameters(Query query) {
			query.setLong("execId", executionId);
			query.setParameter("status", status);
		}
	}




}
