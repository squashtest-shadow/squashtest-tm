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
package org.squashtest.tm.service.internal.repository;

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

import java.util.Collection;
import java.util.List;

public interface IssueDao extends EntityDao<Issue> {


	List<Issue> getAllIssueFromBugTrackerId(Long bugtrackerId);

	/**
	 * Will count the total number of issues related to the given IssueList.
	 *
	 *
	 * @param issueListIds
	 *            the id of the issue lists.
	 * @return how many issues they hold.
	 */
	Integer countIssuesfromIssueList(List<Long> issueListIds);

	/**
	 * Will count the total number of issues related to the given IssueList, for the given bugtracker
	 *
	 *
	 * @param issueListIds
	 *            the id of the issue lists.
	 * @param bugTrackerId
	 *            the id of the bug-tracker we are filtering on
	 * @return how many issues they hold.
	 */
	Integer countIssuesfromIssueList(Collection<Long> issueListIds, Long bugTrackerId);

	/**
	 * Finds all issues for a campaign along with the declaring execution and returns them as pairs.
	 * Issues declared on execution steps are also picked.
	 */
	List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaign(Campaign campaign, PagingAndSorting sorter);

	/**
	 * Counts all issues for a campaign
	 */
	long countByCampaign(Campaign campaign);

	/**
	 * Will find all issues belonging to the issue-lists of the given ids, and, return a list of <code>Object[]</code> that have the following structure :  [IssueList.id, Issue.remoteIssueId, Issue.id]
	 * <br><br>The issues are also filtered over the bug-tracker parameter: only issues linked to the bug-tracker of the given id are retained.
	 *
	 *
	 * @param issueListId
	 *            the list of the ids of the IssueList
	 *
	 * @param sorter
	 *           : will sort and filter the result set
	 *
	 * @param bugTrackerId
	 * 			 the id of the bug-tracker we want the issues to be connected-to
	 *
	 * @return non-null but possibly empty list of <code>Issue</code>
	 **/
	List<Issue> findSortedIssuesFromIssuesLists(Collection<Long> issueListId, PagingAndSorting sorter,
												Long bugTrackerId);

	/**
	 * Will find all issues belonging to the executions/executionSteps of the given ids, and, return a list of <code>Object[]</code> that have the following structure :  [IssueList.id, Issue.remoteIssueId , Issue.bugtracker.id]
	 * <br><br>The issues are also filtered over the bug-tracker parameter: only issues linked to the bug-tracker active for the given execution/executionSteps's project's bug-tracker are retained.
	 *
	 * @param executionIds : ids of executions we will extract Issues from
	 * @param executionStepsIds : ids of executionSteps we will extract Issues from
	 * @param sorter : holds the sort parameters for the query
	 * @return non-null but possibly empty list of <code>Object[]</code> which have the following structure <b>[IssueList.id, Issue.remoteIssueId , Issue.bugtracker.id]</b>
	 */
	List<Issue> findSortedIssuesFromExecutionAndExecutionSteps(List<Long> executionIds,
															   List<Long> executionStepsIds, PagingAndSorting sorter);

	/**
	 * Will count all Issues from the given executions and execution-steps <b>concerned by the active bug-tracker</b> for each
	 * execution/execution-step's project.
	 *
	 * @return the number of Issues detected by the given execution / execution Steps
	 */
	Integer countIssuesfromExecutionAndExecutionSteps(List<Long> executionsIds, List<Long> executionStepsIds);

	/**
	 * Will find all issues declared in the iteration of the given id.
	 * @param id : the id of the concerned {@linkplain Iteration}
	 * @return the list of the iteration's {@link Issue}s
	 */
	List<Issue> findAllForIteration(Long id);

	/**
	 * Will find all issues declared in the test suite of the given id.
	 * @param id : the id of the concerned TestSuite
	 * @return the list of the suite's {@link Issue}s
	 */
	List<Issue> findAllForTestSuite(Long id);

	Integer countIssuesfromExecutionSteps(List<Long> executionStepsIds);

	/**
	 * Will return the Execution or the ExecutionStep that holds the Issue of the given id.
	 * @param id : the id of the Issue we want the owner of.
	 * @return the found IssueDetector or <code>null</code>.
	 */
	IssueDetector findIssueDetectorByIssue(long id);

	TestCase findTestCaseRelatedToIssue(long issueId);

	Execution findExecutionRelatedToIssue(long issueId);

	List<Issue> findIssueListByRemoteIssue(String remoteid, BugTracker bugtracker);

	/**
	 * Finds all issues for a execution (not execution and steps !) along with this execution and returns them as pairs.
	 * Issues declared on execution steps are also picked.
	 */
	List<Pair<Execution, Issue>> findAllDeclaredExecutionIssuePairsByExecution(Execution execution, PagingAndSorting sorter);

	/**
	 * Counts all issues for an execution and its steps
	 */
	long countByExecutionAndSteps(Execution execution);

	List<Pair<Execution, Issue>> findAllExecutionIssuePairsByIteration(Iteration iteration, PagingAndSorting sorter);

	long countByIteration(Iteration iteration);

	List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestSuite(TestSuite testSuite, PagingAndSorting sorter);

	long countByTestSuite(TestSuite testSuite);

	List<Pair<Execution, Issue>> findAllExecutionIssuePairsByCampaignFolder(CampaignFolder folder, PagingAndSorting sorter);

	long countByCampaignFolder(CampaignFolder folder);

	List<Pair<Execution, Issue>> findAllExecutionIssuePairsByTestCase(TestCase testCase, PagingAndSorting sorter);

	long countByTestCase(TestCase testCase);

	List<Issue> findAllByExecutionStep(ExecutionStep executionStep, PagingAndSorting sorter);


	List<Pair<ExecutionStep, Issue>> findAllExecutionStepIssuePairsByExecution(Execution execution, PagingAndSorting sorter);
}
