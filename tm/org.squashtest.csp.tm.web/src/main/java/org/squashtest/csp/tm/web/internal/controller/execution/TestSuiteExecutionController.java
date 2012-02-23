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
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;
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

	private static final String CLASSIC_RUNNER_VIEW_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/runner";
	private static final String OPTIMIZED_RUNNER_VIEW_PATTERN = CLASSIC_RUNNER_VIEW_PATTERN + "?optimized";
	private static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0}/test-plan/{1}";
	private static final String CURRENT_STEP_URL_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/steps/index/";

	private TestSuiteTestPlanManagerService testPlanManager;
	private ExecutionProcessingService executionProcessingService;

	@Inject
	private ExecutionRunnerControllerHelper helper;

	public TestSuiteExecutionController() {
		super();
	}

	@RequestMapping(value = "/new-execution/runner", method = RequestMethod.POST, params = "optimized")
	public String startExecutionInOptimizedRunner(@PathVariable long testSuiteId) {
		return startExecution(testSuiteId, OPTIMIZED_RUNNER_VIEW_PATTERN);
	}

	private String startExecution(long testSuiteId, String runnerViewPattern) {
		Execution execution = testPlanManager.startNewExecution(testSuiteId);

		return "redirect:"
				+ MessageFormat.format(runnerViewPattern, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	@RequestMapping(value = "/new-execution/classic-runner", method = RequestMethod.POST)
	public String startExecutionInClassicRunner(@PathVariable long testSuiteId) {
		return startExecution(testSuiteId, CLASSIC_RUNNER_VIEW_PATTERN);
	}

	@ServiceReference
	public void setTestPlanManager(TestSuiteTestPlanManagerService testPlanManager) {
		this.testPlanManager = testPlanManager;
	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/runner", method = RequestMethod.GET, params = "optimized")
	public String showOptimizedExecutionRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, Model model) {
		helper.populateExecutionRunnerModel(executionId, model);

		boolean hasNextTestCase = testPlanManager.hasMoreExecutableItems(testSuiteId, testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);

		String testPlanItemUrl = MessageFormat.format(TEST_PLAN_ITEM_URL_PATTERN, testSuiteId, testPlanItemId);
		model.addAttribute("testPlanItemUrl", testPlanItemUrl);

		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show OER for test suite using model :" + model.asMap());
		}

		return "page/executions/ieo-execute-execution";
	}

	private void addCurrentStepUrl(Model model, Long... ids) {
		String currentStepUrl = MessageFormat.format(CURRENT_STEP_URL_PATTERN, (Object[]) ids);
		model.addAttribute("currentStepUrl", currentStepUrl);
	}

	@RequestMapping(value = "/{testPlanItemId}/next-execution/runner", method = RequestMethod.POST, params = "optimized")
	public String startNextExecutionInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId) {
		Execution execution = testPlanManager.startNextExecution(testSuiteId, testPlanItemId);

		return "redirect:"
				+ MessageFormat.format(OPTIMIZED_RUNNER_VIEW_PATTERN, testSuiteId, execution.getTestPlan().getId(),
						execution.getId());
	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/classic-runner", method = RequestMethod.GET)
	public String showClassicExecutionRunner(@PathVariable long executionId, Model model) {
		// FIXME not yet worky
		helper.populateExecutionRunnerModel(executionId, model);
		return "page/executions/execute-execution";
	}
	
	/* copypasta from now on. rework asap */

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}", method = RequestMethod.GET)
	public String getClassicExecutionStepFragment(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);

		return "fragment/executions/execute-execution";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}", method = RequestMethod.GET, params = { "ieo" })
	public String getOptimizedExecutionStepFragment(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
			@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);

		return "page/executions/ieo-fragment-step-information";

	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepIndex}/menu", method = RequestMethod.GET)
	public String getOptimizedExecutionToolboxFragment(@PathVariable long testSuiteId, @PathVariable long testPlanItemId,
	@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(model, testSuiteId, testPlanItemId, executionId);

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

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/steps/index/{stepId}", method = RequestMethod.POST, params = { "id=execution-comment", "value" })
	@ResponseBody
	public String updateComment(@RequestParam("value") String newComment, @PathVariable("stepId") Long stepId) {
		executionProcessingService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionStep " + stepId.toString() + ": updated comment to " + newComment);
		return newComment;
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
	/* end copypasta*/

	/**
	 * @param executionProcessingService the executionProcessingService to set
	 */
	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcessingService) {
		this.executionProcessingService = executionProcessingService;
	}

}
