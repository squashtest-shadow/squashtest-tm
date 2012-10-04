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

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

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
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;

@Controller
@RequestMapping("/execute/{executionId}")
public class ExecutionProcessingController {
	private static final String STEP_PAGE_VIEW = "page/executions/execute-execution";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionProcessingController.class);

	@Inject
	private ExecutionRunnerControllerHelper helper;

	private ExecutionProcessingService executionProcService;

	@ServiceReference
	public void setExecutionProcService(ExecutionProcessingService executionProcService) {
		this.executionProcService = executionProcService;
	}

	private void addCurrentStepUrl(long executionId, Model model) {
		model.addAttribute("currentStepUrl", "/execute/" + executionId + "/step/");
	}

	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET)
	public String getClassicExecutionStepFragment(@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(executionId, model);

		return STEP_PAGE_VIEW;

	}

	@RequestMapping(value = "/step/{stepIndex}", method = RequestMethod.GET, params = "ieo")
	public String getOptimizedExecutionStepFragment(@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(executionId, model);

		return "page/executions/ieo-fragment-step-information";

	}

	/**
	 * Only used by IEO
	 */
	@RequestMapping(value = "/step/{stepIndex}/menu", method = RequestMethod.GET)
	public String getOptimizedExecutionToolboxFragment(@PathVariable long executionId, @PathVariable int stepIndex,
			Model model) {
		helper.populateExecutionStepModel(executionId, stepIndex, model);
		addCurrentStepUrl(executionId, model);

		return "fragment/executions/step-information-menu";

	}

	@RequestMapping(value = "/step/{stepIndex}/general", method = RequestMethod.GET)
	public ModelAndView getMenuInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {
		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		ModelAndView mav = new ModelAndView("fragment/executions/step-information-fragment");

		mav.addObject("auditableEntity", executionStep);
		mav.addObject("withoutCreationInfo", true);

		return mav;

	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = { "id=execution-comment", VALUE })
	@ResponseBody
	public String updateComment(@RequestParam(VALUE) String newComment, @PathVariable("stepId") Long stepId) {
		executionProcService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionStep " + stepId.toString() + ": updated comment to " + newComment);
		return newComment;
	}

	@RequestMapping(value = "/step/{stepIndex}/new-step-infos", method = RequestMethod.GET)
	@ResponseBody
	public String getNewStepInfos(@PathVariable Long executionId, @PathVariable Integer stepIndex) {

		JsonSimpleData obj = new JsonSimpleData();

		Execution execution = executionProcService.findExecution(executionId);
		Integer total = execution.getSteps().size();
		ExecutionStep executionStep = executionProcService.findStepAt(executionId, stepIndex);

		if (executionStep == null) {
			executionStep = executionProcService.findStepAt(executionId, total - 1);
		}

		obj.addAttr("executionStepOrder", executionStep.getExecutionStepOrder().toString());
		obj.addAttr("executionStepId", executionStep.getId().toString());

		return obj.toString();
	}

	@RequestMapping(value = "/step/{stepId}", method = RequestMethod.POST, params = "executionStatus")
	@ResponseBody
	public void updateExecutionMode(@RequestParam String executionStatus, @PathVariable("stepId") long stepId) {
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		executionProcService.setExecutionStepStatus(stepId, status);
	}

}
