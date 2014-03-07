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

import javax.inject.Inject;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

@Component
@Scope("prototype")
public class SimulationFacility implements Facility{


	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private Validator validator;
	
	private Model model;
	private TestCaseValidator testCaseValidator = new TestCaseValidator();
	
	
	public void setModel(Model model){
		this.model = model;
		testCaseValidator.setModel(model);
	}
	
	
	public Model getModel(){
		return model;
	}


	/**
	 * <p>Must check the following errors : </p>
	 * <ul>
	 * 	<li>mandatory fields path, name have a value</li>
	 * 	<li>path is well formed</li>
	 * 	<li>mandatory cufs must have a value if no default is available</li>
	 * 	<li>the project actually exists</li>
	 * 	<li>can create in the project</li>
	 * 	<li>there won't be no name clash</li>
	 * 	<li>name and path are consistent</li>
	 * 	<li>format for a given field (especially lists, checkbox and dates for custom fields) are correct
	 * </ul>
	 * 
	 * <p>Must check the following warnings :</p>
	 * <ul>
	 * 	<li>which fields having a size limit 
	 * 	<li>mandatory cufs of type list will default to the default value when no value is given</li>
	 * 	<li>cufs of type list will default to the default value when the imported value is invalid</li>
	 * 	<li>when some fields have a size exceeding the size, they will be truncated to that limit</li>
	 * 	<li>mandatory cufs will default to default value if no value is imported</li>
	 * </ul>
	 */
	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {
		
		LogTrain logs = new LogTrain();
		

		// 2 - mandatory custom fields 
		Collection<CustomField> tcCufs = model.getTestCaseCufs(target);
		
		for (CustomField cuf : tcCufs){
			
			String importValue =  cufValues.get(cuf.getCode());
			if (! cuf.isOptional() && StringUtils.isBlank(importValue)){
				
			}
				
		}
		
		
	}

	@Override
	public LogTrain updateTestCase(long testCaseId, TestCase testCaseData,
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

	
	
	
	
	
	
	
	// *********************** utils ********************
	

	
	
}
