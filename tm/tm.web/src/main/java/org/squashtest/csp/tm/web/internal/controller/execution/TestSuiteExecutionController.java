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

import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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
import org.squashtest.csp.tm.domain.TestPlanItemNotExecutableException;
import org.squashtest.csp.tm.domain.TestPlanTerminatedOrNoStepsException;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;

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
		public static final String INIT_EXECUTION_RUNNER = "/execution/runner";
		public static final String TEST_EXECUTION_BEFORE_INIT = "/execution/test-runner";
		public static final String INIT_NEXT_EXECUTION_RUNNER = "/{testPlanItemId}/next-execution/runner";
	}



	public static final String TEST_PLAN_ITEM_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}";
	public static final String CURRENT_STEP_URL_PATTERN = "/test-suites/{0,number,####}/test-plan/{1,number,####}/executions/{2,number,####}/steps/index/";
	
	
	private static final String OPTIMIZED_RUNNER_MAIN = "page/ieo/ieo-main-page";

	
	private TestSuiteExecutionProcessingService testSuiteExecutionProcessingService;
	

	@Inject
	private ExecutionRunnerControllerHelper helper;
	

	@ServiceReference
	public void setTestSuiteExecutionProcessingService(TestSuiteExecutionProcessingService testSuiteExecutionProcessingService) {
		this.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService;
	}


	public TestSuiteExecutionController() {
		super();
	}
	

	
	
	//redirects to something served by ExecutionProcessingController
	private String getRedirectExecURL(long executionId, boolean optimized, boolean suitemode){
		return "/execute/"+executionId+"?optimized="+optimized+"&suitemode="+suitemode;
	}
	
	
	@RequestMapping(value = RequestMappings.TEST_EXECUTION_BEFORE_INIT, method = RequestMethod.POST, params = {"mode=start-resume"})
	public @ResponseBody void testStartResumeExecutionInClassicRunner(@PathVariable long testSuiteId) {
		try{
			testSuiteExecutionProcessingService.startResume(testSuiteId);
		}
		catch(TestPlanItemNotExecutableException e){
			throw new TestPlanTerminatedOrNoStepsException();
		}
	}
	

	
	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, params = {"optimized=false", "suitemode=true"})
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId, Model model) {
		
		Execution execution = testSuiteExecutionProcessingService.startResume(testSuiteId);

		return "redirect:"+ getRedirectExecURL(execution.getId(), false, true);
		
	}
	
	
	@RequestMapping(value = RequestMappings.INIT_EXECUTION_RUNNER, params = {"optimized=true", "suitemode=true"})
	public String startResumeExecutionInClassicRunner(@PathVariable long testSuiteId, Model model, HttpServletRequest context, Locale locale) {
		
		RunnerState state = helper.initOptimizedTestSuiteContext(testSuiteId, context.getContextPath(), locale);
		model.addAttribute("config", state);
		
		return OPTIMIZED_RUNNER_MAIN;
		
		
	}
	
	

	//that method will create if necessary the next execution then redirect a view to its runner
	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, params={"optimized", "suitemode"}, headers = "Accept=text/html")
	public String moveToNextTestCase(@PathVariable("testPlanItemId") long testPlanItemId,
									 @PathVariable("testSuiteId") long testSuiteId, 
									 @RequestParam("optimized") boolean optimized,
									 @RequestParam("suitemode") boolean suitemode){
		
		Execution exec = testSuiteExecutionProcessingService.startResumeNextExecution(testSuiteId, testPlanItemId);
		
		return "redirect:"+ getRedirectExecURL(exec.getId(), optimized, suitemode);
		
	}
		
	
	//that method will create if necessary the next execution then return the RunnerState that corresponds to it
	//note that most of the time it corresponds to an ieo working in test suite mode so we skip 'optimized' and 'suitemode' parameters here
	@RequestMapping(value = RequestMappings.INIT_NEXT_EXECUTION_RUNNER, params={"optimized", "suitemode"}, headers = "Accept=application/json")
	@ResponseBody
	public RunnerState getNextTestCaseRunnerState(@PathVariable("testPlanItemId") long testPlanItemId,
									 @PathVariable("testSuiteId") long testSuiteId, 
									 HttpServletRequest context,
									 Locale locale){
		
		RunnerState state = helper.createNextOptimizedTestSuiteContext(testSuiteId, testPlanItemId, context.getContextPath(), locale);
		
		return state;
		
	}	

	
}
