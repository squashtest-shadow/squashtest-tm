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

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.bugtracker.Pair;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IssueDao;

@Repository
public class HibernateIssueDao extends HibernateEntityDao<Issue> implements IssueDao {

	private static final String SELECT_ISSUES_INTRO =
			"select Issue from Issue Issue ";


	private static final String COUNT_ISSUES_INTRO =
			"select count(Issue) from Issue Issue ";


	private static final String SELECT_ISSUES_OUTRO =
			"and Issue.bugtracker.id in (" +
					"select bt.id " +
					"from ExecutionStep estep " +
					"inner join estep.execution exec " +
					"inner join exec.testPlan tp " +
					"inner join tp.iteration it " +
					"inner join it.campaign cp " +
					"inner join cp.project proj " +
					"inner join proj.bugtrackerBinding binding " +
					"inner join binding.bugtracker bt " +
					"where estep.id in (:executionStepsIds) " +
					") ";

	private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP =
			// ------------------------------------Where issues is from the given
			// Executions
			"where (" +
			"Issue.id in ( "+
			"select isExec.id "+
			"from Execution exec "+
			"inner join exec.issueList ile "+
			"inner join ile.issues isExec "+
			"where exec.id in (:executionsIds) " +
			") "+
			"or Issue.id in (" +
			"select isStep.id " +
			"from ExecutionStep estep " +
			"inner join estep.issueList ils " +
			"inner join ils.issues isStep " +
			"where estep.id in (:executionStepsIds) " +
			") " +
			") ";

	private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP =
			"where Issue.id in (" +
					"select isStep.id " +
					"from ExecutionStep estep " +
					"inner join estep.issueList ils " +
					"inner join ils.issues isStep " +
					"where estep.id in (:executionStepsIds) " +
					") ";

	/**
	 *
	 * @see org.squashtest.tm.service.internal.repository.IssueDao#countIssuesfromIssueList(java.util.List)
	 */
	@Override
	public Integer countIssuesfromIssueList(final List<Long> issueListIds) {

		if (!issueListIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("issueList.countIssues");
			query.setParameterList("issueListIds", issueListIds);
			Long result = (Long) query.uniqueResult();

			return result.intValue();
		} else {
			return 0;
		}

	}

