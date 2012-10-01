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
package org.squashtest.csp.tm.internal.service.deletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.CampaignDeletionDao;
import org.squashtest.csp.tm.internal.repository.CampaignFolderDao;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.CampaignNodeDeletionHandler;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Component("squashtest.tm.service.deletion.CampaignNodeDeletionHandler")
public class CampaignDeletionHandlerImpl extends AbstractNodeDeletionHandlerImpl<CampaignLibraryNode, CampaignFolder>
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

	@Override
	protected FolderDao<CampaignFolder, CampaignLibraryNode> getFolderDao() {
		return folderDao;
	}

	/* ************************** diagnostic section ******************************* */

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds) {

		// TODO : implement the specs when they are ready. Default is "nothing special".
		return Collections.emptyList();
	}

	@Override
	public List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds) {

		// TODO : implement the specs when they are ready. Default is "nothing special".
		return Collections.emptyList();
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

		// TODO : implement the specs when they are ready. Default is "no lock detected".
		return Collections.emptyList();

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
	protected void batchDeleteNodes(List<Long> ids) {
		List<Campaign> campaigns = campaignDao.findAllByIds(ids);

		// saving the attachment list for later.
		List<AttachmentList> attachLists = new LinkedList<AttachmentList>();
		for (Campaign campaign : campaigns) {
			attachLists.add(campaign.getAttachmentList());
		}

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

	}

	@Override
	public List<Long> deleteIterations(List<Long> targetIds) {

		List<Iteration> iterations = iterationDao.findAllByIds(targetIds);

		for (Iteration iteration : iterations) {
			iteration.getCampaign().removeIteration(iteration);
		}

		doDeleteIterations(iterations);

		return targetIds;

	}

	@Override
	public List<Long> deleteSuites(List<Long> testSuites) {
		List<TestSuite> suites = suiteDao.findAllByIds(testSuites);

		doDeleteSuites(suites);

		return testSuites;

	}

	private void doDeleteSuites(Collection<TestSuite> testSuites) {
		List<Long> attachmentListIds = new ArrayList<Long>();

		for (TestSuite testSuite : testSuites) {
			attachmentListIds.add(testSuite.getAttachmentList().getId());
			for (IterationTestPlanItem testPlanItem : testSuite.getTestPlan()) {
				testPlanItem.setTestSuite(null);
			}
			testSuite.getIteration().removeTestSuite(testSuite);
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
			iteration.getTestSuites().clear();	//XXX isn't that supposed to be iteration.getTestPlans().clear();

			deletionDao.removeAttachmentList(iteration.getAttachmentList());
			deletionDao.removeEntity(iteration);
		}
	}

	/*
	 * removing a test plan :
	 * 
	 * - remove the executions - remove itself.
	 */
	private void deleteIterationTestPlan(List<IterationTestPlanItem> testPlans) {
		for (IterationTestPlanItem testPlan : testPlans) {
			deleteExecutions(testPlan.getExecutions());
			deletionDao.removeEntity(testPlan);
		}
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
			deletionDao.removeEntity(step);
		}

		execution.getSteps().clear();
	}

	private void deleteAutomatedExecutionExtender(Execution execution){
		if (execution.getAutomatedExecutionExtender()!=null){
			deletionDao.removeEntity(execution.getAutomatedExecutionExtender());
		}
	}

}
