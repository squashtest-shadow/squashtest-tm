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
package org.squashtest.tm.web.internal.controller.testcase;

import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestStep;

public class TestStepView {
	private long id;
	private TestCase testCase;
	private int order;
	private TestStep previousStep;
	private TestStep nextStep;
	private ActionTestStep actionStep;
	private CallTestStep callStep;

	public TestStepView(ActionTestStep step) {
		temporaryFake(step);
		actionStep = step;

	}

	public TestStepView(CallTestStep step) {
		temporaryFake(step);
		callStep = step;

	}

	private void temporaryFake(TestStep step) {
		// TODO see what ben is doing with that
		testCase = new TestCase();
		testCase.setName("FAKE TC");

		//  int stepIndex = testCase.getSteps().indexOf(step);
		//order = stepIndex +1
		order = 1;
		
//		if(stepIndex > 0){
//			previousStep = testCase.getSteps().get(stepIndex - 1);
//		}
		setPreviousStep(step);
		
//		if(order < testCase.getSteps().size()){
//			nextStep = testCase.getSteps().get(stepIndex + 1);
//		}
		//setNextStep(step);
		
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

}
