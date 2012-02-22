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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan")
public class TestSuiteExecutionController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteExecutionController.class);
	
	private static final String OPTIMIZED_RUNNER_VIEW_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/runner?optimized";
	private static final String CLASSIC_RUNNER_VIEW_PATTERN = "/test-suites/{0}/test-plan/{1}/executions/{2}/runner";

	private TestSuiteTestPlanManagerService testPlanManager;
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
			@PathVariable long executionId, Model model, WebRequest request) {
		helper.populateExecutionRunnerModel(executionId, model);
		
		boolean hasNextTestCase = testPlanManager.hasMoreExecutableItems(testSuiteId, testPlanItemId);
		model.addAttribute("hasNextTestCase", hasNextTestCase);

		String testPlanItemUrl = request.getContextPath() + "/test-suites/" + testSuiteId + "/test-plan"
				+ testPlanItemId;
		model.addAttribute("testPlanItemUrl", testPlanItemUrl);
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Will show OER for test suite using model :" + model.asMap());;
		}
		
		return "page/executions/ieo-execute-execution";
	}

	@RequestMapping(value = "/{testPlanItemId}/next-execution/runner", method = RequestMethod.POST, params = "optimized")
	@ResponseBody
	public String startNextExecutionInOptimizedRunner(@PathVariable long testSuiteId, @PathVariable long testPlanItemId) {
		return "next exec of suite " + testSuiteId + " item " + testPlanItemId;
	}

	@RequestMapping(value = "/{testPlanItemId}/executions/{executionId}/classic-runner", method = RequestMethod.GET)
	public String showClassicExecutionRunner(@PathVariable long executionId, Model model) {
		helper.populateExecutionRunnerModel(executionId, model);
		return "page/executions/execute-execution";
	}
}
