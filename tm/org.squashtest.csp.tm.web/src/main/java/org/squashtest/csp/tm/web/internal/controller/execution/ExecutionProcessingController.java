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
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;


@Controller
@RequestMapping("/execute/{executionId}")
public class ExecutionProcessingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingController.class);


	private ExecutionProcessingService executionProcService;


	@ServiceReference
	public void setExecutionProcService(
			ExecutionProcessingService executionProcService) {
		this.executionProcService = executionProcService;
	}


	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getLast(@PathVariable Long executionId){
		Execution execution = executionProcService.findExecution(executionId) ;
		Integer total = execution.getSteps().size();

		ModelAndView mav = new ModelAndView("fragment/executions/execute-execution");

		if (total == 0) {
			mav.addObject("totalSteps",total);
		}
		else{

			ExecutionStep executionStep = executionProcService.findRunningExecutionStep(executionId);
			if (executionStep==null) {
				executionStep = executionProcService.getStepAt(executionId, total-1);
			}



			mav.addObject("execution",execution);
			mav.addObject("executionStep",executionStep);
			mav.addObject("totalSteps",total);
			mav.addObject("executionStatus",ExecutionStatus.values());
		}

		return mav;
	}

	@RequestMapping(value="/ieo" ,method=RequestMethod.GET)
	public ModelAndView getIeoLast(@PathVariable Long executionId){
		Execution execution = executionProcService.findExecution(executionId) ;
		Integer total = execution.getSteps().size();

		ModelAndView mav = new ModelAndView("fragment/executions/ieo-execute-execution");

		if (total == 0) {
			mav.addObject("totalSteps",total);
		}
		else{

			ExecutionStep executionStep = executionProcService.findRunningExecutionStep(executionId);
			if (executionStep==null) {
				executionStep = executionProcService.getStepAt(executionId, total-1);
			}



			mav.addObject("execution",execution);
			mav.addObject("executionStep",executionStep);
			mav.addObject("totalSteps",total);
			mav.addObject("executionStatus",ExecutionStatus.values());
		}

		return mav;
	}

	@RequestMapping(value="/step/{stepIndex}", method=RequestMethod.GET)
	public String getStep(@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		showExecutionStep(executionId, stepIndex, model);

		return "fragment/executions/execute-execution";

	}


	private void showExecutionStep(long executionId, int stepIndex, Model model) {
		Execution execution = executionProcService.findExecution(executionId) ;

		Integer total = execution.getSteps().size();
		ExecutionStep executionStep;
		if (stepIndex >= total) {
			executionStep = executionProcService.getStepAt(executionId, total-1);
		} else {
			executionStep = executionProcService.getStepAt(executionId, stepIndex);
		}

		if (executionStep==null) {
			executionStep = executionProcService.getStepAt(executionId, total-1);
		}

		model.addAttribute("execution", execution);
		model.addAttribute("executionStep", executionStep);
		model.addAttribute("totalSteps", total);
		model.addAttribute("executionStatus", ExecutionStatus.values());
	}

	@RequestMapping(value="/step/{stepIndex}", method=RequestMethod.GET, params={"ieo=true"})
	public String getIeoStep(@PathVariable long executionId, @PathVariable int stepIndex, Model model) {
		showExecutionStep(executionId, stepIndex, model);

		return "fragment/executions/ieo-fragment-step-information";

	}

	@RequestMapping(value="/step/{stepIndex}/menu", method=RequestMethod.GET)
	public ModelAndView getGeneralInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex){
		Execution execution = executionProcService.findExecution(executionId) ;
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep;
		if (stepIndex >= total) {
			executionStep = executionProcService.getStepAt(executionId, total-1);
		} else {
			executionStep = executionProcService.getStepAt(executionId, stepIndex);
		}

		if (executionStep==null) {
			executionStep = executionProcService.getStepAt(executionId, total-1);
		}
		ModelAndView mav = new ModelAndView("fragment/executions/step-information-menu");
		mav.addObject("execution",execution);
		mav.addObject("executionStep",executionStep);
		mav.addObject("totalSteps",total);
		mav.addObject("executionStatus",ExecutionStatus.values());
		return mav;

	}

	@RequestMapping(value="/step/{stepIndex}/general", method=RequestMethod.GET)
	public ModelAndView getMenuInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex){

		ExecutionStep executionStep = executionProcService.getStepAt(executionId, stepIndex);

		ModelAndView mav = new ModelAndView("fragment/executions/step-information-fragment");

		mav.addObject("auditableEntity",executionStep);
		mav.addObject("withoutCreationInfo", true);

		return mav;

	}

	@RequestMapping(value="/step/{stepId}", method = RequestMethod.POST, params = {	"id=execution-comment", "value" })
	@ResponseBody
	public String updateComment(@RequestParam("value") String newComment,
			@PathVariable("stepId") Long stepId) {

		executionProcService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionStep " + stepId.toString()	+ ": updated comment to " + newComment);
		return newComment;
	}

	@RequestMapping(value="/step/{stepIndex}/new-step-infos", method = RequestMethod.GET)
	@ResponseBody
	public String getNewStepInfos(@PathVariable Long executionId,
			@PathVariable Integer stepIndex) {

		JsonSimpleData obj = new JsonSimpleData();

		Execution execution = executionProcService.findExecution(executionId) ;
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep = executionProcService.getStepAt(executionId, stepIndex);

		if (executionStep==null) {
			executionStep = executionProcService.getStepAt(executionId, total-1);
		}

		obj.addAttr("executionStepOrder", executionStep.getExecutionStepOrder().toString());
		obj.addAttr("executionStepId", executionStep.getId().toString());

		return obj.toString();
	}


	@RequestMapping(value="/step/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionMode(@RequestParam String executionStatus, @PathVariable("stepId") long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcService.setExecutionStepStatus(stepId, status);
	}








}
