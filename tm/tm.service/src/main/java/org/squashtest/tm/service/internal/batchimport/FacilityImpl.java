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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;

@Component
@Scope("prototype")
public class FacilityImpl implements Facility {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class); 

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private TestCaseLibraryNavigationService navigationService;
	
	@Inject
	private TestCaseModificationService testcaseModificationService;
	
	@Inject
	private TestStepModificationService stepModificationService;
	
	@Inject
	private CustomFieldDao cufDao;
	
	
	private SimulationFacility simulator;
	
	private Model model;
	
	private Map<String, Long> cufIdByCode = new HashMap<String, Long>();

	
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

		LogTrain train = simulator.createTestCase(target, testCase, cufValues);
		
		if (! train.hasCriticalErrors()){
			try{
				reallyCreateTestcase(target, testCase, cufValues);
				model.setExists(target, testCase.getId());
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				model.setNotExists(target);
				LOGGER.error("Excel import : unexpected error while importing "+target+" : ", ex);
			}
		}
		
		return train;
		
	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCaseData,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, TestStep testStep,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, TestStep testStep,
			TestCaseTarget calledTestCase) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public LogTrain updateActionStep(TestStepTarget target, TestStep testStep,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, TestStep testStep,
			TestCaseTarget calledTestCase) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	
	// ************************* private stuffs **************************
	
	// because the service identifies cufs by their id, not their code
	private Map<Long, String> toAcceptableCufs(Map<String, String> origCufs){
		// Map<Long, String> result = new HashMap<Long, String>(origCufs.size());
		 throw new RuntimeException("implement that");
	}
	
	// because this time we're not f'ng around man, this is the real thing
	private void reallyCreateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) throws Exception{
				
	}


}
