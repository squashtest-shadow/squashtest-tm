/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.api.testautomation;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;
import org.squashtest.tm.service.testautomation.AutomatedExecutionManagerService;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;

/**
 * This controller receives callbacks from Squash TA which modify automated executions statuses.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/resultUpdate/{unidentifiedHostId}/{taProjectName}/{automatedSuiteId}")
public class AutomatedExecutionManagerController {
	@Inject
	private AutomatedExecutionManagerService automatedExecutionManager;

	/**
	 * Changes the status of all executions matching an automated test case and an automated execution suite
	 * 
	 * @param taProjectName
	 *            matches TestAutomationProject.name, **not** TAP.label
	 * @param automatedSuiteId
	 * @param automatedTestPath
	 * @param automatedTestName
	 * @param stateChange
	 */
	@RequestMapping(value = "testStatus/{automatedTestPath}/{automatedTestName}", method = RequestMethod.POST)
	public @ResponseBody
	void changeExecutionsStates(@PathVariable String taProjectName, @PathVariable String automatedSuiteId,
			@PathVariable String automatedTestPath, @PathVariable String automatedTestName,
			@RequestBody @Valid TestExecutionStatus stateChange) {

		AutomatedExecutionSetIdentifier setIdentifier = SquashTaExecutionIdentifierBuilder.builder()
				.automatedSuiteId(automatedSuiteId)
				.automatedTestName(stateChange.getTestName())
				.automatedTestPath(automatedTestPath)
				.automationProjectName(taProjectName)
				.build();

		automatedExecutionManager.changeExecutionsStates(setIdentifier, stateChange);
	}
}
