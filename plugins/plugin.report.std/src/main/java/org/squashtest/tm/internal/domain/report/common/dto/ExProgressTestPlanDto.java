/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.internal.domain.report.common.dto;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.ExecutionStatus;

public class ExProgressTestPlanDto {
	private String testCaseName;
	private ExecutionStatus executionStatus;
	private ExProgressIterationDto iteration;

	public ExProgressTestPlanDto() {

	}

	public ExProgressIterationDto getIteration() {
		return iteration;
	}

	public void setIteration(ExProgressIterationDto iteration) {
		this.iteration = iteration;
	}

	public ExProgressTestPlanDto(String testCaseName, ExecutionStatus executionStatus) {
		super();
		this.testCaseName = testCaseName;
		this.executionStatus = executionStatus;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public ExProgressTestPlanDto fillBasicInfo(IterationTestPlanItem testPlan) {

		if(testPlan.isTestCaseDeleted()){
			this.testCaseName = null;
		} else {
			String reference = testPlan.getReferencedTestCase().getReference();
			if(reference != null && reference.isEmpty()){
				this.testCaseName = testPlan.isTestCaseDeleted() ? null : testPlan.getReferencedTestCase().getName();
			} else {
				this.testCaseName = testPlan.isTestCaseDeleted() ? null : reference + " - " + testPlan.getReferencedTestCase().getName();
			}
		}
		this.executionStatus = testPlan.getExecutionStatus();
		return this;
	}

}
