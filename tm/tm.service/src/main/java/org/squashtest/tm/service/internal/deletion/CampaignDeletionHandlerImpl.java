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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.deletion.BoundToLockedMilestonesReport;
import org.squashtest.tm.service.deletion.BoundToMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.BoundToNotSelectedTestSuite;
import org.squashtest.tm.service.deletion.MilestoneModeNoFolderDeletion;
import org.squashtest.tm.service.deletion.NotDeletableCampaignsPreviewReport;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SingleOrMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignDeletionDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

@Component("squashtest.tm.service.deletion.CampaignNodeDeletionHandler")
public class CampaignDeletionHandlerImpl extends AbstractNodeDeletionHandler<CampaignLibraryNode, CampaignFolder>
	implements CampaignNodeDeletionHandler {

	private static final String CAMPAIGNS_TYPE = "campaigns";
	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignDeletionHandlerImpl.class);

	@Inject
	private CampaignFolderDao folderDao;

	@Inject
	private CampaignDeletionDao deletionDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private TestSuiteDao suiteDao;

	@Inject
	private AutomatedTestDao autoTestDao;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private PrivateDenormalizedFieldValueService denormalizedFieldValueService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Override
	protected FolderDao<CampaignFolder, CampaignLibraryNode> getFolderDao() {
		return folderDao;
	}

	/* ************************** diagnostic section ******************************* */

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds, Long milestoneId) {

		List<SuppressionPreviewReport> reportList = new ArrayList<>();

		List<Campaign> campaigns = campaignDao.findAllByIds(nodeIds);

		//by default the user is assumed to be allowed to delete the campaigns without warning
		reportLocksByInsufficientPrivileges(campaigns, reportList);

		// always check for nodes locked by milestones
		reportLocksByMilestone(nodeIds, reportList);

		// milestone mode : additional checks
		if (milestoneId != null) {

			// separate the campaigns from the folders
			List<Long>[] separateIds = deletionDao.separateFolderFromCampaignIds(nodeIds);

			// no folder shall be deleted
			reportNoFoldersAllowed(separateIds[0], reportList);

			// check if some elements are bound to multiple milestones
			reportMultipleMilestoneBinding(separateIds[1], reportList);
		}

		return reportList;
	}

	protected void reportMultipleMilestoneBinding(List<Long> campaignIds, List<SuppressionPreviewReport> reportList) {
		List<Long> boundNodes = campaignDao.findCampaignIdsHavingMultipleMilestones(campaignIds);
		if (!boundNodes.isEmpty()) {
			// case 1 : all the test cases are bound to multiple milestones
			if (campaignIds.size() == boundNodes.size()) {
				reportList.add(new BoundToMultipleMilestonesReport(CAMPAIGNS_TYPE));
			}
			// case 2 : there is a mixed cases of test cases that will be removed
			// from the milestone and some will be removed - period
			else {
				reportList.add(new SingleOrMultipleMilestonesReport(CAMPAIGNS_TYPE));
			}
		}
	}

	protected void reportNoFoldersAllowed(List<Long> folderIds, List<SuppressionPreviewReport> reportList) {
		if (!folderIds.isEmpty()) {
			reportList.add(new MilestoneModeNoFolderDeletion(CAMPAIGNS_TYPE));
		}
	}


	protected void reportLocksByMilestone(List<Long> nodeIds, List<SuppressionPreviewReport> reportList) {
		List<Long> lockedNodes = deletionDao.findCampaignsWhichMilestonesForbidsDeletion(nodeIds);
		if (!lockedNodes.isEmpty()) {
			reportList.add(new BoundToLockedMilestonesReport(CAMPAIGNS_TYPE));
		}
	}


	protected void reportLocksByInsufficientPrivileges(List<Campaign> campaigns,
													   List<SuppressionPreviewReport> reportList) {
		NotDeletableCampaignsPreviewReport report;
		for (Campaign campaign : campaigns) {

			if (campaignDao.countRunningOrDoneExecutions(campaign.getId()) > 0) {

				try {
					PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(campaign, "EXTENDED_DELETE"));

					//The user is allowed to delete the campaign but must be warned
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(campaign.getName());
					report.setHasRights(true);
					reportList.add(report);
				} catch (AccessDeniedException exception) {

					//The user is not allowed to delete the campaign
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(campaign.getName());
					report.setHasRights(false);
					reportList.add(report);
				}

			}
		}
	}

	@Override
	public List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds) {

		List<SuppressionPreviewReport> reportList = new ArrayList<>();
		NotDeletableCampaignsPreviewReport report;
		List<Iteration> iterations = iterationDao.findAllByIds(targetIds);

		//by default the user is assumed to be allowed to delete the iterations without warning

		for (Iteration iteration : iterations) {

			if (iterationDao.countRunningOrDoneExecutions(iteration.getId()) > 0) {

				try {
					PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(iteration, "EXTENDED_DELETE"));

					//The user is allowed to delete the campaign but must be warned
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(iteration.getName());
					report.setHasRights(true);
					reportList.add(report);
				} catch (AccessDeniedException exception) {
					LOGGER.trace("The user is not allowed to delete the campaign");
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(iteration.getName());
					report.setHasRights(false);
					reportList.add(report);
				}
			}
		}

		return reportList;
	}

	@Override
	public List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId) {

		// TODO : implement the specs when they are ready. Default is "nothing special".
		return Collections.emptyList();
	}

	@Override
	public List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds) {

		List<SuppressionPreviewReport> reportList = new ArrayList<>();

		List<TestSuite> suites = suiteDao.findAllByIds(targetIds);

		// Check that test case do not belong to other suite that is not in the selection

		if (containTestPlanItemThatBelongToOtherTestSuite(suites, targetIds)) {
			reportList.add(new BoundToNotSelectedTestSuite());
		}

		// check test case execution
		addTcExecutionErrorToReport(reportList, suites);

		return reportList;
	}

	private void addTcExecutionErrorToReport(List<SuppressionPreviewReport> reportList, List<TestSuite> suites) {

		NotDeletableCampaignsPreviewReport report;
		for (TestSuite suite : suites) {
			if (containExecutedTc(suite)) {
				try {
					PermissionsUtils.checkPermission(permissionEvaluationService,
						new SecurityCheckableObject(suite, "EXTENDED_DELETE"));

					// The user is allowed to delete the campaign but must be warned
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(suite.getName());
					report.setHasRights(true);
					reportList.add(report);
				} catch (AccessDeniedException exception) {

					// The user is not allowed to delete the campaign
					report = new NotDeletableCampaignsPreviewReport();
					report.addName(suite.getName());
					report.setHasRights(false);
					reportList.add(report);
				}

			}

		}

	}

	private boolean containExecutedTc(TestSuite suite) {

		for (IterationTestPlanItem itpi : suite.getTestPlan()) {

			if (itpi.getExecutions().size() > 0) {
				return true;
			}

		}

		return false;
	}

	private boolean containTestPlanItemThatBelongToOtherTestSuite(List<TestSuite> suites, List<Long> targetIds) {

		for (TestSuite suite : suites) {

			for (IterationTestPlanItem itpi : suite.getTestPlan()) {

				for (TestSuite ts : itpi.getTestSuites()) {

					if (!targetIds.contains(ts.getId())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/* *************************locked entities detection section ******************* */

	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds, Long milestoneId) {

		List<Campaign> campaigns = campaignDao.findAllByIds(nodeIds);
		List<Long> lockedNodes = new ArrayList<>(nodeIds.size());

		for (Campaign campaign : campaigns) {

			if (campaignDao.countRunningOrDoneExecutions(campaign.getId()) > 0) {
				try {
					PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(campaign, "EXTENDED_DELETE"));
				} catch (AccessDeniedException exception) {
					lockedNodes.add(campaign.getId());
				}
			}
		}


		// check for locked campaigns
		List<Long> lockedByMilestones = deletionDao.findCampaignsWhichMilestonesForbidsDeletion(nodeIds);
		lockedNodes.addAll(lockedByMilestones);

		/*
		 * milestone mode provides additional checks :
		 * - 1) no folder shall be deleted (enqueued outright)
		 * - 2) no campaign that doesn't belong to the milestone shall be deleted
		 * - 3) no campaign bound to more than one milestone shall be deleted (they will be unbound though, but later).
		 */
		if (milestoneId != null) {

			// 1 - no folder shall be deleted
			List<Long> folderIds = deletionDao.separateFolderFromCampaignIds(nodeIds)[0];

			// 2 - no campaign that aren't bound to the current milestone shall be deleted
			List<Long> notBoundToMilestone = campaignDao.findNonBoundCampaign(nodeIds, milestoneId);

			// 3 - no campaign bound to more than one milestone shall be deleted
			List<Long> boundToMoreMilestones = campaignDao.findCampaignIdsHavingMultipleMilestones(nodeIds);

			lockedNodes.addAll(folderIds);
			lockedNodes.addAll(boundToMoreMilestones);
			lockedNodes.addAll(notBoundToMilestone);

		}

		return lockedNodes;
	}

	/* *********************************************************************************
	 * deletion section
	 *
	 * Sorry, no time to implement something smarter. Maybe in future releases ?
	 *
	 *
	 * TODO : - implement a careful deletion procedure once the policies and rules are defined. - improve code
	 * efficiency.
	 *
	 * ******************************************************************************
	 */

	@Override
	/*
	 * by Nodes we mean the CampaignLibraryNodes.
	 */
	protected OperationReport batchDeleteNodes(List<Long> ids, Long milestoneId) {

		//prepare the operation report:
		List<Long>[] separatedIds = deletionDao.separateFolderFromCampaignIds(ids);

		List<Campaign> campaigns = campaignDao.findAllByIds(ids);
		List<CampaignFolder> folders = folderDao.findAllByIds(ids);

		// saving the attachment list for later.
		List<AttachmentList> attachLists = new LinkedList<>();
		for (Campaign campaign : campaigns) {
			attachLists.add(campaign.getAttachmentList());
		}
		for (CampaignFolder folder : folders) {
			attachLists.add(folder.getAttachmentList());
		}

		//empty of those campaigns
		deleteCampaignContent(campaigns);

		/*
		 * a flush is needed at this point because all operations above where performed by Hibernate, while the rest
		 * will be executed using SQL queries. The inconsistencies between cached entities but not yet flushed entities
		 * and the actual database content would make the operation crash, so we need to synchronize.
		 */
		deletionDao.flush();

		// now we can delete the folders as well
		deletionDao.removeEntities(ids);

		// only now we can delete the attachments according to the fk constraints
		for (AttachmentList list : attachLists) {
			deletionDao.removeAttachmentList(list);
		}


		//and finally prepare the operation report.
		OperationReport report = new OperationReport();
		report.addRemoved(separatedIds[0], "folder");
		report.addRemoved(separatedIds[1], "campaign");

		return report;
	}


	@Override
	protected OperationReport batchUnbindFromMilestone(List<Long> ids, Long milestoneId) {

		List<Long> remainingIds = deletionDao.findRemainingCampaignIds(ids);

		// some node should not be unbound.
		List<Long> lockedIds = deletionDao.findCampaignsWhichMilestonesForbidsDeletion(remainingIds);
		remainingIds.removeAll(lockedIds);

		OperationReport report = new OperationReport();

		deletionDao.unbindFromMilestone(remainingIds, milestoneId);

		report.addRemoved(remainingIds, "campaign");

		return report;
	}


	@Override
	public OperationReport deleteIterations(List<Long> targetIds) {

		List<Iteration> iterations = iterationDao.findAllByIds(targetIds);
		List<Iteration> iterationsToBeDeleted = new ArrayList<>(iterations.size());
		List<Long> deletedTargetIds = new ArrayList<>(targetIds.size());

		for (Iteration iteration : iterations) {

			if (iterationDao.countRunningOrDoneExecutions(iteration.getId()) > 0) {

				try {
					PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(iteration, "EXTENDED_DELETE"));
					registerIterationDeletion(iteration, iterationsToBeDeleted, deletedTargetIds);
				} catch (AccessDeniedException exception) {
					// Apparently, we don't wanna do anything, not even log something.
				}
			} else {
				registerIterationDeletion(iteration, iterationsToBeDeleted, deletedTargetIds);
			}
		}

		doDeleteIterations(iterationsToBeDeleted);

		OperationReport report = new OperationReport();
		report.addRemoved(deletedTargetIds, "iteration");

		return report;
	}

	private void registerIterationDeletion(Iteration iteration, List<Iteration> iterationsToBeDeleted, List<Long> deletedTargetIds) {
		Campaign camp = iteration.getCampaign();
		camp.removeIteration(iteration);
		iterationsToBeDeleted.add(iteration);
		deletedTargetIds.add(iteration.getId());
	}

	private void doDeleteSuites(Collection<TestSuite> testSuites) {
		List<Long> attachmentListIds = new ArrayList<>();

		for (TestSuite testSuite : testSuites) {
			attachmentListIds.add(testSuite.getAttachmentList().getId());
			for (IterationTestPlanItem testPlanItem : testSuite.getTestPlan()) {
				testPlanItem.getTestSuites().clear();
			}
			testSuite.getIteration().removeTestSuite(testSuite);

			customValueService.deleteAllCustomFieldValues(testSuite);

			deletionDao.removeEntity(testSuite);
		}

		deletionDao.flush();
		deletionDao.removeAttachmentsLists(attachmentListIds);
	}

	@Override
	public void deleteExecution(Execution execution) {
		deleteExecSteps(execution);

		IterationTestPlanItem testPlanItem = execution.getTestPlan();
		testPlanItem.removeExecution(execution);
		deleteAutomatedExecutionExtender(execution);

		denormalizedFieldValueService.deleteAllDenormalizedFieldValues(execution);
		customValueService.deleteAllCustomFieldValues(execution);
		deletionDao.removeAttachmentList(execution.getAttachmentList());
		deletionDao.removeEntity(execution);
	}

	/*
	 * we just remove the content of a campaign here. The actual removal of the campaign will be processed in the
	 * calling methods.
	 *
	 * The operations carried over a campaign are : - removal of all its iterations, - removal its attachment list,
	 *
	 * the rest is supposed to cascade normally (node hierarchy, campaign test plans).
	 */

	private void deleteCampaignContent(List<Campaign> campaigns) {

		for (Campaign campaign : campaigns) {
			deleteCampaignTestPlan(campaign.getTestPlan());
			campaign.getTestPlan().clear();

			doDeleteIterations(campaign.getIterations());
			campaign.getIterations().clear();

			customValueService.deleteAllCustomFieldValues(campaign);
		}

	}

	private void deleteCampaignTestPlan(List<CampaignTestPlanItem> itemList) {
		for (CampaignTestPlanItem item : itemList) {
			deletionDao.removeEntity(item);
		}
	}

	/*
	 * removing an iteration means : - removing its test plan, - removing its attachment list - remove itself from
	 * repository.
	 */
	private void doDeleteIterations(List<Iteration> iterations) {
		for (Iteration iteration : iterations) {

			Collection<TestSuite> suites = new ArrayList<>();
			suites.addAll(iteration.getTestSuites());

			doDeleteSuites(suites);
			iteration.getTestSuites().clear();

			deleteIterationTestPlan(iteration.getTestPlans());
			iteration.getTestSuites().clear();    //XXX isn't that supposed to be iteration.getTestPlans().clear();

			customValueService.deleteAllCustomFieldValues(iteration);

			deletionDao.removeAttachmentList(iteration.getAttachmentList());
			deletionDao.removeEntity(iteration);
		}
	}

	/*
	 * removing a test plan :
	 *
	 * - remove the executions - remove itself.
	 */
	private void deleteIterationTestPlan(List<IterationTestPlanItem> testPlan) {
		for (IterationTestPlanItem item : testPlan) {
			deleteIterationTestPlanItem(item);
		}
	}


	@Override
	public void deleteIterationTestPlanItem(IterationTestPlanItem item) {
		deleteExecutions(item.getExecutions());
		deletionDao.removeEntity(item);
	}

	/*
	 *
	 */
	@Override
	public void deleteExecutions(List<Execution> executions) {
		Collection<Execution> executionsCopy = new ArrayList<>();
		executionsCopy.addAll(executions);
		for (Execution execution : executionsCopy) {
			deleteExecution(execution);
		}
	}

	/*
	 * removing the steps mean : - remove their issues, - remove their attachments, - remove themselves.
	 */
	public void deleteExecSteps(Execution execution) {

		for (ExecutionStep step : execution.getSteps()) {

			deletionDao.removeAttachmentList(step.getAttachmentList());
			denormalizedFieldValueService.deleteAllDenormalizedFieldValues(step);
			customValueService.deleteAllCustomFieldValues(step);
			deletionDao.removeEntity(step);
		}

		execution.getSteps().clear();
	}

	private void deleteAutomatedExecutionExtender(Execution execution) {
		if (execution.getAutomatedExecutionExtender() != null) {
			AutomatedExecutionExtender extender = execution.getAutomatedExecutionExtender();
			autoTestDao.removeIfUnused(extender.getAutomatedTest());
			deletionDao.removeEntity(extender);
			execution.setAutomatedExecutionExtender(null);
		}
	}

	@Override
	public OperationReport deleteSuites(List<Long> suiteIds, boolean removeFromIter) {
		List<TestSuite> suites = suiteDao.findAllByIds(suiteIds);

		if (removeFromIter) {
			removeItpiFromIteration(suites, suiteIds);
		}

		doDeleteSuites(suites);

		OperationReport report = new OperationReport();
		report.addRemoved(suiteIds, "test-suite");
		return report;

	}

	private void removeItpiFromIteration(List<TestSuite> suites, final List<Long> targetIds) {

		Set<Long> idsToRemove = new HashSet<>();

		for (TestSuite suite : suites) {

			for (IterationTestPlanItem itpi : suite.getTestPlan()) {

				// Try to find one test suite in the IterationTestPlanItem that is not in the list of selected Iteration
				Object result = CollectionUtils.find(itpi.getTestSuites(), new Predicate() {
					@Override
					public boolean evaluate(Object ts) {
						TestSuite testSuite = (TestSuite) ts;
						return !targetIds.contains(testSuite.getId());
					}
				});

				// If nothing if found (ie : the iteration test plan item only belong to test suite that are selected)
				// we want to remove it from the iteration.
				if (result == null) {
					idsToRemove.add(itpi.getId());
				}

			}
		}

		for (Long id : idsToRemove) {
			iterationTestPlanManagerService.removeTestPlanFromIteration(id);
		}

	}

}
