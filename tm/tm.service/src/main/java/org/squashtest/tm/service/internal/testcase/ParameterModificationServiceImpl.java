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
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepReader;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.testcase.ParameterModificationService;

@Service("squashtest.tm.service.ParameterModificationService")
public class ParameterModificationServiceImpl implements ParameterModificationService {

	@Inject
	private ParameterDao parameterDao;

	@Inject
	private TestStepDao testStepDao;
	
	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private DatasetParamValueDao datasetParamValueDao;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	private static final String PARAM_PATTERN = "(.*?)(\\Q${\\E(.*?)\\Q}\\E)(.*?)";

	@Override
	public List<Parameter> getAllforTestCase(long testCaseId) {
		List<Long> testCaseIds = this.getCallStepTree(testCaseId);
		return parameterDao.findAllByTestCases(testCaseIds);
	}

	@Override
	public void persist(Parameter parameter, long testCaseId) {
		
		Parameter sameName = this.parameterDao.findParameterByNameAndTestCase(parameter.getName(), testCaseId);
		if (sameName != null) {
			throw new DuplicateNameException(sameName.getName(), parameter.getName());
		} else {
			TestCase testCase = testCaseDao.findById(testCaseId);
			parameter.setTestCase(testCase);
			testCase.addParameter(parameter);
			updateDatasetsForParameterCreation(parameter, parameter.getTestCase().getId());
		}
	}

	@Override
	public void changeName(long parameterId, String newName) {
		Parameter parameter = this.parameterDao.findById(parameterId);
		String oldName = parameter.getName();
		if(!oldName.equals(newName)){
			Parameter sameName = this.parameterDao.findParameterByNameAndTestCase(newName, parameter.getTestCase().getId());
			if (sameName != null) {
				throw new DuplicateNameException(oldName, newName);
			} else {
				parameter.setName(newName);
				updateParamNameInSteps(parameter, oldName, newName);
			}
		}
	}

	private void updateParamNameInSteps(Parameter parameter, String oldName, String newName) {

		for (TestStep step : parameter.getTestCase().getSteps()) {
			TestStepContentUpdater updater = new TestStepContentUpdater(oldName, newName, PARAM_PATTERN);
			if (step != null) {
				step.accept(updater);
			}
		}
	}

