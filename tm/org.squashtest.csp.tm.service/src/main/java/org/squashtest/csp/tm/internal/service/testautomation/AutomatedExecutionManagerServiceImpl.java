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

package org.squashtest.csp.tm.internal.service.testautomation;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.internal.repository.testautomation.AutomatedExecutionExtenderDao;
import org.squashtest.csp.tm.service.testautomation.AutomatedExecutionManagerService;
import org.squashtest.csp.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus;


/**
 * @author Gregory Fouquet
 *
 */
@Service("squashtest.tm.service.testautomation.AutomatedExecutionManagerService")
@Transactional
public class AutomatedExecutionManagerServiceImpl implements AutomatedExecutionManagerService {
	@Inject private AutomatedExecutionExtenderDao automatedExecutionDao;
	/**
	 * @see org.squashtest.csp.tm.service.testautomation.AutomatedExecutionManagerService#changeExecutionsStates(org.squashtest.csp.tm.service.testautomation.AutomatedExecutionSetIdentifier, org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus)
	 */
	@Override
	public void changeExecutionsStates(@NotNull AutomatedExecutionSetIdentifier setIdentifier,
			@NotNull TestExecutionStatus stateChange) {
		List<AutomatedExecutionExtender> execs = automatedExecutionDao.findAllBySuiteIdAndTestName(setIdentifier.getAutomatedSuiteId(), setIdentifier.getAutomatedTestName(), setIdentifier.getTestAutomationProjectName());

		for (AutomatedExecutionExtender exec : execs) {
			changeState(exec, stateChange);
		}
	}
	/**
	 * @param exec
	 * @param stateChange
	 */
	private void changeState(AutomatedExecutionExtender exec, TestExecutionStatus stateChange) {
		exec.setResultSummary(stateChange.getStatusMessage());
		exec.setExecutionStatus(coerce(stateChange.getStatus()));
		
	}
	/**
	 * @param status
	 * @return
	 */
	private ExecutionStatus coerce(org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus status) {
		return ExecutionStatus.valueOf(status.name());
	}

}
