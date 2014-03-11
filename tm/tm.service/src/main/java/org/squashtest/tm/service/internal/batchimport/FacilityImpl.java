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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Map;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

@Component
@Scope("prototype")
public class FacilityImpl implements Facility {

	@Inject
	private SessionFactory sessionFactory;
	
	private SimulationFacility simulator;
	
	private Model model;

	
	public SimulationFacility getSimulator() {
		return simulator;
	}

	public void setSimulator(SimulationFacility simulator) {
		this.simulator = simulator;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		//LogTrain train = simulator.createTestCase(target, testCase, cufValues);
		
		
		throw new UnsupportedOperationException("not implemented yet");
		
	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCaseData,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestCase(long testCaseId) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestCase(TestCase testCase) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain addTestStep(TestStepTarget target, TestStep testStep,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain updateTestStep(long testStepId, TestStep testStepData) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestStep(long testStepId) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestStep(TestStep testStep) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

}
