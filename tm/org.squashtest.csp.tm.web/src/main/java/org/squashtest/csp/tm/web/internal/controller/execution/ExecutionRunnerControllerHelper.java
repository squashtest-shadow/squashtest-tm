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
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.service.ExecutionProcessingService;

/**
 * Helper class for Controllers which need to show classic and optimized execution runners.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
public class ExecutionRunnerControllerHelper {
	private interface FetchStepCommand {
		ExecutionStep execute(int stepCount);
	}

	private ExecutionProcessingService executionProcessingService;

	@ServiceReference
	public void setExecutionProcessingService(ExecutionProcessingService executionProcService) {
		this.executionProcessingService = executionProcService;
	}

	public void populateExecutionRunnerModel(final long executionId, Model model) {
		FetchStepCommand command = new FetchStepCommand() {
			@Override
			public ExecutionStep execute(int stepCount) {
				if (stepCount == 0) {
					return null;
				}

				ExecutionStep executionStep = executionProcessingService.findRunningExecutionStep(executionId);

				if (executionStep == null) {
					executionStep = executionProcessingService.findStepAt(executionId, stepCount - 1);
				}

				return executionStep;
			}
		};

		populateExecutionStepModel(executionId, model, command);
	}

	public void populateExecutionStepModel(final long executionId, final int stepIndex, Model model) {
		FetchStepCommand command = new FetchStepCommand() {
			@Override
			public ExecutionStep execute(int stepCount) {
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

		populateExecutionStepModel(executionId, model, command);
	}

	public void populateExecutionStepModel(long executionId, Model model, FetchStepCommand command) {
		Execution execution = executionProcessingService.findExecution(executionId);
		Integer total = execution.getSteps().size();

		ExecutionStep executionStep = command.execute(total);
		
		int stepOrder = executionStep == null ? 0 : executionStep.getExecutionStepOrder();

		model.addAttribute("execution", execution);
		model.addAttribute("executionStep", executionStep);
		model.addAttribute("totalSteps", total);
		model.addAttribute("executionStatus", ExecutionStatus.values());
		model.addAttribute("hasPreviousStep", stepOrder != 0);
		model.addAttribute("hasNextStep", stepOrder != (total - 1));
	}

}
