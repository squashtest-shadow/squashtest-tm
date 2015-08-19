/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.execution;

import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/executions/{executionId}/runner")
public class TestCaseExecutionRunnerController {

	private static final String OPTIMIZED_RUNNER_MAIN = "page/ieo/ieo-main-page";

	@Inject
	private ExecutionRunnerControllerHelper helper;

	@Inject
	private ExecutionProcessingService executionProcessingService;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;

	@Inject
	private BugTrackerFinderService bugTrackerFinderService;


	public TestCaseExecutionRunnerController() {
		super();
	}


	// redirects to something served by ExecutionProcessingController
	private String getRedirectExecURL(long executionId, boolean optimized) {
		return "/execute/" + executionId + "?optimized=" + optimized;
	}

	@RequestMapping(params = { "optimized=true" })
	public String startResumeExecutionInOptimizedRunner(@PathVariable long executionId, Model model,
			HttpServletRequest context, Locale locale) {

		RunnerState state = helper.initOptimizedSingleContext(executionId, context.getContextPath(), locale);
		model.addAttribute("config", state);

		try{
			Project project = executionProcessingService.findExecution(executionId).getProject();
			BugTracker bugtracker = project.findBugTracker();
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(bugtracker);
			model.addAttribute("interfaceDescriptor", descriptor);
			model.addAttribute("bugTracker", bugtracker);
		}
		catch(NoBugTrackerBindingException ex){
			//well, no bugtracker then. It's fine.
		}


		return OPTIMIZED_RUNNER_MAIN;

	}

	@RequestMapping(params = { "optimized=false" })
	public String startResumeExecutionInClassicRunner(@PathVariable long executionId, Model model) {

		// simple case here : the context is simply the popup. We redirect to the execution processing view controller.
		return "redirect:" + getRedirectExecURL(executionId, false);

	}

	@RequestMapping(params = "dry-run")
	@ResponseBody
	public void dryRunStartResumeExecution(@PathVariable long executionId) {
		executionProcessingService.findRunnableExecutionStep(executionId);
	}

}
