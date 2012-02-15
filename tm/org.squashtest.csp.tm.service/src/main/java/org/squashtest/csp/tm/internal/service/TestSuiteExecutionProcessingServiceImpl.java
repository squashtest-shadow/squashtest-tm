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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

@Service
public class TestSuiteExecutionProcessingServiceImpl implements TestSuiteExecutionProcessingService {
	@Inject
	private TestSuiteDao suiteDao;
	@Inject
	private IterationModificationService iterationModificationService;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")
	public ExecutionStep findExecutionStepWhereToResumeExecutionOfSuite(Long testSuiteId) {
		List<IterationTestPlanItem> testSuiteResumableTestPlan = suiteDao.findLaunchableTestPlan(testSuiteId);
		if (!testSuiteResumableTestPlan.isEmpty()) {
			return findExecutionStepWhereToResumeExecutionOfTestPlan(testSuiteResumableTestPlan);
		}
		return null;
	}

	private ExecutionStep findFirstUnexecutedStepOfLastExecution(List<Execution> executions) {
		Execution lastExecution = executions.get(executions.size() - 1);
		ExecutionStep step = lastExecution.findFirstUnexecutedStep();
		return step;
	}

	private ExecutionStep findExecutionStepWhereToResumeExecutionOfTestPlan(
			List<IterationTestPlanItem> testSuiteResumableTestPlan) {
		ExecutionStep executionStep = null;
		for (IterationTestPlanItem testPlanItem : testSuiteResumableTestPlan) {
			List<Execution> executions = testPlanItem.getExecutions();
			executionStep = findFirstUnexecutedOrCreateExecution(executions, testPlanItem);
			if (executionStep != null) {
				break;
			}
		}
		return executionStep;
	}

	private ExecutionStep findFirstUnexecutedOrCreateExecution(List<Execution> executions,
			IterationTestPlanItem testPlanItem) {
		ExecutionStep executionStep = null;
		if (!executions.isEmpty()) {
			executionStep = findFirstUnexecutedStepOfLastExecution(executions);

		} else {
			if (!testPlanItem.isTestCaseDeleted()) {
				Execution execution = iterationModificationService.addExecution(testPlanItem.getIteration().getId(),
						testPlanItem.getId());
				if (!execution.getSteps().isEmpty()) {
					executionStep = execution.getSteps().get(0);
				}
			}
		}
		return executionStep;
	}

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")
	public ExecutionStep relaunchExecution(Long testSuiteId) {
		// getTest plan
		ExecutionStep firstStep = null;
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		List<IterationTestPlanItem> suiteTestPlan = testSuite.getTestPlan();
		// get test plan
		if (!suiteTestPlan.isEmpty()) {
			// delete all executions
			deleteAllExecutionsOfTestPlan(suiteTestPlan);
			// create new execution for first test_plan_item
			firstStep = createExecutionsUntillHasSteps(suiteTestPlan);
		}
		return firstStep;
	}

	private ExecutionStep createExecutionsUntillHasSteps(List<IterationTestPlanItem> suiteTestPlan) {
		ExecutionStep step = null;
		for (IterationTestPlanItem iterationTestPlanItem : suiteTestPlan) {
			if (!iterationTestPlanItem.isTestCaseDeleted()) {
				Iteration iteration = iterationTestPlanItem.getIteration();
				Long iterationId = iteration.getId();
				Long testPlanId = iterationTestPlanItem.getId();
				Execution execution = iterationModificationService.addExecution(iterationId, testPlanId);
				if (!execution.getSteps().isEmpty()) {
					step = execution.getSteps().get(0);
					break;
				}
			}
		}
		return step;
	}

	private void deleteAllExecutionsOfTestPlan(List<IterationTestPlanItem> suiteTestPlan) {
		for (IterationTestPlanItem iterationTestPlanItem : suiteTestPlan) {
			List<Execution> executions = iterationTestPlanItem.getExecutions();
			if (!executions.isEmpty()) {
				campaignDeletionHandler.deleteExecutions(executions);
			}
		}
	}
}