	/**
	 *
	 * @see org.squashtest.tm.service.internal.repository.IssueDao#countIssuesfromIssueList(java.util.Collection,
	 *      java.lang.Long)
	 */
	@Override
	public Integer countIssuesfromIssueList(Collection<Long> issueListIds, Long bugTrackerId) {
		if (!issueListIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("issueList.countIssuesByTracker");
			query.setParameterList("issueListIds", issueListIds);
			query.setParameter("bugTrackerId", bugTrackerId);
			Long result = (Long) query.uniqueResult();

			return result.intValue();
		} else {
			return 0;
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaign(Campaign campaign, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.testPlan tp join tp.iteration i join i.campaign c join ex.issues Issue where c = :camp", sorter);

		Query query = currentSession().createQuery(hql).setParameter("camp", campaign);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}


	@Override
	public long countByCampaign(Campaign campaign) {
		return (long) currentSession().getNamedQuery("issue.countByCampaign")
			.setParameter("campaign", campaign)
			.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findSortedIssuesFromIssuesLists(final Collection<Long> issueListIds,
			final PagingAndSorting sorter, Long bugtrackerId) {

		if (issueListIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria crit = currentSession().createCriteria(Issue.class, "Issue")
				.add(Restrictions.in("Issue.issueList.id", issueListIds))
				.add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));

		SortingUtils.addOrder(crit, sorter);
		PagingUtils.addPaging(crit, sorter);

		return crit.list();

	}

	/**
	 * @see {@linkplain IssueDao#findSortedIssuesFromExecutionAndExecutionSteps(List, List, PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findSortedIssuesFromExecutionAndExecutionSteps(List<Long> executionsIds,
			List<Long> executionStepsIds, PagingAndSorting sorter) {
		if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {

			String queryString = SELECT_ISSUES_INTRO + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP + SELECT_ISSUES_OUTRO;

			queryString += " order by " + sorter.getSortedAttribute() + " " + sorter.getSortOrder().getCode();

			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionsIds", executionsIds);
			query.setParameterList("executionStepsIds", executionStepsIds);

			if (!sorter.shouldDisplayAll()) {
				PagingUtils.addPaging(query, sorter);
			}

			return query.list();

		}

		return Collections.emptyList();
	}

	@Override
	public Integer countIssuesfromExecutionAndExecutionSteps(List<Long> executionsIds, List<Long> executionStepsIds) {
		if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {

			String queryString = COUNT_ISSUES_INTRO + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP + SELECT_ISSUES_OUTRO;

			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionsIds", executionsIds);
			query.setParameterList("executionStepsIds", executionStepsIds);

			Long result = (Long) query.uniqueResult();
			return result.intValue();

		} else {
			return 0;
		}
	}

	@Override
	public Integer countIssuesfromExecutionSteps(List<Long> executionStepsIds) {
		if (!executionStepsIds.isEmpty()) {
			String queryString = COUNT_ISSUES_INTRO + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP + SELECT_ISSUES_OUTRO;
			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionStepsIds", executionStepsIds);
			Long result = (Long) query.uniqueResult();
			return result.intValue();

		} else {
			return 0;
		}
	}

	@Override
	public List<Issue> findAllForIteration(Long id) {
		return executeListNamedQuery("Issue.findAllForIteration", new SetIdParameter("id", id));

	}

	@Override
	public List<Issue> findAllForTestSuite(Long id) {
		return executeListNamedQuery("Issue.findAllForTestSuite", new SetIdParameter("id", id));
	}

	@Override
	public IssueDetector findIssueDetectorByIssue(long id) {
		Execution exec = executeEntityNamedQuery("Issue.findExecution", new SetIdParameter("id", id));
		if (exec != null) {
			return exec;
		} else {
			return executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
		}
	}

	@Override
	public TestCase findTestCaseRelatedToIssue(long id) {

		TestCase testCase = null;

		Execution exec = executeEntityNamedQuery("Issue.findExecution", new SetIdParameter("id", id));
		if (exec != null) {
			testCase = exec.getReferencedTestCase();
		} else {
			ExecutionStep step = executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
			if (step != null && step.getExecution() != null) {
				testCase = step.getExecution().getReferencedTestCase();
			}
		}

		return testCase;
	}

    @Override
    public Execution findExecutionRelatedToIssue(long id) {
    	 Execution exec = executeEntityNamedQuery("Issue.findExecution", new SetIdParameter("id", id));
         if (exec == null) {
        	 ExecutionStep step = executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
        	 if (step != null && step.getExecution() != null) {
        		 exec = step.getExecution();
        	 }
         }
         return exec;
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> getAllIssueFromBugTrackerId(Long bugtrackerId) {
		final Criteria crit = currentSession().createCriteria(Issue.class, "Issue")
				.add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));

		return crit.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Issue> findIssueListByRemoteIssue(String remoteid, BugTracker bugtracker) {
		final Criteria crit = currentSession().createCriteria(Issue.class, "Issue")
				.add(Restrictions.eq("Issue.remoteIssueId", remoteid))
				.add(Restrictions.eq("Issue.bugtracker.id", bugtracker.getId()));

		return crit.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllDeclaredExecutionIssuePairsByExecution(Execution execution, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issueList il join il.issues Issue where ex = :execution", sorter);

		Query query = currentSession().createQuery(hql).setParameter("execution", execution);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	public long countByExecutionAndSteps(Execution execution) {
		return (long) currentSession().getNamedQuery("issue.countByExecutionAndSteps")
			.setParameter("execution", execution)
			.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByIteration(Iteration iteration, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.testPlan tp join tp.iteration i join ex.issues Issue where i = :iteration", sorter);

		Query query = currentSession().createQuery(hql).setParameter("iteration", iteration);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	public long countByIteration(Iteration iteration) {
		return (long) currentSession().getNamedQuery("issue.countByIteration")
			.setParameter("iteration", iteration)
			.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestSuite(TestSuite testSuite, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from TestSuite ts join ts.testPlan tp join tp.executions ex join ex.issues Issue where ts = :testSuite", sorter);

		Query query = currentSession().createQuery(hql).setParameter("testSuite", testSuite);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	public long countByTestSuite(TestSuite testSuite) {
		return (long) currentSession().getNamedQuery("issue.countByTestSuite")
			.setParameter("testSuite", testSuite)
			.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaignFolder(CampaignFolder folder, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issues Issue where ex.testPlan.iteration.campaign.id in (select cpe.descendantId from CampaignPathEdge cpe where cpe.ancestorId = :folderId)", sorter);

		Query query = currentSession().createQuery(hql).setParameter("folderId", folder.getId());
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	public long countByCampaignFolder(CampaignFolder folder) {
		return (long) currentSession().getNamedQuery("issue.countByCampaignFolder")
			.setParameter("folderId", folder.getId())
			.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestCase(TestCase testCase, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(ex, Issue) from Execution ex join ex.issues Issue join ex.testPlan tp join tp.referencedTestCase tc where tc = :testCase", sorter);

		Query query = currentSession().createQuery(hql).setParameter("testCase", testCase);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

	@Override
	public long countByTestCase(TestCase testCase) {
		return (long) currentSession().getNamedQuery("issue.countByTestCase")
			.setParameter("testCase", testCase)
			.uniqueResult();
	}

	@Override
	public List<Issue> findAllByExecutionStep(ExecutionStep executionStep, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select Issue from ExecutionStep s join s.issueList il join il.issues Issue where s = :step", sorter);

		Query query = currentSession().createQuery(hql).setParameter("step", executionStep);
		PagingUtils.addPaging(query, sorter);

		return query.list();
}

	@Override
	public List<Pair<ExecutionStep, Issue>> findAllExecutionStepIssuePairsByExecution(Execution execution, PagingAndSorting sorter) {
		String hql = SortingUtils.addOrder("select new org.squashtest.tm.service.internal.bugtracker.Pair(s, Issue) from ExecutionStep s join s.issueList il join il.issues Issue join s.execution ex where ex = :execution", sorter);

		Query query = currentSession().createQuery(hql).setParameter("execution", execution);
		PagingUtils.addPaging(query, sorter);

		return query.list();
	}

}
