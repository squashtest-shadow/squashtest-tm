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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_NAME;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_REFERENCE;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.TO_BE_DELETED;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.NOT_EXISTS;


import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.library.WhichNodeVisitor;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;

class EntityValidator {

	private Model model;
	
	
	//private Validator validator = ValidatorFactoryBean.getInstance().getValidator();

	Model getModel() {
		return model;
	}

	void setModel(Model model) {
		this.model = model;
	}


	/**
	 *  those checks are run for a test case for any type of operations.
	 *  
	 *  It checks : 
	 *  - the path is well formed (failure)
	 *  - the test case has a name (failure)
	 *  - the test case name has length between 0 and 255
	 *  - the project exists (failure)
	 *  - the size of fields that are restricted in size  (warning)
	 * 
	 * @param target
	 * @param testCase
	 * @return
	 */
	LogTrain basicTestCaseChecks(TestCaseTarget target, TestCase testCase){
		
		LogTrain logs = new LogTrain();
		String[] fieldNameErrorArgs = new String[]{TC_NAME.header};	// that variable is simple convenience for logging
		
		// 1 - path must be supplied and and well formed
		if (! target.isWellFormed()){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MALFORMED_PATH));
		}
		
		// 2 - name must be supplied
		String name = testCase.getName();
		if (StringUtils.isBlank(name)){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_FIELD_MANDATORY, fieldNameErrorArgs));
		}
		
		// 3 - the project actually exists
		TargetStatus projectStatus = model.getProjectStatus(target.getProject()); 
		if (projectStatus.getStatus() != Existence.EXISTS){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PROJECT_NOT_EXIST));
		}
		
		// 4 - name has length between 0 and 255		
		if (name != null && name.length() > 255){
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, fieldNameErrorArgs, Messages.IMPACT_MAX_SIZE, null));
		}

		// 5 - reference, if exists, has length between 0 and 50
		String reference = testCase.getReference();
		if (! StringUtils.isBlank(reference) && reference.length() > 50){
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, new String[]{TC_REFERENCE.header}));
		}
		
		return logs;
	}
	
	
	
	/**
	 *  those checks are run for a test step for any type of operations.
	 *  
	 *  It checks : 
	 *  - the path of the test case is well formed (failure)
	 *  - the project exists (failure)
	 *  - the format of the custom fields (lists, dates and checkbox) (warning)
	 * 
	 * 
	 * 
	 * @param target
	 * @param testStep
	 * @return
	 */
	LogTrain basicTestStepChecks(TestStepTarget target){
		
		LogTrain logs = new LogTrain();
		
		TestCaseTarget testCase = target.getTestCase();
		
		// 1 - test case owner path must be supplied and and well formed
		if (! testCase.isWellFormed()){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MALFORMED_PATH));
		}
		
		// 2 - the test case must exist
		TargetStatus tcStatus = model.getStatus(testCase);
		if (tcStatus.status == TO_BE_DELETED || tcStatus.status == NOT_EXISTS){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_NOT_FOUND));
		}
		
		// 3 - the project actually exists
		TargetStatus projectStatus = model.getProjectStatus(target.getProject()); 
		if (projectStatus.getStatus() != Existence.EXISTS){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PROJECT_NOT_EXIST));
		}
		
		return logs;

	}
	
	LogTrain basicTestStepChecks(TestStepTarget target, TestStep testStep){
		
		// for now nothing much more to do with the TestStep
		return basicTestStepChecks(target);

	}
	
	
	LogTrain validateCallStep(TestStepTarget target, TestStep testStep, TestCaseTarget calledTestCase){
		
		LogTrain logs = new LogTrain();
		
		TargetStatus calledStatus = model.getStatus(calledTestCase);
		
		// 1 - the target must exist and be valid
		if (calledStatus.status == NOT_EXISTS || calledStatus.status == TO_BE_DELETED || ! calledTestCase.isWellFormed()){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, "message.import.log.error.tc.callStep.calledTcNotFound"));
		}
		
		// 2 - there must be no cyclic calls
		if (model.wouldCreateCycle(target, calledTestCase)){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, 
										"message.import.log.error.tc.callStep.cyclicCalls", 
										new Object[]{target.getTestCase().getPath(), calledTestCase.getPath()}));
		}
		
		return logs;
		
	}
	
	
	
}