	@Override
	public void changeDescription(long parameterId, String newDescription) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setDescription(newDescription);
	}

	@Override
	public void remove(Parameter parameter) {

		this.parameterDao.remove(parameter);
	}
	
	@Override
	public void removeAllByTestCaseIds(List<Long> testCaseIds) {
		//note : hibernate bulk delete don't care of cascade delete so we have to remove the values by ourselves
		this.parameterDao.removeAllValuesByTestCaseIds(testCaseIds);
		this.parameterDao.removeAllByTestCaseIds(testCaseIds);
	}
	

	@Override
	public void removeById(long parameterId) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		this.parameterDao.remove(parameter);
	}

	@Override
	public List<Parameter> checkForParamsInStep(long stepId) {
		TestStep step = testStepDao.findById(stepId);
		return createParamsForStep(step);
	}

	/**
	 * Will go through step's content if action step, see if the parameter pattern is found, and, if so, will create all
	 * corresponding parameters in the test case. Will not create homonymes parameters in the test case.
	 * 
	 * @param step
	 * @return
	 */
	private static List<Parameter> createParamsForStep(TestStep step) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		TestStepContentReader reader = new TestStepContentReader();
		String content = step.accept(reader);
		for (String name : parseStepContent(content)) {
			if(name.matches(Parameter.CODE_REGEXP)){
				parameters.add(findOrCreateParameter(name, step.getTestCase()));
			}
		}
		return parameters;
	}

	private List<Long> getCallStepTree(Long testCaseId) {
		List<Long> testCaseIds = new ArrayList<Long>();

		for (Long id : this.callTreeFinder.getTestCaseCallTree(testCaseId)) {
			testCaseIds.add(id);
		}
		testCaseIds.add(testCaseId);
		return testCaseIds;
	}

	private List<Long> getCallers(Long testCaseId) {
		List<Long> testCaseIds = new ArrayList<Long>();

		for (Long id : this.callTreeFinder.getTestCaseCallers(testCaseId)) {
			testCaseIds.add(id);
		}
		testCaseIds.add(testCaseId);
		return testCaseIds;
	}

	private static List<String> parseStepContent(String content) {

		List<String> paramNames = new ArrayList<String>();
		String paramPattern = PARAM_PATTERN;

		Pattern pattern = Pattern.compile(paramPattern);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			paramNames.add(matcher.group(3));
		}
		return paramNames;
	}

	private static Parameter findOrCreateParameter(String name, TestCase testCase) {
		Parameter parameter = testCase.findParameterByName(name);
		if (parameter == null) {
			parameter = new Parameter(name, testCase);
		}
		return parameter;
	}

	private static final class TestStepContentUpdater implements TestStepVisitor {

		private String oldParamName;
		private String newParamName;
		private String paramPattern;

		public TestStepContentUpdater(String oldParamName, String newParamName, String paramPattern) {
			this.oldParamName = oldParamName;
			this.newParamName = newParamName;
			this.paramPattern = paramPattern;
		}

		private String replace(String content) {

			String result = content;
			if (result != null && result.length() > 0) {
				Pattern pattern = Pattern.compile(paramPattern);
				Matcher matcher = pattern.matcher(result);

				while (matcher.find()) {
					String paramName = matcher.group(3);
					if (paramName.equals(oldParamName)) {
						result = result.replace(oldParamName, newParamName);
					}
				}
			}
			return result;
		}

		@Override
		public void visit(ActionTestStep visited) {
			visited.setAction(replace(visited.getAction()));
			visited.setExpectedResult(replace(visited.getExpectedResult()));
		}

		@Override
		public void visit(CallTestStep visited) {

		}
	}

	private static final class TestStepContentReader implements TestStepReader {

		private TestStepContentReader() {

		}

		@Override
		public String visit(ActionTestStep visited) {
			return visited.getAction() + " " + visited.getExpectedResult();
		}

		@Override
		public String visit(CallTestStep visited) {
			StringBuilder builder = new StringBuilder();
			for (TestStep step : visited.getCalledTestCase().getSteps()) {
				builder.append(" " + step.accept(this) + " ");
			}
			return builder.toString();
		}

	}

	@Override
	public boolean isUsed(long parameterId, long testCaseId) {
		Parameter parameter = this.parameterDao.findById(parameterId);
		return isUsed(parameter.getName(), testCaseId);
	}

	@Override
	public boolean isUsed(String parameterName, long testCaseId) {
		Parameter parameter = this.parameterDao.findParameterByNameAndTestCase(parameterName, testCaseId);
		return parameter != null;
	}

	@Override
	public boolean isUsed(long parameterId) {
		Parameter parameter = this.parameterDao.findById(parameterId);
		return isUsed(parameter.getName(), parameter.getTestCase().getId());
	}

	private void updateDatasetsForParameterCreation(Parameter parameter, long testCaseId) {

		// get all test cases who call this test case
		List<Long> testCaseIds = this.getCallers(testCaseId);

		// get all datasets for local test case or test case who call this test case
		List<Dataset> datasets = datasetDao.findAllDatasetsByTestCases(testCaseIds);

		// add parameter entry to these datasets
		for (Dataset dataset : datasets) {
			DatasetParamValue datasetParamValue = new DatasetParamValue();
			datasetParamValue.setParameter(parameter);
			datasetParamValue.setParamValue("");
			datasetParamValue.setDataset(dataset);
			dataset.addParameterValue(datasetParamValue);
		}
	}

	@Override
	public Parameter getById(long testCaseId) {
		return parameterDao.findById(testCaseId);
	}
	
	/**
	 * 
	 * @param testCase
	 */
	public static void createParamsForTestCaseSteps(TestCase testCase) {
		for(TestStep step : testCase.getActionSteps()){
			createParamsForStep(step);
		}
		
		
	}

}
