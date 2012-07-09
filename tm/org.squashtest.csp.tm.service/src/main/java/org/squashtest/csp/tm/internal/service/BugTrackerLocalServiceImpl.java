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
package org.squashtest.csp.tm.internal.service;

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
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.service.BugTrackerService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.IssueAlreadyBoundException;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao;
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.BugTrackerLocalService;

@Service("squashtest.tm.service.BugTrackerLocalService")
@Transactional
public class BugTrackerLocalServiceImpl implements BugTrackerLocalService {

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackerService remoteBugTrackerService;

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

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return remoteBugTrackerService.getInterfaceDescriptor();
	}

	@Override
	public BugTrackerStatus checkBugTrackerStatus() {
		BugTrackerStatus status;

		if (!remoteBugTrackerService.isBugTrackerDefined()) {
			status = BugTrackerStatus.BUGTRACKER_UNDEFINED;
		} else if (remoteBugTrackerService.isCredentialsNeeded()) {
			status = BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS;
		} else {
			status = BugTrackerStatus.BUGTRACKER_READY;
		}
		return status;
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public BTIssue createIssue(IssueDetector entity, BTIssue issue) {

		String btName = remoteBugTrackerService.getBugTrackerName();
		issue.setBugtracker(btName);

		BTIssue createdIssue = remoteBugTrackerService.createIssue(issue);
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
	public BTIssue getIssue(String issueKey) {
		return remoteBugTrackerService.getIssue(issueKey);
	}

	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList) {
		return remoteBugTrackerService.getIssues(issueKeyList);
	}

	/* ************** delegate methods ************* */

	@Override
	public BTProject findRemoteProject(String name) {
		return remoteBugTrackerService.findProject(name);

	}

	@Override
	public List<Priority> getRemotePriorities() {
		return remoteBugTrackerService.getPriorities();

	}

	@Override
	public void setCredentials(String username, String password) {
		remoteBugTrackerService.setCredentials(username, password);
	}

	@Override
	public URL getIssueUrl(String btIssueId) {
		return remoteBugTrackerService.getViewIssueUrl(btIssueId);
	}

	@Override
	public URL getBugtrackerUrl() {
		return remoteBugTrackerService.getBugTrackerUrl();
	}

	@Override
	public Boolean getBugtrackerIframeFriendly() {
		return new Boolean(remoteBugTrackerService.isIframeFriendly());
	}

	@Override
	@PreAuthorize("hasPermission(#bugged, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public void attachIssue(IssueDetector bugged, String remoteIssueKey) {

		IssueList issueList = bugged.getIssueList();

		// check that the issue exists
		BTIssue test = getIssue(remoteIssueKey);

		// at that point the service was supposed to fail if not found so we can move on
		// but, in case of a wrong implementation of a connector here is a safety belt:
		if (test == null) {
			throw new BugTrackerNotFoundException("issue " + remoteIssueKey + " could not be found", null);
		}

		if (issueList.hasRemoteIssue(remoteIssueKey)) {
			throw new IssueAlreadyBoundException();
		} else {

			Issue issue = new Issue();
			issue.setBugtrackerName(remoteBugTrackerService.getBugTrackerName());
			issue.setRemoteIssueId(test.getId());
			issueList.addIssue(issue);
			issueDao.persist(issue);

		}
	}

	/* ------------------------ExecutionStep--------------------------------------- */
	@Override
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnerShipsForExecutionStep(
			Long stepId, CollectionSorting sorter) {
		// find the ExecutionStep's IssueList id
		ExecutionStep executionStep = executionStepDao.findById(stepId);
		List<Long> issueListId = Arrays.asList(executionStep.getIssueListId());

		// find the list of remoteIds out of the IssueListId
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromIssuesLists(issueListId,
				sorter, remoteBugTrackerService.getBugTrackerName());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		// find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackerService.getIssues(issuesRemoteIds);

		// make a FiltredCollectionHolder of IssueOwnerShip
		List<IssueOwnership<BTIssue>> ownerships = bindBTIssuesToExecutionStep(btIssues, executionStep);
		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListId,
				remoteBugTrackerService.getBugTrackerName());
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
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsforExecution(Long execId,
			CollectionSorting sorter) {
		// Find all concerned IssueDetector
		List<IssueDetector> issueDetectors = executionDao.findAllIssueDetectorsForExecution(execId);

		return createOwnershipsCollection(sorter, issueDetectors);
	}

	/* ------------------------Iteration--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipForIteration(Long iterId,
			CollectionSorting sorter) {

		// Find all concerned IssueDetector
		List<Execution> executions = iterationDao.findAllExecutionByIterationId(iterId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors);
	}

	/* ------------------------Campaign--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForCampaigns(Long campId,
			CollectionSorting sorter) {
		// Find all concerned IssueDetector
		List<Execution> executions = campaignDao.findAllExecutionsByCampaignId(campId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors);
	}

	/* ------------------------TestSuite--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForTestSuite(
			Long testSuiteId, CollectionSorting sorter) {
		// Find all concerned IssueDetector
		List<Execution> executions = testSuiteDao.findAllExecutionByTestSuite(testSuiteId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors);
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
			CollectionSorting sorter, List<IssueDetector> issueDetectors) {
		// Collect all IssueList.id out of the IssueDetector list, but keep the information about the
		// IssueDetector/IssueList association
		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByListId(issueDetectors);
		Set<Long> issueListIds = issueDetectorByListId.keySet();
		List<Long> issueListIdsList = new ArrayList<Long>(issueListIds.size());
		issueListIdsList.addAll(issueListIds);

		// Find the list of Issues RemoteIds out of the list of IssueList.id , but keep the information about the
		// IssueList/remoteId association
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromIssuesLists(issueListIdsList,
				sorter, remoteBugTrackerService.getBugTrackerName());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		// Find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackerService.getIssues(issuesRemoteIds);

		// Bind the BT issues to their owner with the kept informations
		List<IssueOwnership<BTIssue>> ownerships = bindBTIssuesToOwner(btIssues, sortedIssueListIdsAndIssueRemoteIds,
				issueDetectorByListId);

		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIdsList,
				remoteBugTrackerService.getBugTrackerName());
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
}
