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

import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

public class RunnerState {

	
	private boolean isLastTestCase;
	private boolean testSuiteMode;
	private boolean isOptimized;
	private boolean isPrologue;
	
	private String baseStepUrl;
	private String nextTestCaseUrl;
	
	private long currentExecutionId;
	private long currentStepId;
	
	private int firstStepIndex = 0;
	private int lastStepIndex;
	private int currentStepIndex;
	
	private ExecutionStatus currentStepStatus;

	private String completeTitle;
	private String completeTestMessage;
	private String completeSuiteMessage;
	
	
	
	
	public boolean isLastTestCase() {
		return isLastTestCase;
	}

	public void setLastTestCase(boolean isLastTestCase) {
		this.isLastTestCase = isLastTestCase;
	}

	public boolean isTestSuiteMode() {
		return testSuiteMode;
	}

	public void setTestSuiteMode(boolean testSuiteMode) {
		this.testSuiteMode = testSuiteMode;
	}

	public boolean isPrologue() {
		return isPrologue;
	}

	public void setPrologue(boolean isPrologue) {
		this.isPrologue = isPrologue;
	}

	public String getBaseStepUrl() {
		return baseStepUrl;
	}

	public void setBaseStepUrl(String baseStepUrl) {
		this.baseStepUrl = baseStepUrl;
	}

	public String getNextTestCaseUrl() {
		return nextTestCaseUrl;
	}

	public void setNextTestCaseUrl(String nextTestCaseUrl) {
		this.nextTestCaseUrl = nextTestCaseUrl;
	}

	public long getCurrentExecutionId() {
		return currentExecutionId;
	}

	public void setCurrentExecutionId(long currentExecutionId) {
		this.currentExecutionId = currentExecutionId;
	}

	public long getCurrentStepId() {
		return currentStepId;
	}

	public void setCurrentStepId(long currentStepId) {
		this.currentStepId = currentStepId;
	}

	public int getFirstStepIndex() {
		return firstStepIndex;
	}

	public void setFirstStepIndex(int firstStepIndex) {
		this.firstStepIndex = firstStepIndex;
	}

	public int getLastStepIndex() {
		return lastStepIndex;
	}

	public void setLastStepIndex(int lastStepIndex) {
		this.lastStepIndex = lastStepIndex;
	}

	public int getCurrentStepIndex() {
		return currentStepIndex;
	}

	public void setCurrentStepIndex(int currentStepIndex) {
		this.currentStepIndex = currentStepIndex;
	}

	public ExecutionStatus getCurrentStepStatus() {
		return currentStepStatus;
	}

	public void setCurrentStepStatus(ExecutionStatus currentStepStatus) {
		this.currentStepStatus = currentStepStatus;
	}

	public String getCompleteTitle() {
		return completeTitle;
	}

	public void setCompleteTitle(String completeTitle) {
		this.completeTitle = completeTitle;
	}

	public String getCompleteTestMessage() {
		return completeTestMessage;
	}

	public void setCompleteTestMessage(String completeTestMessage) {
		this.completeTestMessage = completeTestMessage;
	}

	public String getCompleteSuiteMessage() {
		return completeSuiteMessage;
	}

	public void setCompleteSuiteMessage(String completeSuiteMessage) {
		this.completeSuiteMessage = completeSuiteMessage;
	}

	public boolean isOptimized() {
		return isOptimized;
	}

	public void setOptimized(boolean isOptimized) {
		this.isOptimized = isOptimized;
	}

	
	
	
}
