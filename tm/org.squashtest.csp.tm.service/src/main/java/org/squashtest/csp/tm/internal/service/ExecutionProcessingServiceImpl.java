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

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.exception.ExecutionHasNoRunnableStepException;
import org.squashtest.csp.tm.domain.exception.ExecutionHasNoStepsException;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao;
import org.squashtest.csp.tm.service.ExecutionModificationService;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;


@Service("squashtest.tm.service.ExecutionProcessingService")
public class ExecutionProcessingServiceImpl implements ExecutionProcessingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingServiceImpl.class);

	@Inject
	private UserContextService userContextService;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private ExecutionModificationService execModService;

	@Inject
	private IterationTestPlanManagerService testPlanService;
	
	@Override
	public ExecutionStep findExecutionStep(Long executionStepId) {
		return executionStepDao.findById(executionStepId);
	}

	@Override
	public ExecutionStep findRunnableExecutionStep(long executionId) throws ExecutionHasNoStepsException {
		Execution execution = executionDao.findById(executionId);
		
		ExecutionStep step;
		try {
			step = execution.findFirstRunnableStep();
		} catch (ExecutionHasNoRunnableStepException e) {
			step = execution.getLastStep();
		}
		
		return step;
	}

	@Override
	public ExecutionStep findStepAt(long executionId, int executionStepIndex) {
		Execution execution = executionDao.findAndInit(executionId);

		return execution.getSteps().get(executionStepIndex);
	}

	@Override
	public void setExecutionStepStatus(Long executionStepId, ExecutionStatus status) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		ExecutionStatus formerStatus = step.getExecutionStatus();

		step.setExecutionStatus(status);

		// update execution data for step and update execution and item test plan status and execution data
		forwardAndUpdateStatus(step, formerStatus);
	}

	@Override
	public void setExecutionStepComment(Long executionStepId, String comment) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		step.setComment(comment);
	}

	@Override
	public Execution findExecution(Long executionId) {
		return execModService.findExecution(executionId);
	}

	@Override
	public List<ExecutionStep> getExecutionSteps(Long executionId) {
		return execModService.getExecutionSteps(executionId);
	}

	@Override
	public int findExecutionStepRank(Long executionStepId) {
		ExecutionStep step = executionStepDao.findById(executionStepId);
		return step.getExecutionStepOrder();
	}

	@Override
	public int findTotalNumberSteps(Long executionId) {
		Execution execution = executionDao.findAndInit(executionId);
		return execution.getSteps().size();
	}

	@Override
	public void setExecutionStatus(Long executionId, ExecutionStatus status) {
		Execution execution = executionDao.findById(executionId);
		execution.setExecutionStatus(status);

	}

	@Override
	public ExecutionStatusReport getExecutionStatusReport(Long executionId) {
		return executionDao.getStatusReport(executionId);
	}

	/***
	 * Method which update :<br>
	 * * execution and item test plan status * execution data for the step, execution and item test plan
	 *
	 * @param executionStep
	 * @param formerStepStatus
	 */
	private void forwardAndUpdateStatus(ExecutionStep executionStep, ExecutionStatus formerStepStatus) {
		// update step execution data
		updateStepExecutionData(executionStep);

		Execution execution = executionStepDao.findParentExecution(executionStep.getId());

		ExecutionStatus formerExecutionStatus = execution.getExecutionStatus();
		ExecutionStatus newStepStatus = executionStep.getExecutionStatus();

		// let's see if we can autocompute that thing
		ExecutionStatus newExecutionStatus = newStepStatus.deduceNewStatus(formerExecutionStatus, formerStepStatus);

		if (newExecutionStatus == null) {
			ExecutionStatusReport report = executionDao.getStatusReport(execution.getId());
			newExecutionStatus = ExecutionStatus.computeNewStatus(report);
		}

		execution.setExecutionStatus(newExecutionStatus);
		// update execution and item test plan data
		updateExecutionAndItemTestPlanExecutionData(execution);
	}

	/***
	 * Update the execution step lastExecutionBy and On values depending on the status
	 *
	 * @param executionStep
	 *            the step to update
	 */
	private void updateStepExecutionData(ExecutionStep executionStep) {
		// check the execution step status
		if (executionStep.getExecutionStatus().compareTo(ExecutionStatus.READY) == 0) {
			// if the item test plan status is READY, we reset the data
			executionStep.setLastExecutedBy(null);
			executionStep.setLastExecutedOn(null);
		} else {
			// we update the step execution data
			executionStep.setLastExecutedBy(userContextService.getUsername());
			executionStep.setLastExecutedOn(new Date());
		}
	}

	/***
	 * Update the execution lastExecutionBy and On values depending on the status<br>
	 * Update the Item Test Plan if it's the last execution
	 *
	 * @param givenExecution
	 *            the execution to update
	 */
	private void updateExecutionAndItemTestPlanExecutionData(Execution givenExecution) {
		// Default last executed by and on values
		String lastExecutedBy = null;
		Date lastExecutedOn = null;
		if (givenExecution.getExecutionStatus().compareTo(ExecutionStatus.READY) != 0) {
			// The status is not READY
			// executed by and on values are not null
			// Get the date and user of the most recent step which status is not at READY
			ExecutionStep mostRecentStep = getMostRecentExecutionStep(givenExecution);
			lastExecutedBy = mostRecentStep.getLastExecutedBy();
			lastExecutedOn = mostRecentStep.getLastExecutedOn();
		}
		// We update the execution data
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Set {} values to {} and {} " ,new Object[]{givenExecution.getName(), lastExecutedBy, lastExecutedOn });
		}
		givenExecution.setLastExecutedBy(lastExecutedBy);
		givenExecution.setLastExecutedOn(lastExecutedOn);

		// Check if we have to update item test plan too
		// get the test case (ItemTestPlan)
		IterationTestPlanItem testPlan = givenExecution.getTestPlan();
		// Then get the execution list
		List<Execution> executionList = testPlan.getExecutions();
		// check the execution status
		if (executionList.get(executionList.size() - 1).getId().equals(givenExecution.getId())) {
			// last execution, we update the item test plan
			LOGGER.debug("**************** last execution - initiate update ");
			testPlanService.updateTestCaseLastExecutedByAndOn(testPlan, lastExecutedOn, lastExecutedBy);
		}
	}

	/***
	 * Method which gets the most recent execution step which status is not at READY
	 *
	 * @param givenExecution
	 *            the execution from which we get the steps
	 * @return the most recent Execution Step which is not "READY"
	 */
	private ExecutionStep getMostRecentExecutionStep(Execution givenExecution) {
		// Start at the fist one
		ExecutionStep mostRecentExecutionStep = givenExecution.getSteps().get(0);
		List<ExecutionStep> stepList = givenExecution.getSteps();
		for (ExecutionStep executionStep : stepList) {
			// first the status
			if (executionStep.getExecutionStatus().compareTo(ExecutionStatus.READY) != 0) {
				// first the most recent execution step has no execution date
				if (mostRecentExecutionStep.getLastExecutedOn() == null) {
					mostRecentExecutionStep = executionStep;
				}
				// we compare the date and update the value if the step date is greater
				else if (executionStep.getLastExecutedOn() != null
						&& mostRecentExecutionStep.getLastExecutedOn().compareTo(executionStep.getLastExecutedOn()) < 0) {
					mostRecentExecutionStep = executionStep;
				}
			}
		}
		return mostRecentExecutionStep;
	}

	@Override
	public void setExecutionStatus(Long executionId, ExecutionStatusReport report) {
		Execution execution = executionDao.findAndInit(executionId);

		ExecutionStatus newStatus = ExecutionStatus.computeNewStatus(report);

		execution.setExecutionStatus(newStatus);

	}

}
