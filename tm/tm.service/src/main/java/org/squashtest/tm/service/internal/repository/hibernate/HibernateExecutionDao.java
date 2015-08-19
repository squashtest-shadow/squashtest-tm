/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
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
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.ExecutionDao;

@Repository
public class HibernateExecutionDao extends HibernateEntityDao<Execution> implements ExecutionDao {
	private static final String TEST_SUITE = "TestSuite";
	private static final String TEST_CASE = "TestCase";
	private static final String EXECUTION = "Execution";
	private static final String TEST_PLAN = "TestPlan";
	private static final String EXECUTION_COUNT_STATUS = "execution.countStatus";
	private static final String CAMPAIGN = "Campaign";
	private static final String CAMPAIGN_PROJECT = "Campaign.project";
	private static final String ITERATION = "Iteration";
	private static final String PROJECT = "Project";
	private static final String PROJECT_ID = "Project.id";
	private static final String ITERATION_CAMPAIGN = "Iteration.campaign";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String EXECUTION_ID = "executionId";

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
		Query q = currentSession().getNamedQuery("execution.findSteps");
		q.setParameter(EXECUTION_ID, executionId);
		return q.list();
	}

	@Override
	public List<ActionTestStep> findOriginalSteps(long executionId) {
		Query q = currentSession().getNamedQuery("execution.findOriginalSteps");
		q.setParameter(EXECUTION_ID, executionId);
		return q.list();
	}

	@Override
	public List<Long> findOriginalStepIds(long executionId) {
		Query q = currentSession().getNamedQuery("execution.findOriginalStepIds");
		q.setParameter(EXECUTION_ID, executionId);
		return q.list();
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
				.createCriteria(IterationTestPlanItem.class).createAlias("executions", "execution")
				.add(Restrictions.eq("execution.id", executionId)).uniqueResult();

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

		for (ExecutionStatus status : ExecutionStatus.values()) {
			final ExecutionStatus fStatus = status;

			SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId, fStatus);

			Long lResult = executeEntityNamedQuery(EXECUTION_COUNT_STATUS, newCallBack);

			report.set(status, lResult.intValue());
		}

		return report;
	}

	@Override
	public long countSuccess(final long executionId) {
		SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId,
				ExecutionStatus.SUCCESS);

		return (Long) executeEntityNamedQuery(EXECUTION_COUNT_STATUS, newCallBack);
	}

	@Override
	public long countReady(final long executionId) {
		SetQueryParametersCallback newCallBack = new CountStepStatusByExecutionParamSetter(executionId,
				ExecutionStatus.READY);

		return (Long) executeEntityNamedQuery(EXECUTION_COUNT_STATUS, newCallBack);
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

	// ************** special execution status deactivation section ***************

	@SuppressWarnings("unchecked")
	@Override
	public List<ExecutionStep> findAllExecutionStepsWithStatus(Long projectId, ExecutionStatus executionStatus) {

		Criteria crit = currentSession().createCriteria(ExecutionStep.class, "ExecutionStep");
		crit.createAlias("execution", EXECUTION, JoinType.INNER_JOIN);
		crit.createAlias("Execution.testPlan.iteration", ITERATION, JoinType.INNER_JOIN);
		crit.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.INNER_JOIN);
		crit.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.INNER_JOIN);
		crit.add(Restrictions.eq(PROJECT_ID, Long.valueOf(projectId)));
		crit.add(Restrictions.eq(EXECUTION_STATUS, executionStatus));

		return crit.list();
	};

	@Override
	public List<Long> findAllExecutionIdHavingStepWithStatus(Long projectId, ExecutionStatus source) {

		Query q = currentSession().getNamedQuery("execution.findExecutionIdsHavingStepStatus");
		q.setParameter("status", source);
		q.setParameter("projectId", projectId);
		return q.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IterationTestPlanItem> findAllIterationTestPlanItemsWithStatus(Long projectId,
			ExecutionStatus executionStatus) {

		Criteria crit = currentSession().createCriteria(IterationTestPlanItem.class, TEST_PLAN);
		crit.createAlias("iteration", ITERATION, JoinType.INNER_JOIN);
		crit.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.INNER_JOIN);
		crit.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.INNER_JOIN);
		crit.add(Restrictions.eq(PROJECT_ID, Long.valueOf(projectId)));
		crit.add(Restrictions.eq(EXECUTION_STATUS, executionStatus));

		return crit.list();
	};

	@Override
	public boolean projectUsesExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		return (hasExecStepWithStatus(projectId, executionStatus)
				|| hasItemTestPlanWithStatus(projectId, executionStatus)
				|| hasExecWithStatus(projectId, executionStatus) );
	}

	private boolean hasItemTestPlanWithStatus(long projectId, ExecutionStatus executionStatus) {
		Session session = currentSession();
		Query qStep = session.getNamedQuery("executionStep.countAllStatus");
		qStep.setParameter("status", executionStatus);
		qStep.setParameter("projectId", projectId);
		Long nStep = (Long) qStep.uniqueResult();
		return nStep > 0;
	}

	private boolean hasExecWithStatus(long projectId, ExecutionStatus executionStatus) {
		Session session = currentSession();
		Query qExec = session.getNamedQuery("execution.countAllStatus");
		qExec.setParameter("status", executionStatus);
		qExec.setParameter("projectId", projectId);
		Long nExec = (Long) qExec.uniqueResult();
		return nExec > 0;
	}

	private boolean hasExecStepWithStatus(long projectId, ExecutionStatus executionStatus) {
		Session session = currentSession();
		Query qITP = session.getNamedQuery("iterationTestPlanItem.countAllStatus");
		qITP.setParameter("status", executionStatus);
		qITP.setParameter("projectId", projectId);
		Long nITP = (Long) qITP.uniqueResult();
		return nITP > 0;
	}

	@Override
	public void replaceExecutionStepStatus(long projectId, ExecutionStatus oldStatus, ExecutionStatus newStatus) {

		Session session = currentSession();
		Query qStep = session.getNamedQuery("executionStep.replaceStatus");
		qStep.setParameter("oldStatus", oldStatus);
		qStep.setParameter("newStatus", newStatus);
		qStep.setParameter("projectId", projectId);
		qStep.executeUpdate();

	}

	@Override
	public void replaceTestPlanStatus(long projectId, ExecutionStatus oldStatus, ExecutionStatus newStatus) {

		Session session = currentSession();

		Query qStep = session.getNamedQuery("iterationTestPlanItem.replaceStatus");
		qStep.setParameter("oldStatus", oldStatus);
		qStep.setParameter("newStatus", newStatus);
		qStep.setParameter("projectId", projectId);
		qStep.executeUpdate();

	}

	// ************** /special execution status deactivation section **************

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
		return (Long) executeEntityNamedQuery("execution.countSteps", EXECUTION_ID, executionId);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.ExecutionDao#findAllByTestCaseIdOrderByRunDate(long,
	 *      org.squashtest.csp.core.infrastructure.collection.Paging)
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
		Criteria crit = currentSession().createCriteria(Execution.class, EXECUTION);
		crit.createAlias("testPlan.iteration", ITERATION, JoinType.LEFT_OUTER_JOIN);
		crit.createAlias(ITERATION_CAMPAIGN, CAMPAIGN, JoinType.LEFT_OUTER_JOIN);
		crit.createAlias(CAMPAIGN_PROJECT, PROJECT, JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("referencedTestCase", TEST_CASE, JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("testPlan.testSuites", TEST_SUITE, JoinType.LEFT_OUTER_JOIN);

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

	private static class CountStepStatusByExecutionParamSetter implements SetQueryParametersCallback {

		private Long executionId;
		private ExecutionStatus status;

		public CountStepStatusByExecutionParamSetter(Long executionId, ExecutionStatus status) {
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
