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
package org.squashtest.tm.service.internal.bugtracker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.bugtracker.BTIssueDecorator;
import org.squashtest.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.IssueAlreadyBoundException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;

@Service("squashtest.tm.service.BugTrackersLocalService")
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
	
	@Inject
	private BugTrackerDao bugTrackerDao;
	
	@Inject
	private ProjectDao projectDao;

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
		return remoteBugTrackersService.getInterfaceDescriptor(bugTracker);
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE') or hasRole('ROLE_ADMIN')")	
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
	public BugTrackerStatus checkBugTrackerStatus(Long projectId) {
		Project project = projectDao.findById(projectId);
		return checkBugTrackerStatus(project);
	}

	@Override
	public BTIssue createIssue(IssueDetector entity, BTIssue btIssue) {
		BugTracker bugTracker = entity.getBugTracker();
		String btName = bugTracker.getName();
		btIssue.setBugtracker(btName);

		BTIssue createdIssue = remoteBugTrackersService.createIssue(btIssue, bugTracker);
		createdIssue.setBugtracker(btName);

		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());
		sqIssue.setBugtracker(bugTracker);

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
			issue.setBugtracker(bugged.getBugTracker());
			issue.setRemoteIssueId(test.getId());
			issueList.addIssue(issue);
			issueDao.persist(issue);

		}
	}
	
	@Override
	@PreAuthorize("hasPermission(#bugged, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public void detachIssue(Long id){
		
		Issue issue = issueDao.findById(id);
		issueDao.remove(issue);
	}

	/* ------------------------ExecutionStep--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#stepId, 'org.squashtest.tm.domain.execution.ExecutionStep', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnerShipsForExecutionStep(
			Long stepId, CollectionSorting sorter) {
		// find the ExecutionStep's IssueList id
		ExecutionStep executionStep = executionStepDao.findById(stepId);
		BugTracker bugTracker = executionStep.getBugTracker();
		List<Long> issueListId = Arrays.asList(executionStep.getIssueListId());

		// find the list of remoteIds out of the IssueListId
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromIssuesLists(issueListId,
				sorter, bugTracker.getId());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		//Get association between remote and local ids
		Map<String,Long> removeIdsLocalIdsMapping = new HashMap<String,Long>();
		for(Object[] issueTuple : sortedIssueListIdsAndIssueRemoteIds) {
			removeIdsLocalIdsMapping.put((String) issueTuple[1], (Long) issueTuple[2]);
		}
		
		// find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackersService.getIssues(issuesRemoteIds, bugTracker);

		//Build the final list
		List<BTIssueDecorator> btIssueDecorators = new ArrayList(btIssues.size());
		for(BTIssue issue : btIssues){
			BTIssueDecorator issueDecorator = new BTIssueDecorator(issue);
			issueDecorator.setIssueId(removeIdsLocalIdsMapping.get(issue.getId()));
			btIssueDecorators.add(issueDecorator);
		}
		
		// make a FiltredCollectionHolder of IssueOwnerShip
		List<IssueOwnership<BTIssueDecorator>> ownerships = bindBTIssuesToExecutionStep(btIssueDecorators, executionStep);
		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListId, bugTracker.getId());
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>>(totalIssues, ownerships);
	}

	private List<IssueOwnership<BTIssueDecorator>> bindBTIssuesToExecutionStep(List<BTIssueDecorator> btIssues,
			ExecutionStep executionStep) {
		List<IssueOwnership<BTIssueDecorator>> ownerships = new ArrayList<IssueOwnership<BTIssueDecorator>>(btIssues.size());
		for (BTIssueDecorator btIssue : btIssues) {
			IssueOwnership<BTIssueDecorator> ownership = new IssueOwnership<BTIssueDecorator>(btIssue, executionStep);
			ownerships.add(ownership);
		}
		return ownerships;
	}

	/* ------------------------Execution--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#execId, 'org.squashtest.tm.domain.execution.Execution', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnershipsforExecution(Long execId,
			CollectionSorting sorter) {
		// find bug-tracker
		Execution execution = executionDao.findById(execId);
		BugTracker bugTracker = execution.getBugTracker();

		// find all concerned IssueDetector
		List<IssueDetector> issueDetectors = executionDao.findAllIssueDetectorsForExecution(execId);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------Iteration--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#iterId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnershipForIteration(Long iterId,
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
	@PreAuthorize("hasPermission(#campId, 'org.squashtest.tm.domain.campaign.Campaign' ,'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnershipsForCampaigns(Long campId,
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
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnershipsForTestSuite(
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
	@PreAuthorize("hasPermission(#tcId, 'org.squashtest.tm.domain.testcase.TestCase', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> findSortedIssueOwnershipForTestCase(Long tcId,
				CollectionSorting sorter) {

		// Find all concerned IssueDetector
		List<Execution> executions = testCaseDao.findAllExecutionByTestCase(tcId);
		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, executions, executionSteps);
	}

	private List<ExecutionStep> collectExecutionStepsFromExecution(List<Execution> executions) {
		List<ExecutionStep> executionSteps = new ArrayList<ExecutionStep>();
		for(Execution execution : executions){
			executionSteps.addAll(execution.getSteps());
		}
		return executionSteps;
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

	/**
	 *  
	 * @param sorter
	 * @param issueDetectors
	 * @param bugTracker
	 * @return the filtered collection holder of IssuesOwnerships
	 */
	private FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> createOwnershipsCollection(
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
				sorter, bugTracker.getId());
		List<String> issuesRemoteIds = extractRemoteIds(sortedIssueListIdsAndIssueRemoteIds);

		//Get association between remote and local ids
		Map<String,Long> removeIdsLocalIdsMapping = new HashMap<String,Long>();
		for(Object[] issueTuple : sortedIssueListIdsAndIssueRemoteIds) {
			removeIdsLocalIdsMapping.put((String) issueTuple[1], (Long) issueTuple[2]);
		}
		
		// Find the BT issues out of the remote ids
		List<BTIssue> btIssues = remoteBugTrackersService.getIssues(issuesRemoteIds, bugTracker);

		//Build the final list
		List<BTIssueDecorator> btIssueDecorators = new ArrayList(btIssues.size());
		for(BTIssue issue : btIssues){
			BTIssueDecorator issueDecorator = new BTIssueDecorator(issue);
			issueDecorator.setIssueId(removeIdsLocalIdsMapping.get(issue.getId()));
			btIssueDecorators.add(issueDecorator);
		}
		
		// Bind the BT issues to their owner with the kept informations
		List<IssueOwnership<BTIssueDecorator>> ownerships = bindBTIssuesToOwner(btIssueDecorators, sortedIssueListIdsAndIssueRemoteIds,
				issueDetectorByListId);

		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIdsList, bugTracker.getId());
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>>(totalIssues, ownerships);
	}

	/**
	 * This method is not the same as the  {@linkplain BugTrackersLocalServiceImpl#createOwnershipsCollection(CollectionSorting, List, BugTracker)} ,in the
	 * way that the restriction on the bug-tracker is done directly through each Execution of Execution-Step. The
	 * issue's bug-tracker id must be the same as the issue's owner's project's bug-tracker id. <br>
	 * With this request there is no need to check the project-bug-tracker connection before
	 * the request, and for a test-case, it can return a list of issues coming from different bug-trackers.
	 * 
	 * 
	 * @param sorter
	 * @param issueDetectors
	 * @return
	 */
	private FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>> createOwnershipsCollection(
			CollectionSorting sorter, List<Execution> executions, List<ExecutionStep> executionSteps) {
		// Keep the information about the
		// IssueDetector/IssueList association
		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);
		
		//Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepsIds = IdentifiedUtil.extractIds(executionSteps);
		
		// Find the list of Issues RemoteIds/BugTrackerId out of the list of IssueList.id , but keep the information about the
		// IssueList/remoteId association
		List<Object[]> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(executionIds,
				executionStepsIds, sorter);
	
		//Get association between remote and local ids
		Map<String,Long> removeIdsLocalIdsMapping = new HashMap<String,Long>();
		for(Object[] issueTuple : sortedIssueListIdsAndIssueRemoteIds) {
			removeIdsLocalIdsMapping.put((String) issueTuple[1], (Long) issueTuple[2]);
		}
		
		// Find the BT issues out of the remote ids
		List<BTIssue> btIssues = findBTIssuesOutOfRemoteIdsAndBTIds(sortedIssueListIdsAndIssueRemoteIds);		
	
		//Build the final list
		List<BTIssueDecorator> btIssueDecorators = new ArrayList(btIssues.size());
		for(BTIssue issue : btIssues){
			BTIssueDecorator issueDecorator = new BTIssueDecorator(issue);
			issueDecorator.setIssueId(removeIdsLocalIdsMapping.get(issue.getId()));
			btIssueDecorators.add(issueDecorator);
		}
		
		// Bind the BT issues to their owner with the kept informations
		List<IssueOwnership<BTIssueDecorator>> ownerships = bindBTIssuesToOwner(btIssueDecorators, sortedIssueListIdsAndIssueRemoteIds,
				issueDetectorByListId);
		
		Integer totalIssues = issueDao.countIssuesfromExecutionAndExecutionSteps(executionIds, executionStepsIds);
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssueDecorator>>>(totalIssues, ownerships);
		
	}

	private List<BTIssue> findBTIssuesOutOfRemoteIdsAndBTIds(List<Object[]> sortedIssueListIdsAndIssueRemoteIds) {
		Map<BugTracker, List<String>> issueRemoteIdsByBugTrackers = organizeIssueRemoteIdsByBugTrackers(sortedIssueListIdsAndIssueRemoteIds);
		List<BTIssue> btIssues = new ArrayList<BTIssue>();
		for(Entry<BugTracker, List<String>> remoteIdsByBugTracker : issueRemoteIdsByBugTrackers.entrySet()){
			List<BTIssue> btIssuesOfBugTracker = remoteBugTrackersService.getIssues(remoteIdsByBugTracker.getValue(), remoteIdsByBugTracker.getKey());
			btIssues.addAll(btIssuesOfBugTracker);
		}
		return btIssues;
	}

	private Map<BugTracker, List<String>> organizeIssueRemoteIdsByBugTrackers(
			List<Object[]> sortedIssueListIdsAndIssueRemoteIds) {
		Map<Long, List<String>> organizedRemoteIds = organizeRemoteIdsByBugTrackerId(sortedIssueListIdsAndIssueRemoteIds);
		Map<BugTracker, List<String>> remoteIdsByBugTracker = replaceBugTrackerIdByBugtracker(organizedRemoteIds);
		return remoteIdsByBugTracker;
	}

	private Map<BugTracker, List<String>> replaceBugTrackerIdByBugtracker(Map<Long, List<String>> organizedRemoteIds) {
		Map<BugTracker, List<String>> remoteIdsByBugTracker = new HashMap<BugTracker, List<String>>(organizedRemoteIds.size());
		for(Entry<Long, List<String>> remoteIdsByBugTrackerId : organizedRemoteIds.entrySet()){
			BugTracker bugTracker = bugTrackerDao.findById(remoteIdsByBugTrackerId.getKey());
			remoteIdsByBugTracker.put(bugTracker, remoteIdsByBugTrackerId.getValue());
		}
		return remoteIdsByBugTracker;
	}

	private Map<Long, List<String>> organizeRemoteIdsByBugTrackerId(List<Object[]> sortedIssueListIdsAndIssueRemoteIds) {
		Map<Long, List<String>> organizedRemoteIds = new HashMap<Long, List<String>>(sortedIssueListIdsAndIssueRemoteIds.size());
		for(Object[] object : sortedIssueListIdsAndIssueRemoteIds){
			Long bugtrackerId = (Long) object[2];
			List<String> remoteIds = organizedRemoteIds.get(bugtrackerId);
			if( remoteIds != null){
				remoteIds.add((String) object[1]);
			}else{
				remoteIds = new ArrayList<String>();
				remoteIds.add((String) object[1]);
				organizedRemoteIds.put(bugtrackerId, remoteIds);
			}
		}
		return organizedRemoteIds;
	}

	private Map<Long, IssueDetector> createIssueDetectorByListId(List<? extends IssueDetector> issueDetectors) {
		Map<Long, IssueDetector> issueDetectorByListId = new HashMap<Long, IssueDetector>();
		for (IssueDetector issueDetector : issueDetectors) {
			issueDetectorByListId.put(issueDetector.getIssueListId(), issueDetector);
		}
		return issueDetectorByListId;
	}

	private List<IssueOwnership<BTIssueDecorator>> bindBTIssuesToOwner(List<BTIssueDecorator> btIssues,
			List<Object[]> sortedIssueListIdsAndIssueRemoteIds, Map<Long, IssueDetector> issueDetectorByListId) {
		List<IssueOwnership<BTIssueDecorator>> ownerships = new ArrayList<IssueOwnership<BTIssueDecorator>>(btIssues.size());
		Map<String, Long> listIdByRemoteId = createMapOutOfList(sortedIssueListIdsAndIssueRemoteIds);
		for (BTIssueDecorator btIssue : btIssues) {
			Long listId = listIdByRemoteId.get(btIssue.getId());
			IssueDetector owner = issueDetectorByListId.get(listId);
			IssueOwnership<BTIssueDecorator> ownership = new IssueOwnership<BTIssueDecorator>(btIssue, owner);
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
