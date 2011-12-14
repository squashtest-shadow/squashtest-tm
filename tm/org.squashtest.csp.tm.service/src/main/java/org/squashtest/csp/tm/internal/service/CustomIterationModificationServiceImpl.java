/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.CannotCreateExecutionException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao;
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.service.CustomIterationModificationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Service("CustomIterationModificationService")
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private ItemTestPlanDao testPlanDao;
	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public int addIterationToCampaign(Iteration iteration, long campaignId) {
		Campaign campaign = campaignDao.findById(campaignId);

		// copy the campaign test plan in the iteration

		List<CampaignTestPlanItem> tcList = campaign.getTestPlan();

		for (CampaignTestPlanItem tc : tcList) {
			IterationTestPlanItem iterTP = new IterationTestPlanItem(tc);
			testPlanDao.persist(iterTP);
			iteration.addTestPlan(iterTP);
		}

		iterationDao.persist(iteration);
		campaign.addIteration(iteration);
		return campaign.getIterations().size() - 1;
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return campaignDao.findByIdWithInitializedIterations(campaignId).getIterations();
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') " + "or hasRole('ROLE_ADMIN')")
	public Iteration findById(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public String delete(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		if (iteration == null) {
			return "ko";
		}

		iterationDao.removeFromCampaign(iteration);
		iterationDao.remove(iteration);

		return "ok";

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		iteration.setName(newName);
	}

	// FIXME : should be secured with a permission 'EXECUTION' when it's done
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addExecution(long iterationId, long testPlanId) {

		Iteration iteration = iterationDao.findAndInit(iterationId);
		IterationTestPlanItem testPlan = iteration.getTestPlan(testPlanId);

		if (testPlan.isTestCaseDeleted()) {
			throw new CannotCreateExecutionException();
		}

		TestCase testCase = testPlan.getReferencedTestCase();

		Execution execution = new Execution(testCase);

		// copy the steps
		for (TestStep step : testCase.getSteps()) {
			List<ExecutionStep> execList = step.getExecutionStep();
			for (ExecutionStep executionStep : execList) {
				executionStepDao.persist(executionStep);
				execution.addStep(executionStep);
			}
		}

		// copy the attachments
		for (Attachment tcAttach : testCase.getAllAttachments()) {
			Attachment clone = tcAttach.hardCopy();
			execution.getAttachmentList().addAttachment(clone);
		}

		executionDao.persist(execution);

		iteration.addExecution(execution);

	}

	/****
	 * Method which change the index of test case in the selected iteration
	 * 
	 * @param iterationId
	 *            the iteration at which the test case is attached
	 * @param testCaseId
	 *            the test case to move
	 * @param newTestCasePosition
	 *            the test case new position
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void changeTestPlanPosition(long iterationId, long testPlanId, int newTestPlanPosition) {

		Iteration iteration = iterationDao.findById(iterationId);

		int currentPosition = iteration.findTestPlanInIteration(testPlanId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("**************** change test case order : old index = " + currentPosition + ",new index : "
					+ newTestPlanPosition);
		}

		iteration.moveTestPlan(currentPosition, newTestPlanPosition);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Execution> findAllExecutions(long iterationId) {
		return iterationDao.findOrderedExecutionsByIterationId(iterationId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId) {
		return iterationDao.findOrderedExecutionsByIterationAndTestPlan(iterationId, testPlanId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<TestCase> findPlannedTestCases(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IterationTestPlanItem>> findIterationTestPlan(long iterationId,
			CollectionSorting filter) {
		List<IterationTestPlanItem> testPlan = iterationDao.findTestPlanFiltered(iterationId, filter);
		long count = iterationDao.countTestPlans(iterationId);
		return new FilteredCollectionHolder<List<IterationTestPlanItem>>(count, testPlan);
	}

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return deletionHandler.simulateIterationDeletion(targetIds);
	}

	@Override
	public List<Long> deleteNodes(List<Long> targetIds) {
		return deletionHandler.deleteIterations(targetIds);
	}
}
