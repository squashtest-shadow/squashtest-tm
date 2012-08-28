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
package org.squashtest.csp.tm.internal.service.bugtracker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.IssueAlreadyBoundException;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao;
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.BugTrackersLocalService;

@Service("squashtest.tm.service.BugTrackersLocalService")
@Transactional
public class BugTrackersLocalServiceImpl implements BugTrackersLocalService {

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackersService remoteBugTrackersService;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
		return remoteBugTrackersService.getInterfaceDescriptor(bugTracker);
	}

	@Override
	public BugTrackerStatus checkBugTrackerStatus(Project project) {
		BugTrackerStatus status;

		if (!project.isBugtrackerConnected()) {
			status = BugTrackerStatus.BUGTRACKER_UNDEFINED;
		} else if (remoteBugTrackersService.isCredentialsNeeded(project.findBugTracker())) {
			status = BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS;
		} else {
			status = BugTrackerStatus.BUGTRACKER_READY;
		}
		return status;
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public BTIssue createIssue(IssueDetector entity, BTIssue issue) {
		BugTracker bugTracker = entity.getBugTracker();
		String btName = bugTracker.getName();
		issue.setBugtracker(btName);

		BTIssue createdIssue = remoteBugTrackersService.createIssue(issue, bugTracker);
		createdIssue.setBugtracker(btName);

		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());
		sqIssue.setBugtrackerName(btName);

		IssueList list = entity.getIssueList();

		list.addIssue(sqIssue);

		issueDao.persist(sqIssue);

		return createdIssue;
	}

	@Override
	public BTIssue getIssue(String issueKey, BugTracker bugTracker) {
		return remoteBugTrackersService.getIssue(issueKey, bugTracker);
	}

	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker) {
		return remoteBugTrackersService.getIssues(issueKeyList, bugTracker);
	}

	/* ************** delegate methods ************* */

	@Override
	public BTProject findRemoteProject(String name, BugTracker bugTracker) {
		return remoteBugTrackersService.findProject(name, bugTracker);

	}

	@Override
	public List<Priority> getRemotePriorities(BugTracker bugTracker) {
		return remoteBugTrackersService.getPriorities(bugTracker);

	}

	@Override
	public void setCredentials(String username, String password, BugTracker bugTracker) {
		remoteBugTrackersService.setCredentials(username, password, bugTracker);
	}

	@Override
	public URL getIssueUrl(String btIssueId, BugTracker bugTracker) {
		return remoteBugTrackersService.getViewIssueUrl(btIssueId, bugTracker);
	}

	@Override
	@PreAuthorize("hasPermission(#bugged, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public void attachIssue(IssueDetector bugged, String remoteIssueKey) {

		IssueList issueList = bugged.getIssueList();

		// check that the issue exists
		BTIssue test = getIssue(remoteIssueKey, bugged.getBugTracker());

		// at that point the service was supposed to fail if not found so we can move on
		// but, in case of a wrong implementation of a connector here is a safety belt:
		if (test == null) {
			throw new BugTrackerNotFoundException("issue " + remoteIssueKey + " could not be found", null);
		}

		if (issueList.hasRemoteIssue(remoteIssueKey)) {
			throw new IssueAlreadyBoundException();
		} else {

			Issue issue = new Issue();
			issue.setBugtrackerName(bugged.getBugTracker().getName());
			issue.setRemoteIssueId(test.getId());
			issueList.addIssue(issue);
			issueDao.persist(issue);

		}
	}

	/* ------------------------ExecutionStep--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#stepId, 'org.squashtest.csp.tm.domain.execution.ExecutionStep', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnerShipsForExecutionStep(
			Long stepId, CollectionSorting sorter) {
		// find the ExecutionStep's IssueList id
		ExecutionStep executionStep = executionStepDao.findById(stepId);
		BugTracker bugTracker = executionStep.getBugTracker();
		List<Long> issueListId = Arrays.asList(executionStep.getIssueListId());

		// find the list of remoteIds out of the IssueListId
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromIssuesLists(issueListId,
				sorter, bugTracker.getName());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		// find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackersService.getIssues(issuesRemoteIds, bugTracker);

		// make a FiltredCollectionHolder of IssueOwnerShip
		List<IssueOwnership<BTIssue>> ownerships = bindBTIssuesToExecutionStep(btIssues, executionStep);
		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListId, bugTracker.getName());
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(totalIssues, ownerships);
	}

	private List<IssueOwnership<BTIssue>> bindBTIssuesToExecutionStep(List<BTIssue> btIssues,
			ExecutionStep executionStep) {
		List<IssueOwnership<BTIssue>> ownerships = new ArrayList<IssueOwnership<BTIssue>>(btIssues.size());
		for (BTIssue btIssue : btIssues) {
			IssueOwnership<BTIssue> ownership = new IssueOwnership<BTIssue>(btIssue, executionStep);
			ownerships.add(ownership);
		}
		return ownerships;
	}

	/* ------------------------Execution--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#execId, 'org.squashtest.csp.tm.domain.execution.Execution', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsforExecution(Long execId,
			CollectionSorting sorter) {
		Execution execution = executionDao.findById(execId);
		BugTracker bugTracker = execution.getBugTracker();
		List<IssueDetector> issueDetectors = executionDao.findAllIssueDetectorsForExecution(execId);
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------Iteration--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#iterId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipForIteration(Long iterId,
			CollectionSorting sorter) {
		// find bug-tracker
		Iteration iteration = iterationDao.findById(iterId);
		BugTracker bugTracker = iteration.getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = iterationDao.findAllExecutionByIterationId(iterId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------Campaign--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#campId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForCampaigns(Long campId,
			CollectionSorting sorter) {
		// find bug-tracker
		Campaign campaign = campaignDao.findById(campId);
		BugTracker bugTracker = campaign.getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = campaignDao.findAllExecutionsByCampaignId(campId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------TestSuite--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForTestSuite(
			Long testSuiteId, CollectionSorting sorter) {
		// find bug-tracker
		TestSuite testSuite = testSuiteDao.findById(testSuiteId);
		BugTracker bugTracker = testSuite.getIteration().getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = testSuiteDao.findAllExecutionByTestSuite(testSuiteId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------TestCase--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#tcId, 'org.squashtest.csp.tm.domain.testcase.TestCase', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipForTestCase(Long tcId,
			//FIXME [Feat 1194] we are supposed to return issues from different bugTrackers.
			CollectionSorting sorter) {
		// Find bug-tracker
		TestCase testCase = testCaseDao.findById(tcId);
		BugTracker bugTracker = testCase.getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = testCaseDao.findAllExecutionByTestCase(tcId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------generic--------------------------------------- */

	private List<IssueDetector> collectIssueDetectorsFromExecution(List<Execution> executions) {
		List<IssueDetector> issueDetectors = new ArrayList<IssueDetector>();
		for (Execution execution : executions) {
			issueDetectors.add(execution);
			issueDetectors.addAll(execution.getSteps());
		}
		return issueDetectors;
	}

	private FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> createOwnershipsCollection(
			CollectionSorting sorter, List<IssueDetector> issueDetectors, BugTracker bugTracker) {
		// Collect all IssueList.id out of the IssueDetector list, but keep the information about the
		// IssueDetector/IssueList association
		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByListId(issueDetectors);
		Set<Long> issueListIds = issueDetectorByListId.keySet();
		List<Long> issueListIdsList = new ArrayList<Long>(issueListIds.size());
		issueListIdsList.addAll(issueListIds);

		// Find the list of Issues RemoteIds out of the list of IssueList.id , but keep the information about the
		// IssueList/remoteId association
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromIssuesLists(issueListIdsList,
				sorter, bugTracker.getName());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		// Find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackersService.getIssues(issuesRemoteIds, bugTracker);

		// Bind the BT issues to their owner with the kept informations
		List<IssueOwnership<BTIssue>> ownerships = bindBTIssuesToOwner(btIssues, sortedIssueListIdsAndIssueRemoteIds,
				issueDetectorByListId);

		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIdsList, bugTracker.getName());
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(totalIssues, ownerships);
	}

	private Map<Long, IssueDetector> createIssueDetectorByListId(List<IssueDetector> issueDetectors) {
		Map<Long, IssueDetector> issueDetectorByListId = new HashMap<Long, IssueDetector>();
		for (IssueDetector issueDetector : issueDetectors) {
			issueDetectorByListId.put(issueDetector.getIssueListId(), issueDetector);
		}
		return issueDetectorByListId;
	}

	private List<IssueOwnership<BTIssue>> bindBTIssuesToOwner(List<BTIssue> btIssues,
			List<Object[]> sortedIssueListIdsAndIssueRemoteIds, Map<Long, IssueDetector> issueDetectorByListId) {
		List<IssueOwnership<BTIssue>> ownerships = new ArrayList<IssueOwnership<BTIssue>>(btIssues.size());
		Map<String, Long> listIdByRemoteId = createMapOutOfList(sortedIssueListIdsAndIssueRemoteIds);
		for (BTIssue btIssue : btIssues) {
			Long listId = listIdByRemoteId.get(btIssue.getId());
			IssueDetector owner = issueDetectorByListId.get(listId);
			IssueOwnership<BTIssue> ownership = new IssueOwnership<BTIssue>(btIssue, owner);
			ownerships.add(ownership);
		}
		return ownerships;
	}

	private Map<String, Long> createMapOutOfList(List<Object[]> sortedIssueListIdsAndIssueRemoteIds) {
		Map<String, Long> map = new HashMap<String, Long>(sortedIssueListIdsAndIssueRemoteIds.size());
		for (Object[] object : sortedIssueListIdsAndIssueRemoteIds) {
			map.put((String) object[1], (Long) object[0]);
		}
		return map;
	}

	private List<String> extractRemoteIds(List<Object[]> sortedIssueListIdsAndIssueRemoteIds) {
		List<String> issuesRemoteIds = new ArrayList<String>(sortedIssueListIdsAndIssueRemoteIds.size());
		for (Object[] issueListIdAndIssueRemoteId : sortedIssueListIdsAndIssueRemoteIds) {
			issuesRemoteIds.add((String) issueListIdAndIssueRemoteId[1]);
		}
		return issuesRemoteIds;
	}

	@Override
	public Set<String> getProviderKinds() {
		return remoteBugTrackersService.getProviderKinds();
	}

}
