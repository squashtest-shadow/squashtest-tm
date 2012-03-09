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
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

@Service("squashtest.tm.service.TestSuiteExecutionProcessingService")
@Transactional
public class TestSuiteExecutionProcessingServiceImpl implements TestSuiteExecutionProcessingService {
	/**
	 * 
	 */
	private static final String CAN_WRITE_BY_CAMPAIGN_ID = "hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') or hasRole('ROLE_ADMIN')";
	@Inject
	private TestSuiteDao suiteDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;
	@Inject
	private IterationTestPlanManager testPlanManager;

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#startResume(long, long)
	 */
	@Override
	@PreAuthorize(CAN_WRITE_BY_CAMPAIGN_ID)
	public Execution startResume(long testSuiteId) {
		Execution execution = null;
		TestSuite suite = suiteDao.findById(testSuiteId);
		IterationTestPlanItem item = suite.findFirstExecutableTestPlanItem();
		execution = findUnexecutedOrCreateExecution(item);
		if (execution == null || execution.getSteps().isEmpty()) {
			startResumeNextExecution(testSuiteId, item.getId());
		}
		return execution;
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#findIndexOfFirstUnexecuted(long, long)
	 */
	@Override
	@PreAuthorize("hasPermission(#executionId, 'org.squashtest.csp.tm.domain.execution.Execution', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public int findIndexOfFirstUnexecuted(long executionId) {
		Execution execution = executionDao.findById(executionId);
		return execution.findIndexOfFirstUnexecutedStep();
	}

	/**
	 * if has executions: will return last execution if not terminated,<br>
	 * if has no execution and is not test-case deleted : will return new execution<br>
	 * else will return null
	 * 
	 * @param executions
	 * @param testPlanItem
	 * @return
	 * 
	 */
	private Execution findUnexecutedOrCreateExecution(IterationTestPlanItem testPlanItem) {
		Execution executionToReturn = null;
		if (testPlanItem.isExecutableThroughTestSuite()) {
			executionToReturn = testPlanItem.getLastExecution();
			if (executionToReturn == null) {
				executionToReturn = testPlanManager.addExecution(testPlanItem);
			}
		}
		return executionToReturn;
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#restart(long, long)
	 */
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")
	public void deleteAllExecutions(long testSuiteId) {
		// getTest plan
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		List<IterationTestPlanItem> suiteTestPlan = testSuite.getTestPlan();
		if (!suiteTestPlan.isEmpty()) {
			// delete all executions
			deleteAllExecutionsOfTestPlan(suiteTestPlan);
		}

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
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#hasMoreExecutableItems(long, long)
	 */
	@Override
	public boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		return !testSuite.isLastExecutableTestPlanItem(testPlanItemId);
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#hasPreviousExecutableItems(long, long)
	 */
	@Override
	public boolean hasPreviousExecutableItems(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		return !testSuite.isFirstExecutableTestPlanItem(testPlanItemId);
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService#startNextExecution(long, long)
	 */
	@Override
	@PreAuthorize(CAN_WRITE_BY_CAMPAIGN_ID)
	public Execution startResumeNextExecution(long testSuiteId, long testPlanItemId) {
		Execution execution = null;
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		IterationTestPlanItem item = testSuite.findNextExecutableTestPlanItem(testPlanItemId);
		execution = findUnexecutedOrCreateExecution(item);
		while (execution == null || execution.getSteps().isEmpty()) {
			item = testSuite.findNextExecutableTestPlanItem(testPlanItemId);
			execution = findUnexecutedOrCreateExecution(item);
		}
		return execution;
	}

}
