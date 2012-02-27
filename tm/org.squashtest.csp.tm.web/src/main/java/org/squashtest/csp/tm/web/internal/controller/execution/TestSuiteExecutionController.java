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
package org.squashtest.csp.tm.web.internal.controller.execution;

import java.text.MessageFormat;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan")
public class TestSuiteExecutionController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteExecutionController.class);

	private static class RequestMappings {
		public static final String SHOW_STEP_INFO = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}";
		public static final String SHOW_EXECUTION_RUNNER = "/{testPlanItemId}/executions/{executionId}/runner";
		public static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		public static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
	}

	private static class ViewNames {
		public static final String RUNNER_VIEW_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/runner";
		public static final String CLASSIC_RUNNER_VIEW_PATTERN = RUNNER_VIEW_PATTERN + "?classic";
		public static final String OPTIMIZED_RUNNER_VIEW_PATTERN = RUNNER_VIEW_PATTERN + "?optimized";
	}

	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0}/test-plan/{1}";
	public static final String CURRENT_STEP_URL_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/steps/index/";

	private TestSuiteExecutionProcessingService testSuiteExecutionProcessingService;
	private ExecutionProcessingService executionProcessingService;

	@Inject
	private ExecutionRunnerControllerHelper helper;

	public TestSuiteExecutionController() {
		super();
	}

	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"optimized", "mode=start-resume"})
	public String startResumeExecutionInOptimizedRunner(@PathVariable long testSuiteId) {
		return startResumeExecution(testSuiteId, ViewNames.OPTIMIZED_RUNNER_VIEW_PATTERN);
	}

	@RequestMapping(value = RequestMappings.SHOW_EXECUTION_RUNNER, method = RequestMethod.GET, params = "optimized")
	public String showOptimizedExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model) {
		populateExecutionRunnerModel(testSuiteId, testPlanItemId, executionId, model);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show OER for test suite using model :" + model.asMap());
		}

		return "page/executions/ieo-execute-execution";
	}

	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"classic", "mode=start-resume"})
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		return startResumeExecution(testSuiteId, ViewNames.CLASSIC_RUNNER_VIEW_PATTERN);
	}

	@RequestMapping(value = RequestMappings.SHOW_EXECUTION_RUNNER, method = RequestMethod.GET, params = "classic")
	public String showClassicExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model) {
		populateExecutionRunnerModel(testSuiteId, testPlanItemId, executionId, model);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show Classic exec runner for test suite using model :" + model.asMap());
		}

		return "page/executions/execute-execution";
	}

	private String startResumeExecution(long testSuiteId, String runnerViewPattern) {
		Execution execution = testSuiteExecutionProcessingService.startResume(testSuiteId);

		return "redirect:"
				+ MessageFormat.format(runnerViewPattern, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	@ServiceReference
	public void setTestSuiteExecutionProcessingService(TestSuiteExecutionProcessingService testSuiteExecutionProcessingService) {
		this.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService;
	}
	private void addTestPlanItemUrl(long testSuiteId, long testPlanItemId, Model model) {
		String testPlanItemUrl = MessageFormat.format(TEST_PLAN_ITEM_URL_PATTERN, testSuiteId, testPlanItemId);
		model.addAttribute("testPlanItemUrl", testPlanItemUrl);
	}

	private void addHasNextTestCase(long testSuiteId, long testPlanItemId, Model model) {
		boolean hasNextTestCase = testSuiteExecutionProcessingService.hasMoreExecutableItems(testSuiteId,
				testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);
	}

	private void addCurrentStepUrl(Model model, Long... ids) {
		String currentStepUrl = MessageFormat.format( CURRENT_STEP_URL_PATTERN, (Object[]) ids);
		model.addAttribute("currentStepUrl", currentStepUrl);
	}

	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, method = RequestMethod.POST, params = "optimized")
	public String startResumeNextExecutionInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId) {
		Execution execution = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);

		return "redirect:"
				+ MessageFormat.format(ViewNames.OPTIMIZED_RUNNER_VIEW_PATTERN, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, method = RequestMethod.POST, params = "classic")
	public String startResumeNextExecutionInClassicRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId) {
		Execution execution = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);

		return "redirect:"
				+ MessageFormat.format(ViewNames.CLASSIC_RUNNER_VIEW_PATTERN, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	private void populateExecutionRunnerModel(long testSuiteId, long testPlanItemId, long executionId, Model model) {
		helper.populateExecutionRunnerModel(executionId, model);

		addTestSuiteTestPlanItemData(testSuiteId, testPlanItemId, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);
	}

	private void addTestSuiteTestPlanItemData(long testSuiteId, long testPlanItemId, Model model) {
		addHasNextTestCase(testSuiteId, testPlanItemId, model);
		addTestPlanItemUrl(testSuiteId, testPlanItemId, model);
	}

	/* copypasta from now on. rework asap */

	@RequestMapping(value = RequestMappings.SHOW_STEP_INFO, method = RequestMethod.GET)
	public String showStepInClassicRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "page/executions/execute-execution";

	}

	private void populateExecutionStepModel(long testSuiteId, long testPlanItemId, long executionId, int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);

		addTestSuiteTestPlanItemData(testSuiteId, testPlanItemId, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);
	}

	@RequestMapping(value = RequestMappings.SHOW_STEP_INFO, method = RequestMethod.GET, params = "ieo")
	public String showStepInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "page/executions/ieo-fragment-step-information";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/menu", method = RequestMethod.GET)
	public String getOptimizedRunnerToolboxFragment(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		populateExecutionStepModel(testSuiteId, testPlanItemId, executionId, stepIndex, model);

		return "fragment/executions/step-information-menu";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/general", method = RequestMethod.GET)
	public ModelAndView getMenuInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

		ModelAndView mav = new ModelAndView("fragment/executions/step-information-fragment");

		mav.addObject("auditableEntity", executionStep);
		mav.addObject("withoutCreationInfo", true);

		return mav;

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/new-step-infos", method = RequestMethod.GET)
	@ResponseBody
	public String getNewStepInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		JsonSimpleData obj = new JsonSimpleData();

		Execution execution = executionProcessingService.findExecution(executionId);
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep = executionProcessingService.findStepAt(executionId, stepIndex);

		if (executionStep == null) {
			executionStep = executionProcessingService.findStepAt(executionId, total - 1);
		}

		obj.addAttr("executionStepOrder", executionStep.getExecutionStepOrder().toString());
		obj.addAttr("executionStepId", executionStep.getId().toString());

		return obj.toString();
	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionMode(@RequestParam String executionStatus, @PathVariable long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcessingService.setExecutionStepStatus(stepId, status);
	}

	/* end copypasta */

	/**
	 * @param executionProcessingService
	 *            the executionProcessingService to set
	 */
	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcessingService) {
		this.executionProcessingService = executionProcessingService;
	}

	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"optimized", "mode=restart"})
	public String restartExecutionInOptimizedRunner(@PathVariable long testSuiteId) {
		return restartExecution(testSuiteId, ViewNames.OPTIMIZED_RUNNER_VIEW_PATTERN);
	}

	private String restartExecution(long testSuiteId, String runnerViewPattern) {
		Execution execution = testSuiteExecutionProcessingService.restart(testSuiteId);

		return "redirect:"
				+ MessageFormat.format(runnerViewPattern, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, method = RequestMethod.POST, params = {"classic", "mode=restart"})
	public String restartExecutionInClassicRunner(@PathVariable long testSuiteId) {
		return restartExecution(testSuiteId, ViewNames.CLASSIC_RUNNER_VIEW_PATTERN);
	}
}
