/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.testcase.steps;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

// made "final" because SONAR wants to be sure that subclasses wont mess with overrides and constructors
public final class TestStepView {
	private long id;
	private TestCase testCase;
	private int totalNumberOfSteps;
	private int order;
	private TestStep previousStep;
	private TestStep nextStep;
	private ActionTestStep actionStep;
	private CallTestStep callStep;

	public TestStepView(ActionTestStep step) {
		genericSettings(step);
		actionStep = step;

	}

	public TestStepView(CallTestStep step) {
		genericSettings(step);
		callStep = step;

	}

	private void genericSettings(TestStep step) {
		testCase = step.getTestCase();
		setTotalNumberOfSteps(testCase.getSteps().size());
		int stepIndex = testCase.getPositionOfStep(step.getId());

		order = stepIndex +1;

		if(stepIndex > 0){
			previousStep = testCase.getSteps().get(stepIndex - 1);
		}

		if(order < testCase.getSteps().size()){
			nextStep = testCase.getSteps().get(stepIndex + 1);
		}

		id = step.getId();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	public ActionTestStep getActionStep() {
		return actionStep;
	}

	public void setActionStep(ActionTestStep actionStep) {
		this.actionStep = actionStep;
	}

	public CallTestStep getCallStep() {
		return callStep;
	}

	public void setCallStep(CallTestStep callStep) {
		this.callStep = callStep;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public TestStep getPreviousStep() {
		return previousStep;
	}

	public void setPreviousStep(TestStep previousStep) {
		this.previousStep = previousStep;
	}

	public TestStep getNextStep() {
		return nextStep;
	}

	public void setNextStep(TestStep nextStep) {
		this.nextStep = nextStep;
	}

	public int getTotalNumberOfSteps() {
		return totalNumberOfSteps;
	}

	public void setTotalNumberOfSteps(int totalNumberOfSteps) {
		this.totalNumberOfSteps = totalNumberOfSteps;
	}

}
