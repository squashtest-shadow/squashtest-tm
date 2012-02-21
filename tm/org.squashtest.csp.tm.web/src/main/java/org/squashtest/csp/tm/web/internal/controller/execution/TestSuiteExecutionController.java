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

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-suites/{testSuiteId}/test-plan/")
public class TestSuiteExecutionController {
	private TestSuiteTestPlanManagerService testPlanManager;
	
	public TestSuiteExecutionController() {
		super();
	}

	@RequestMapping(value = "/new-execution/ieo", method = RequestMethod.POST)
	public String startNewExecutionInIEO(@PathVariable long testSuiteId) {
		Execution newExec = testPlanManager.startNewExecution(testSuiteId);
		// TODO un PRG piloté par le client
		String url = "redirect:/test-suites/" + testSuiteId + "/test-plan/" + newExec.getTestPlan().getId()
				+ "/executions/" + newExec.getId() + "/ieo";

		return url;
	}

	@ServiceReference
	public void setTestPlanManager(TestSuiteTestPlanManagerService testPlanManager) {
		this.testPlanManager = testPlanManager;
	}

	@RequestMapping(value = "{testPlanItemId}/executions/{executionId}/ieo", method = RequestMethod.GET)
	@ResponseBody
	public String showOptimizedExecutionRunner() {
		return "Optimized Execution Runner";
	}
}
