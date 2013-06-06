/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepReader;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.testcase.ParameterModificationService;

@Service("squashtest.tm.service.ParameterModificationService")
public class ParameterModificationServiceImpl implements ParameterModificationService {

	@Inject
	private ParameterDao parameterDao;
	
	@Inject
	private TestStepDao testStepDao;
	
	@Inject
	private TestCaseCallTreeFinder callTreeFinder;
	
	@Override
	public List<Parameter> getAllforTestCase(long testCaseId) {
		
		List<Long> testCaseIds = new ArrayList<Long>();
		
		for(Long id : this.callTreeFinder.getTestCaseCallTree(testCaseId)){
			testCaseIds.add(id);
		}
		testCaseIds.add(testCaseId);
		
		return parameterDao.findAllByTestCases(testCaseIds);
	}
	
	@Override
	public void persist(Parameter parameter) {
		this.parameterDao.persist(parameter);
	}

	@Override
	public void changeName(long parameterId, String newName) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setName(newName);
		
	}

	@Override
	public void changeDescription(long parameterId, String newDescription) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setDescription(newDescription);
	}

	@Override
	public void remove(Parameter parameter) {
		
		this.parameterDao.delete(parameter);
	}

	@Override
	public void removeById(long parameterId) {
		
		Parameter parameter = this.parameterDao.findById(parameterId);
		this.parameterDao.delete(parameter);
	}
	
	@Override
	public List<Parameter> checkForParamsInStep(long stepId) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		TestStep step = testStepDao.findById(stepId);
		TestStepContentReader reader = new TestStepContentReader();
		String content = step.accept(reader);
		for(String name : this.parseStepContent(content)){
			parameters.add(findOrCreateParameter(name, step.getTestCase()));
		}
		return parameters;
	}
	
	private List<String> parseStepContent(String content){
		
		List<String> paramNames = new ArrayList<String>();
		String paramPattern = "(.*?)*$\\{(.*?)\\}.*?";
		
        Pattern pattern = Pattern.compile(paramPattern);
        Matcher matcher = pattern.matcher(content);
        
        while(matcher.find()){ 
        	paramNames.add(matcher.group(2)); 
        } 
        return paramNames;
	}
	
	private Parameter findOrCreateParameter(String name, TestCase testCase){
		Parameter parameter = this.parameterDao.findParameterByNameAndTestCase(name, testCase.getId());
		if(parameter == null){
			parameter = this.createMissingParameter(name, testCase);
		}
		return parameter;
	}
	
	private Parameter createMissingParameter(String name, TestCase testCase){
		Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setDescription("");
		parameter.setTestCase(testCase);
		this.persist(parameter);
		return parameter;
	}

	private final class TestStepContentReader implements TestStepReader {

		private TestStepContentReader() {

		}

		@Override
		public String visit(ActionTestStep visited) {
			return visited.getAction()+" "+visited.getExpectedResult();
		}

		@Override
		public String visit(CallTestStep visited) {
			StringBuilder builder = new StringBuilder();
			for(TestStep step : visited.getCalledTestCase().getSteps()){
				builder.append(" "+step.accept(this)+" ");
			}
			return builder.toString();
		}

	}
}
