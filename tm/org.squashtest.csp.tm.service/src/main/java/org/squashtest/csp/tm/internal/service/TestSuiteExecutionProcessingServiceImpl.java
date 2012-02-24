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
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

@Service("squashtest.tm.service.TestSuiteExecutionProcessingService")
@Transactional
public class TestSuiteExecutionProcessingServiceImpl implements TestSuiteExecutionProcessingService {
	@Inject
	private TestSuiteDao suiteDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;
	@Inject
	private IterationTestPlanManager testPlanManager;

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Execution startResume(long testSuiteId) {
		List<IterationTestPlanItem> testSuiteResumableTestPlan = suiteDao.findLaunchableTestPlan(testSuiteId);
		if (!testSuiteResumableTestPlan.isEmpty()) {
			return findExecutionWhereToResume(testSuiteResumableTestPlan);
		}
		return null;
	}

	@Override
	@PreAuthorize("hasPermission(#executionId, 'org.squashtest.csp.tm.domain.execution.Execution', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public int findIndexOfFirstUnexecuted(long executionId) {
		Execution execution = executionDao.findById(executionId);
		return execution.findIndexOfFirstUnexecutedStep();
	}

	private Execution findExecutionWhereToResume(List<IterationTestPlanItem> testSuiteResumableTestPlan) {
		Execution execution = null;
		for (IterationTestPlanItem testPlanItem : testSuiteResumableTestPlan) {
			List<Execution> executions = testPlanItem.getExecutions();
			execution = findFirstUnexecutedOrCreateExecution(executions, testPlanItem);
			if (execution != null) {
				break;
			}
		}
		return execution;
	}

	/**
	 * will go through testPlan and find one with no execution or unterminated one, else will return null
	 * 
	 * @param executions
	 * @param testPlanItem
	 * @return
	 */
	private Execution findFirstUnexecutedOrCreateExecution(List<Execution> executions,
			IterationTestPlanItem testPlanItem) {
		Execution executionToReturn = null;
		if (!executions.isEmpty()) {
			if (lastExecutionIsNotTerminated(executions)) {
				executionToReturn = executions.get(executions.size() - 1);
			}
		} else {
			if (!testPlanItem.isTestCaseDeleted()) {
				Execution execution = testPlanManager.addExecution(testPlanItem);
				if (!execution.getSteps().isEmpty()) {
					executionToReturn = execution;
				}
			}
		}
		return executionToReturn;
	}

	private boolean lastExecutionIsNotTerminated(List<Execution> executions) {
		Execution lastExecution = executions.get(executions.size() - 1);
		return lastExecution.findFirstUnexecutedStep() != null;
	}

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")
	public Execution restart(long testSuiteId) {
		// getTest plan
		Execution execution = null;
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		List<IterationTestPlanItem> suiteTestPlan = testSuite.getTestPlan();
		// get test plan
		if (!suiteTestPlan.isEmpty()) {
			// delete all executions
			deleteAllExecutionsOfTestPlan(suiteTestPlan);
			// create new execution for first test_plan_item
			execution = createExecutionsUntillHasSteps(suiteTestPlan);
		}
		return execution;
	}

	private Execution createExecutionsUntillHasSteps(List<IterationTestPlanItem> suiteTestPlan) {
		Execution executionToReturn = null;
		for (IterationTestPlanItem iterationTestPlanItem : suiteTestPlan) {
			if (!iterationTestPlanItem.isTestCaseDeleted()) {
				Execution execution = testPlanManager.addExecution(iterationTestPlanItem);
				if (!execution.getSteps().isEmpty()) {
					executionToReturn = execution;
					break;
				}
			}
		}
		return executionToReturn;
	}

	private void deleteAllExecutionsOfTestPlan(List<IterationTestPlanItem> suiteTestPlan) {
		for (IterationTestPlanItem iterationTestPlanItem : suiteTestPlan) {
			List<Execution> executions = iterationTestPlanItem.getExecutions();
			if (!executions.isEmpty()) {
				campaignDeletionHandler.deleteExecutions(executions);
			}
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService#hasMoreExecutableItems(long, long)
	 */
	@Override
	public boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		return !testSuite.isLastExecutableTestPlanItem(testPlanItemId);
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService#startNextExecution(long, long)
	 */
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Execution startNextExecution(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		IterationTestPlanItem next = testSuite.findNextExecutableTestPlanItem(testPlanItemId);
		return testPlanManager.addExecution(next);
	}

}
