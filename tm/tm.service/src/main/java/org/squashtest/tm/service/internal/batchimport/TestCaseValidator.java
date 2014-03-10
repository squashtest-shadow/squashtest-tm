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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;

class TestCaseValidator {
	
	private static final String FIELD_NAME = "TC_NAME";
	private static final String FIELD_REF = "TC_REFERENCE";
	

	private Model model;
	
	//private Validator validator = ValidatorFactoryBean.getInstance().getValidator();

	Model getModel() {
		return model;
	}

	void setModel(Model model) {
		this.model = model;
	}


	/**
	 *  those checks are performed for a test case for any type of operations.
	 *  
	 *  It checks : 
	 *  - the path is well formed (failure)
	 *  - the test case has a name (failure)
	 *  - the test case name has length between 0 and 255
	 *  - the project exists (failure)
	 *  - the size of fields that are restricted in size  (warning)
	 *  - the format of the custom fields (lists, dates and checkbox) (warning)
	 * 
	 * @param target
	 * @param testCase
	 * @param cufValues
	 * @return
	 */
	LogTrain basicTestCaseChecks(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues){
		
		LogTrain logs = new LogTrain();
		String[] fieldNameErrorArgs = new String[]{FIELD_NAME};	// that variable is simple convenience for logging
		
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
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, new String[]{FIELD_REF}));
		}
		
		return logs;
	}
	
	

	
}
