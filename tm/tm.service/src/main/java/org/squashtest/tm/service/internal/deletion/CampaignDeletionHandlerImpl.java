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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

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
import org.squashtest.tm.service.deletion.NotDeletableCampaignsPreviewReport;
import org.squashtest.tm.service.deletion.OperationReport;
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

	@Override
	protected FolderDao<CampaignFolder, CampaignLibraryNode> getFolderDao() {
		return folderDao;
	}

	/* ************************** diagnostic section ******************************* */

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds) {

		List<SuppressionPreviewReport> reportList = new ArrayList<SuppressionPreviewReport>();
		NotDeletableCampaignsPreviewReport report;
		List<Campaign> campaigns = campaignDao.findAllByIds(nodeIds);

		//by default the user is assumed to be allowed to delete the campaigns without warning

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

		return reportList;
	}

	@Override
	public List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds) {

		List<SuppressionPreviewReport> reportList = new ArrayList<SuppressionPreviewReport>();
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

					//The user is not allowed to delete the campaign
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

		// TODO : implement the specs when they are ready. Default is "nothing special".
		return Collections.emptyList();
	}

	/* *************************locked entities detection section ******************* */

	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds) {

		List<Campaign> campaigns = campaignDao.findAllByIds(nodeIds);
		List<Long> lockedNodes = new ArrayList<Long>(nodeIds.size());

		for (Campaign campaign : campaigns) {

			if (campaignDao.countRunningOrDoneExecutions(campaign.getId()) > 0) {

				try {
					PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(campaign, "EXTENDED_DELETE"));
				} catch (AccessDeniedException exception) {
					lockedNodes.add(campaign.getId());
				}
			}
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
	protected OperationReport batchDeleteNodes(List<Long> ids) {

		//prepare the operation report:
		List<Long>[] separatedIds = deletionDao.separateFolderFromCampaignIds(ids);

		List<Campaign> campaigns = campaignDao.findAllByIds(ids);
		List<CampaignFolder> folders = folderDao.findAllByIds(ids);

		// saving the attachment list for later.
		List<AttachmentList> attachLists = new LinkedList<AttachmentList>();
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
	public OperationReport deleteIterations(List<Long> targetIds) {

		List<Iteration> iterations = iterationDao.findAllByIds(targetIds);
		List<Iteration> iterationsToBeDeleted = new ArrayList<Iteration>(iterations.size());
		List<Long> deletedTargetIds = new ArrayList<Long>(targetIds.size());

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

	@Override
	public OperationReport deleteSuites(List<Long> testSuites) {
		List<TestSuite> suites = suiteDao.findAllByIds(testSuites);

		doDeleteSuites(suites);

		OperationReport report = new OperationReport();
		report.addRemoved(testSuites, "test-suite");
		return report;

	}

	private void doDeleteSuites(Collection<TestSuite> testSuites) {
		List<Long> attachmentListIds = new ArrayList<Long>();

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

			Collection<TestSuite> suites = new ArrayList<TestSuite>();
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
		Collection<Execution> executionsCopy = new ArrayList<Execution>();
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

}
