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
import javax.servlet.ServletContext;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.service.ExecutionProcessingService;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/executions/{executionId}/runner")
public class TestCaseExecutionController {
	
	private static final String OPTIMIZED_RUNNER_MAIN = "page/executions/ieo-main-page";

	@Inject
	private ExecutionRunnerControllerHelper helper;

	private ExecutionProcessingService executionProcessingService;
	
	public TestCaseExecutionController() {
		super();
	}

	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcService) {
		this.executionProcessingService = executionProcService;
	}
	

	
	//redirects to something served by ExecutionProcessingController
	private String getRedirectExecURL(long executionId, boolean optimized, boolean suitemode){
		return "/execute/"+executionId+"?optimized="+optimized+"&suitemode="+suitemode;
	}
	
	
	@RequestMapping(params = {"optimized=true", "suitemode=false"})
	public String startResumeExecutionInOptimizedRunner(@PathVariable long executionId, Model model, ServletContext context, Locale locale) {
		
		RunnerState state = helper.initOptimizedSingleContext(executionId, context.getContextPath(), locale);
		model.addAttribute("config", state);
		
		return OPTIMIZED_RUNNER_MAIN;
		
	}

	

	@RequestMapping(params = {"optimized=false", "suitemode=false"})
	public String startResumeExecutionInClassicRunner(@PathVariable long executionId, Model model) {
		
		//simple case here : the context is simply the popup. We redirect to the execution processing view controller.
		return "redirect:" + getRedirectExecURL(executionId, false, false);		
		
	}
	
	
	@RequestMapping(params = "dry-run")
	@ResponseBody
	public void dryRunStartResumeExecution(@PathVariable long executionId) {
		executionProcessingService.findRunnableExecutionStep(executionId);
	}
	
	
}
