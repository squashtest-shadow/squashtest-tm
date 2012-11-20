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
package org.squashtest.csp.tm.web.internal.controller.execution;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

/**
 * Helper class for Controllers which need to show classic and optimized execution runners.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
public class ExecutionRunnerControllerHelper {
	
	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}";
	public static final String CURRENT_STEP_URL_PATTERN = "/execute/{0,number,####}/step/";
	
	
	private interface FetchStepCommand {
		ExecutionStep firstFirstRunnable(long executionId);
		ExecutionStep findStepAtIndex(long executionId, int stepIndex);
	}
	
	private FetchStepCommand FETCHER = new FetchStepCommand() {
		
		@Override
		public ExecutionStep firstFirstRunnable(long executionId) {
			return executionProcessingService.findRunnableExecutionStep(executionId);
		}
		
		@Override
		public ExecutionStep findStepAtIndex(long executionId, int stepIndex) {
			
			int stepCount = executionProcessingService.findTotalNumberSteps(executionId);
			
			if (stepIndex >= stepCount) {
				return executionProcessingService.findStepAt(executionId, stepCount - 1);
			}

			ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

			if (executionStep == null) {
				executionStep = executionProcessingService.findStepAt(executionId, stepCount - 1);
			}

			return executionStep;
		}
	};
	

	private ExecutionProcessingService executionProcessingService;
	
	
	private TestSuiteExecutionProcessingService testSuiteExecutionProcessingService;

	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcService) {
		this.executionProcessingService = executionProcService;
	}
	
	@ServiceReference
	public void setTestSuiteExecutionProcessingService(TestSuiteExecutionProcessingService testSuiteExecutionProcessingService) {
		this.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService;
	}
	
	@Inject
	private MessageSource messageSource;
	
	
	
	
	public RunnerState createNewRunnerState(boolean isOptimized, boolean isTestSuiteMode){
		RunnerState state = new RunnerState();
		
		state.setOptimized(isOptimized);
		state.setTestSuiteMode(isTestSuiteMode);
		
		return state;
	}


	public void populateExecutionPreview(final long executionId, Model model){
		popuplateExecutionPreview(executionId, false, false, model);
	}
	
	
	public void popuplateExecutionPreview(final long executionId, boolean isOptimized, boolean isTestSuiteMode, Model model){
		
		Execution execution = executionProcessingService.findExecution(executionId);
		
		RunnerState state = createNewRunnerState(isOptimized, isTestSuiteMode);
		state.setPrologue(true);
		
		model.addAttribute("execution", execution);
		model.addAttribute("config", state);
		
	}
	
	
	
	public void populateClassicTestSuiteSpecifics(final long executionId, Model model) {
	
		Execution execution = executionProcessingService.findExecution(executionId);
		IterationTestPlanItem itpi = execution.getTestPlan();
		TestSuite ts = itpi.getTestSuite();
		
		
		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", true);
		
		addTestPlanItemUrl(ts.getId(), itpi.getId(), model);
		addHasNextTestCase(ts.getId(), itpi.getId(), model);
		addCurrentStepUrl(executionId, model);
		
	}
	
	public void populateClassicSingleSpecifics(Model model) {

		model.addAttribute("optimized", false);
		model.addAttribute("suitemode", false);
	}
	
	public void populateOptimizedSingleSpecifics(Model model) {
		//TODO
	}
	
	
	public void populateOptimizedTestSuiteSpecifics(long executionId, Model model){
		//TODO
	}
	
	
	
	public void populateExecutionStepModel(long executionId, int stepIndex, Model model) {
		
		Execution execution = executionProcessingService.findExecution(executionId);
		ExecutionStep executionStep = FETCHER.findStepAtIndex(executionId, stepIndex);

		_populateExecutionStepModel(execution, executionStep, model);
	}
	
	public void populateExecutionStepModel(long executionId, Model model){
		
		Execution execution = executionProcessingService.findExecution(executionId);
		ExecutionStep executionStep = FETCHER.firstFirstRunnable(executionId);
		
		_populateExecutionStepModel(execution, executionStep, model);
	
	}
	
	private void _populateExecutionStepModel(Execution execution, ExecutionStep executionStep, Model model){
		
		int stepOrder = 0;
		int total = execution.getSteps().size();
		
		Set<ExecutionStatus> statusSet = Collections.emptySet();
		if(executionStep != null){
			stepOrder = executionStep.getExecutionStepOrder();
			statusSet = executionStep.getLegalStatusSet();
		}

		model.addAttribute("execution", execution);
		model.addAttribute("executionStep", executionStep);
		model.addAttribute("totalSteps", total );
		model.addAttribute("executionStatus", statusSet );
		model.addAttribute("hasNextStep", stepOrder != (total - 1));	
		
		addCurrentStepUrl(execution.getId(), model);
	}
	
	
	// ************************ private stuff **************************
	
	private void addTestPlanItemUrl(long testSuiteId, long testPlanItemId, Model model) {
		String testPlanItemUrl = MessageFormat.format(TEST_PLAN_ITEM_URL_PATTERN, testSuiteId, testPlanItemId);
		model.addAttribute("testPlanItemUrl", testPlanItemUrl);
	}

	private void addHasNextTestCase(long testSuiteId, long testPlanItemId, Model model) {
		boolean hasNextTestCase = testSuiteExecutionProcessingService.hasMoreExecutableItems(testSuiteId,
				testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);
	}


	private void addCurrentStepUrl(long executionId, Model model) {
		String currentStepUrl = MessageFormat.format( CURRENT_STEP_URL_PATTERN, executionId);
		model.addAttribute("currentStepUrl", currentStepUrl);
	}
	

}
