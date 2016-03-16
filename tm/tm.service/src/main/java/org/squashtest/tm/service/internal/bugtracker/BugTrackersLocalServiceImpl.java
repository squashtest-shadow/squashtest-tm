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
package org.squashtest.tm.service.internal.bugtracker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.bugtracker.*;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.IssueAlreadyBoundException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.BugTrackersLocalService")
public class BugTrackersLocalServiceImpl implements BugTrackersLocalService {


	@Value("${squashtm.bugtracker.timeout:15}")
	private long timeout;

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
	private IterationTestPlanDao iterationTestPlanDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private CampaignFolderDao campaignFolderDao;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private BugTrackerDao bugTrackerDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private IndexationService indexationService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private BugTrackerContextHolder contextHolder;

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
		return remoteBugTrackersService.getInterfaceDescriptor(bugTracker);
	}

	private LocaleContext getLocaleContext() {
		return LocaleContextHolder.getLocaleContext();
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public AuthenticationStatus checkBugTrackerStatus(Project project) {
		AuthenticationStatus status;

		if (!project.isBugtrackerConnected()) {
			status = AuthenticationStatus.UNDEFINED;
		} else if (remoteBugTrackersService.isCredentialsNeeded(project.findBugTracker())) {
			status = AuthenticationStatus.NON_AUTHENTICATED;
		} else {
			status = AuthenticationStatus.AUTHENTICATED;
		}
		return status;
	}

	@Override
	public AuthenticationStatus checkBugTrackerStatus(Long projectId) {
		Project project = projectDao.findById(projectId);
		return checkBugTrackerStatus(project);
	}

	@Override
	public AuthenticationStatus checkAuthenticationStatus(Long bugtrackerId) {
		AuthenticationStatus status;
		BugTracker bugtracker = bugTrackerDao.findById(bugtrackerId);
		if (bugtracker == null) {
			status = AuthenticationStatus.UNDEFINED;
		} else {
			boolean needs = remoteBugTrackersService.isCredentialsNeeded(bugtracker);
			status = (needs) ? AuthenticationStatus.NON_AUTHENTICATED : AuthenticationStatus.AUTHENTICATED;
		}
		return status;
	}

	private RemoteIssue createRemoteIssue(IssueDetector entity, RemoteIssue btIssue) {

		BugTracker bugTracker = entity.getBugTracker();
		String btName = bugTracker.getName();
		btIssue.setBugtracker(btName);

		RemoteIssue createdIssue = remoteBugTrackersService.createIssue(btIssue, bugTracker);
		createdIssue.setBugtracker(btName);

		return createdIssue;
	}

	@Override
	public RemoteIssue createIssue(IssueDetector entity, RemoteIssue btIssue) {

		RemoteIssue createdIssue = createRemoteIssue(entity, btIssue);
		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions
		BugTracker bugTracker = entity.getBugTracker();

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());
		sqIssue.setBugtracker(bugTracker);

		IssueList list = entity.getIssueList();

		list.addIssue(sqIssue);

		issueDao.persist(sqIssue);

		TestCase testCase = this.findTestCaseRelatedToIssue(sqIssue.getId());
		this.indexationService.reindexTestCase(testCase.getId());

		return createdIssue;
	}

	@Override
	public RemoteIssue getIssue(String issueKey, BugTracker bugTracker) {
		return remoteBugTrackersService.getIssue(issueKey, bugTracker);
	}

	@Override
	public List<RemoteIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker) {

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(issueKeyList, bugTracker, contextHolder.getContext(), getLocaleContext());
			return futureIssues.get(timeout, TimeUnit.SECONDS);
		} catch (TimeoutException timex) {
			throw new BugTrackerRemoteException(timex);
		} catch (InterruptedException | ExecutionException e) {
			throw new BugTrackerRemoteException(e.getCause());
		}

	}

	/* ************** delegate methods ************* */

	@Override
	public RemoteProject findRemoteProject(String name, BugTracker bugTracker) {
		return remoteBugTrackersService.findProject(name, bugTracker);

	}

	@Override
	public RemoteIssue createReportIssueTemplate(String projectName, BugTracker bugTracker) {
		return remoteBugTrackersService.createReportIssueTemplate(projectName, bugTracker);
	}

	@Override
	public void setCredentials(String username, String password, BugTracker bugTracker) {
		remoteBugTrackersService.setCredentials(username, password, bugTracker);
	}

	@Override
	public void setCredentials(String username, String password, Long bugtrackerId) throws BugTrackerRemoteException {
		BugTracker bugtracker = bugTrackerDao.findById(bugtrackerId);
		remoteBugTrackersService.setCredentials(username, password, bugtracker);
	}

	@Override
	public URL getIssueUrl(String btIssueId, BugTracker bugTracker) {
		return remoteBugTrackersService.getViewIssueUrl(btIssueId, bugTracker);
	}

	@Override
	public void forwardAttachments(String remoteIssueKey, String bugtrackerName, List<Attachment> attachments) {
		BugTracker bugtracker = bugTrackerDao.findByName(bugtrackerName); // NOTE : this may crash is multiple
		// bugtracker have the same name. One could
		// cross check with the remoteissuekey if
		// one day shit happened.
		remoteBugTrackersService.forwardAttachments(remoteIssueKey, bugtracker, attachments);
	}

	@Override
	public Object forwardDelegateCommand(DelegateCommand command, String bugtrackerName) {
		BugTracker bugtracker = bugTrackerDao.findByName(bugtrackerName);
		return remoteBugTrackersService.forwardDelegateCommand(command, bugtracker);
	}

	@Override
	@PreAuthorize("hasPermission(#bugged, 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public void attachIssue(IssueDetector bugged, String remoteIssueKey) {

		IssueList issueList = bugged.getIssueList();

		// check that the issue exists
		RemoteIssue test = getIssue(remoteIssueKey, bugged.getBugTracker());

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

			TestCase testCase = this.findTestCaseRelatedToIssue(issue.getId());
			this.indexationService.reindexTestCase(testCase.getId());

		}


	}

	@Override
	public void detachIssue(long id) {
		IssueDetector bugged = issueDao.findIssueDetectorByIssue(id);
		PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(bugged, "EXECUTE"));

		Issue issue = issueDao.findById(id);
		TestCase testCase = this.findTestCaseRelatedToIssue(issue.getId());
		issueDao.remove(issue);
		this.indexationService.reindexTestCase(testCase.getId());
	}

	/* ------------------------ExecutionStep--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#stepId, 'org.squashtest.tm.domain.execution.ExecutionStep', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnerShipsForExecutionStep(
		Long stepId, PagingAndSorting sorter) {

		ExecutionStep step = executionStepDao.findById(stepId);

		List<IssueDetector> detectors = new ArrayList<>(1);
		detectors.add(step);

		BugTracker bt = step.getBugTracker();

		return createOwnershipsCollection(sorter, detectors, bt);

	}

	@SuppressWarnings("unchecked")
	private List<RemoteIssueDecorator> decorateRemoteIssues(List<RemoteIssue> remoteIssues, MultiMap localIdsByRemoteId) {
		List<RemoteIssueDecorator> btIssueDecorators = new ArrayList<>(remoteIssues.size());

		for (RemoteIssue issue : remoteIssues) {
			Collection<Long> localIds = (Collection<Long>) localIdsByRemoteId.get(issue.getId());

			for (Long localId : localIds) {
				RemoteIssueDecorator issueDecorator = new RemoteIssueDecorator(issue, localId);
				btIssueDecorators.add(issueDecorator);
			}
		}
		return btIssueDecorators;
	}


	/* ------------------------Execution--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#execId, 'org.squashtest.tm.domain.execution.Execution', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsforExecution(
		Long execId, PagingAndSorting sorter) {
		// find bug-tracker
		Execution execution = executionDao.findById(execId);
		BugTracker bugTracker = execution.getBugTracker();

		// find all concerned IssueDetector
		List<IssueDetector> issueDetectors = executionDao.findAllIssueDetectorsForExecution(execId);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------TestSuite--------------------------------------- */
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForTestSuite(
		Long testSuiteId, PagingAndSorting sorter) {
		// find bug-tracker
		TestSuite testSuite = testSuiteDao.findById(testSuiteId);
		BugTracker bugTracker = testSuite.getIteration().getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = testSuiteDao.findAllExecutionByTestSuite(testSuiteId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------Iteration--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#iterId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForIteration(
		Long iterId, PagingAndSorting sorter) {
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
	@PreAuthorize("hasPermission(#campId, 'org.squashtest.tm.domain.campaign.Campaign' ,'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForCampaigns(
		Long campId, PagingAndSorting sorter) {
		// find bug-tracker
		Campaign campaign = campaignDao.findById(campId);
		BugTracker bugTracker = campaign.getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = campaignDao.findAllExecutionsByCampaignId(campId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bugTracker);
	}

	/* ------------------------- CampaignFolder ---------------------------------*/

	@Override
	@PreAuthorize("hasPermission(#cfId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForCampaignFolder(
		Long cfId, PagingAndSorting sorter) {

		// find bug-tracker
		CampaignFolder cf = campaignFolderDao.findById(cfId);
		BugTracker bt = cf.getProject().findBugTracker();

		// Find all concerned IssueDetector
		List<Execution> executions = campaignFolderDao.findAllExecutionsByCampaignFolder(cfId);
		List<IssueDetector> issueDetectors = collectIssueDetectorsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, issueDetectors, bt);

	}


	/* ------------------------TestCase--------------------------------------- */

	@Override
	@PreAuthorize("hasPermission(#tcId, 'org.squashtest.tm.domain.testcase.TestCase', 'READ')" + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForTestCase(
		Long tcId, PagingAndSorting sorter) {

		// Find all concerned IssueDetector
		List<Execution> executions = testCaseDao.findAllExecutionByTestCase(tcId);
		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		// create filtredCollection of IssueOwnership<BTIssue>
		return createOwnershipsCollection(sorter, executions, executionSteps);
	}


	@Override
	@PreAuthorize("hasPermission(#tcId, 'org.squashtest.tm.domain.testcase.TestCase', 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<IssueOwnership<RemoteIssueDecorator>> findIssueOwnershipForTestCase(long tcId) {

		// create filtredCollection of IssueOwnership<BTIssue>
		DefaultPagingAndSorting sorter = new DefaultPagingAndSorting("Issue.id", true);

		return findSortedIssueOwnershipForTestCase(tcId, sorter).getPagedItems();
	}


	/* ------------------------generic--------------------------------------- */

	@SuppressWarnings("unchecked")
	private List<ExecutionStep> collectExecutionStepsFromExecution(List<Execution> executions) {
		List<Long> execIds = (List<Long>) CollectionUtils.collect(executions, new IdCollector(), new ArrayList<Long>());
		return executionDao.findExecutionSteps(execIds);
	}

	private List<IssueDetector> collectIssueDetectorsFromExecution(List<Execution> executions) {

		List<ExecutionStep> steps = collectExecutionStepsFromExecution(executions);

		List<IssueDetector> detectors = new ArrayList<>(executions.size() + steps.size());
		detectors.addAll(executions);
		detectors.addAll(steps);

		return detectors;
	}

	/**
	 *
	 * @param sorter
	 * @param issueDetectors
	 * @param bugTracker
	 * @return the filtered collection holder of IssuesOwnerships
	 */
	@SuppressWarnings("unchecked")
	private PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> createOwnershipsCollection(
		PagingAndSorting sorter, List<IssueDetector> issueDetectors, BugTracker bugTracker) {

		// Collect all IssueList.id out of the IssueDetector list, but keep the information about the
		// IssueDetector/IssueList association
		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(issueDetectors);

		Set<Long> issueListIds = issueDetectorByListId.keySet();

		// Find the list of Issues RemoteIds out of the list of IssueList.id , but keep the information about the
		// IssueList/remoteId association
		List<Issue> localIssues = issueDao.findSortedIssuesFromIssuesLists(issueListIds,
			sorter, bugTracker.getId());

		MultiMap localIdsByRemoteId = mapLocalIssuesByRemoteIssue(localIssues);

		Collection<String> issuesRemoteIds = localIdsByRemoteId.keySet();

		// Find the BT issues out of the remote ids
		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(issuesRemoteIds, bugTracker, contextHolder.getContext(), getLocaleContext());
			List<RemoteIssue> btIssues = futureIssues.get(timeout, TimeUnit.SECONDS);

			List<RemoteIssueDecorator> btIssueDecorators = decorateRemoteIssues(btIssues, localIdsByRemoteId);

			// Bind the BT issues to their owner with the kept informations
			List<IssueOwnership<RemoteIssueDecorator>> ownerships = bindBTIssuesToOwner(btIssueDecorators,
				localIssues, issueDetectorByListId);

			Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIds, bugTracker.getId());
			return new PagingBackedPagedCollectionHolder<>(sorter, totalIssues,
				ownerships);

		} catch (TimeoutException | ExecutionException | InterruptedException ex) {
			throw new BugTrackerRemoteException(ex);
		}
	}

	/**
	 * Uses a list of tuples [issue list id, remote issue id, local issue id] and creates a map [remote issue id :
	 * [local issues ids]]
	 *
	 * @param issues
	 * @return
	 */
	private MultiMap mapLocalIssuesByRemoteIssue(List<Issue> issues) {
		MultiMap localIdsByRemoteId = new MultiValueMap();

		for (Issue issue : issues) {
			localIdsByRemoteId.put(issue.getRemoteIssueId(), issue.getId());
		}
		return localIdsByRemoteId;
	}


	/**
	 * This method is not the same as the
	 * {@linkplain BugTrackersLocalServiceImpl#createOwnershipsCollection(PagingAndSorting, List, BugTracker)} ,in the
	 * way that the restriction on the bug-tracker is done directly through each Execution of Execution-Step. The
	 * issue's bug-tracker id must be the same as the issue's owner's project's bug-tracker id. <br>
	 * With this request there is no need to check the project-bug-tracker connection before the request, and for a
	 * test-case, it can return a list of issues coming from different bug-trackers.
	 *
	 *
	 * @param sorter
	 * @param issueDetectors
	 * @return
	 */
	private PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> createOwnershipsCollection(
		PagingAndSorting sorter, List<Execution> executions, List<ExecutionStep> executionSteps) {

		// Keep the information about the IssueDetector/IssueList association
		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByIssueListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);

		// Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepsIds = IdentifiedUtil.extractIds(executionSteps);

		// Find the list of Issues RemoteIds/BugTrackerId out of the list of IssueList.id , but keep the information
		// about the
		// IssueList/remoteId association
		List<Issue> sortedIssueListIdsAndIssueRemoteIds = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(
			executionIds, executionStepsIds, sorter);

		MultiMap localIdsByRemoteId = mapLocalIssuesByRemoteIssue(sortedIssueListIdsAndIssueRemoteIds);

		// Find the BT issues out of the remote ids
		List<RemoteIssue> btIssues = findBTIssuesOutOfRemoteIdsAndBTIds(sortedIssueListIdsAndIssueRemoteIds);

		List<RemoteIssueDecorator> btIssueDecorators = decorateRemoteIssues(btIssues, localIdsByRemoteId);

		// Bind the BT issues to their owner with the kept informations
		List<IssueOwnership<RemoteIssueDecorator>> ownerships = bindBTIssuesToOwner(btIssueDecorators,
			sortedIssueListIdsAndIssueRemoteIds, issueDetectorByListId);

		Integer totalIssues = sortedIssueListIdsAndIssueRemoteIds.size();
		return new PagingBackedPagedCollectionHolder<>(sorter, totalIssues,
			ownerships);

	}

	private List<RemoteIssue> findBTIssuesOutOfRemoteIdsAndBTIds(List<Issue> sortedIssueListIdsAndIssueRemoteIds) {
		Map<BugTracker, List<String>> issueRemoteIdsByBugTrackers = organizeIssueRemoteIdsByBugTrackers(sortedIssueListIdsAndIssueRemoteIds);
		List<RemoteIssue> btIssues = new ArrayList<>();

		try {
			for (Entry<BugTracker, List<String>> remoteIdsByBugTracker : issueRemoteIdsByBugTrackers.entrySet()) {
				Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(
					remoteIdsByBugTracker.getValue(), remoteIdsByBugTracker.getKey(), contextHolder.getContext(), getLocaleContext());

				List<RemoteIssue> btIssuesOfBugTracker = futureIssues.get(timeout, TimeUnit.SECONDS);

				btIssues.addAll(btIssuesOfBugTracker);
			}
			return btIssues;
		} catch (TimeoutException | ExecutionException | InterruptedException timex) {
			throw new BugTrackerRemoteException(timex);
		}
	}

	private Map<BugTracker, List<String>> organizeIssueRemoteIdsByBugTrackers(
		List<Issue> sortedIssueListIdsAndIssueRemoteIds) {
		Map<Long, List<String>> organizedRemoteIds = organizeRemoteIdsByBugTrackerId(sortedIssueListIdsAndIssueRemoteIds);
		return replaceBugTrackerIdByBugtracker(organizedRemoteIds);
	}

	private Map<BugTracker, List<String>> replaceBugTrackerIdByBugtracker(Map<Long, List<String>> organizedRemoteIds) {
		Map<BugTracker, List<String>> remoteIdsByBugTracker = new HashMap<>(
			organizedRemoteIds.size());
		for (Entry<Long, List<String>> remoteIdsByBugTrackerId : organizedRemoteIds.entrySet()) {
			BugTracker bugTracker = bugTrackerDao.findById(remoteIdsByBugTrackerId.getKey());
			remoteIdsByBugTracker.put(bugTracker, remoteIdsByBugTrackerId.getValue());
		}
		return remoteIdsByBugTracker;
	}

	private Map<Long, List<String>> organizeRemoteIdsByBugTrackerId(List<Issue> sortedIssueListIdsAndIssueRemoteIds) {
		Map<Long, List<String>> organizedRemoteIds = new HashMap<>(
			sortedIssueListIdsAndIssueRemoteIds.size());
		for (Issue issue : sortedIssueListIdsAndIssueRemoteIds) {
			Long bugtrackerId = issue.getBugtracker().getId();
			List<String> remoteIds = organizedRemoteIds.get(bugtrackerId);
			if (remoteIds != null) {
				remoteIds.add(issue.getRemoteIssueId());
			} else {
				remoteIds = new ArrayList<>();
				remoteIds.add(issue.getRemoteIssueId());
				organizedRemoteIds.put(bugtrackerId, remoteIds);
			}
		}
		return organizedRemoteIds;
	}

	/**
	 * creates the map [detector.issueList.id : detector] from a list of detectors
	 *
	 * @param issueDetectors
	 * @return
	 */
	private Map<Long, IssueDetector> createIssueDetectorByIssueListId(List<? extends IssueDetector> issueDetectors) {
		Map<Long, IssueDetector> issueDetectorByListId = new HashMap<>();

		for (IssueDetector issueDetector : issueDetectors) {
			issueDetectorByListId.put(issueDetector.getIssueListId(), issueDetector);
		}
		return issueDetectorByListId;
	}

	private List<IssueOwnership<RemoteIssueDecorator>> bindBTIssuesToOwner(List<RemoteIssueDecorator> btIssues,
																		   List<Issue> issues, Map<Long, IssueDetector> issueDetectorByListId) {

		// we have as many bindings as IssueDetectors, hence the size
		List<IssueOwnership<RemoteIssueDecorator>> bindings = new ArrayList<>(
			issueDetectorByListId.size());

		Map<Long, RemoteIssueDecorator> remoteIssueByLocalId = mapRemoteIssueByLocalId(btIssues);

		for (Issue issue : issues) {
			Long listId = issue.getIssueList().getId();
			IssueDetector detector = issueDetectorByListId.get(listId);

			Long localId = issue.getId();
			RemoteIssueDecorator ish = remoteIssueByLocalId.get(localId);
			if (ish != null) {
				bindings.add(new IssueOwnership<>(ish, detector));
			}
		}

		return bindings;
	}

	/**
	 * Creates a map of [issueDecorator.issueId : issueDecorator]
	 *
	 * @param issues
	 * @return
	 */
	private Map<Long, RemoteIssueDecorator> mapRemoteIssueByLocalId(List<RemoteIssueDecorator> issues) {
		Map<Long, RemoteIssueDecorator> res = new HashMap<>(issues.size());

		for (RemoteIssueDecorator ish : issues) {
			res.put(ish.getIssueId(), ish);
		}

		return res;
	}


	@Override
	public Set<String> getProviderKinds() {
		return remoteBugTrackersService.getProviderKinds();
	}


	@Override
	public int findNumberOfIssueForTestCase(Long tcId) {

		// Find all concerned IssueDetector
		List<Execution> executions = testCaseDao.findAllExecutionByTestCase(tcId);
		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByIssueListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);

		// Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepIds = IdentifiedUtil.extractIds(executionSteps);

		return issueDao.countIssuesfromExecutionAndExecutionSteps(executionIds, executionStepIds);
	}

	@Override
	public int findNumberOfIssueForItemTestPlanLastExecution(Long itemTestPlanId) {

		IterationTestPlanItem itp = iterationTestPlanDao.findById(itemTestPlanId);
		Execution execution = itp.getLatestExecution();
		if (execution == null) {
			return 0;
		} else {
			List<Execution> executions = new ArrayList<>();
			executions.add(execution);
			return findNumberOfIssueForExecutions(executions);
		}
	}

	@Override
	public int findNumberOfIssueForExecutionStep(Long testStepId) {
		List<Long> executionStepIds = new ArrayList<>();
		executionStepIds.add(testStepId);
		return issueDao.countIssuesfromExecutionSteps(executionStepIds);
	}

	private int findNumberOfIssueForExecutions(List<Execution> executions) {

		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByIssueListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);

		// Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepIds = IdentifiedUtil.extractIds(executionSteps);

		return issueDao.countIssuesfromExecutionAndExecutionSteps(executionIds, executionStepIds);
	}

	@Override
	public TestCase findTestCaseRelatedToIssue(Long issueId) {
		return issueDao.findTestCaseRelatedToIssue(issueId);
	}

	@Override
	public Issue findIssueById(Long id) {
		return issueDao.findById(id);
	}

	@Override
	public List<Issue> getIssueList(String remoteid, String name) {
		BugTracker bugtracker = bugTrackerDao.findByName(name);
		if (bugtracker != null) {
			return issueDao.findIssueListByRemoteIssue(remoteid, bugtracker);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Execution findExecutionByIssueId(Long id) {
		return issueDao.findExecutionRelatedToIssue(id);
	}

	@Override
	public List<Execution> findExecutionsByRemoteIssue(String remoteid, String name) {
		List<Issue> issues = getIssueList(remoteid, name);
		List<Execution> executions = new ArrayList<>();
		for (Issue issue : issues) {
			Execution execution = issueDao.findExecutionRelatedToIssue(issue.getId());
			if (execution != null) {
				executions.add(execution);
			}
		}
		return executions;
	}

}
