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
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
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

	/**
	 * @see
	 */
	@Override
	public List<Parameter> findAllforTestCase(long testCaseId) {
		List<Long> testCaseIds = new ArrayList<Long>(this.callTreeFinder.getTestCaseCallTree(testCaseId));
		testCaseIds.add(testCaseId);
		return parameterDao.findAllByTestCases(testCaseIds);
	}

	/**
	 * @see ParameterModificationService#addNewParameterToTestCase(Parameter, long)
	 */
	@Override
	public void addNewParameterToTestCase(Parameter parameter, long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		addNewParameterToTestCase(parameter, testCase);
	}

	private void addNewParameterToTestCase(Parameter parameter, TestCase testCase) {
		parameter.setTestCase(testCase);
		updateDatasetsForParameterCreation(parameter, parameter.getTestCase().getId());
	}

	/**
	 * @see ParameterModificationService#addNewParameterToTestCase(Parameter, long)
	 */
	@Override
	public void changeName(long parameterId, String newName) {
		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setName(newName);
	}

	/**
	 * @see ParameterModificationService#changeDescription(long, String)
	 */
	@Override
	public void changeDescription(long parameterId, String newDescription) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setDescription(newDescription);
	}

	/**
	 * @see ParameterModificationService#remove(Parameter)
	 */
	@Override
	public void remove(Parameter parameter) {
		this.parameterDao.remove(parameter);
	}

	/**
	 * @see ParameterModificationService#removeAllByTestCaseIds(List)
	 */
	@Override
	public void removeAllByTestCaseIds(List<Long> testCaseIds) {
		// note : hibernate bulk delete don't care of cascade delete so we have to remove the values by ourselves
		this.parameterDao.removeAllValuesByTestCaseIds(testCaseIds);
		this.parameterDao.removeAllByTestCaseIds(testCaseIds);
	}

	/**
	 * @see ParameterModificationService#removeById(long)
	 */
	@Override
	public void removeById(long parameterId) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		this.parameterDao.remove(parameter);
	}

	/**
	 * @see ParameterModificationService#createParamsForStep(long)
	 */
	@Override
	public void createParamsForStep(long stepId) {
		TestStep step = testStepDao.findById(stepId);
		createParamsForStep(step);
	}

	/**
	 * @see ParameterModificationService#createParamsForStep(TestStep)
	 */
	public void createParamsForStep(TestStep step) {
		Set<String> parameterNames = new ParameterNamesFinder().findParametersNamesInActionAndExpectedResult(step);
		for (String name : parameterNames) {
			createParameterIfNotExists(name, step.getTestCase());
		}
	}

	/**
	 * Will first check for a parameter of the given name in the test case. If there is none, will create one. When a
	 * parameter is created, the datasets of the test case and it's calling test cases will be updated in consequence.
	 * 
	 * @param name
	 *            : the name of the potential new Parameter
	 * @param testCase
	 *            : the testCase to add the potential new parameter to
	 */
	private void createParameterIfNotExists(String name, TestCase testCase) {
		if (testCase != null) {
			Parameter parameter = testCase.findParameterByName(name);
			if (parameter == null) {
				parameter = new Parameter(name);
				addNewParameterToTestCase(parameter, testCase);
			}
		}
	}

	/**
	 * @see ParameterModificationService#isUsed(long)
	 */
	@Override
	public boolean isUsed(long parameterId) {
		Parameter parameter = this.parameterDao.findById(parameterId);
		long testCaseId = parameter.getTestCase().getId();
		return testStepDao.stringIsFoundInStepsOfTestCase(parameter.getParamStringAsUsedInStep(), testCaseId);
	}

	private void updateDatasetsForParameterCreation(Parameter parameter, long testCaseId) {

		// get all test cases who call this test case
		List<Long> testCaseIds = new ArrayList<Long>(this.callTreeFinder.getTestCaseCallers(testCaseId));
		testCaseIds.add(testCaseId);
		// get all datasets for local test case or test case who call this test case
		List<Dataset> datasets = datasetDao.findAllDatasetsByTestCases(testCaseIds);

		// add parameter entry to these datasets
		for (Dataset dataset : datasets) {
			DatasetParamValue datasetParamValue = new DatasetParamValue(parameter, dataset, "");
			dataset.addParameterValue(datasetParamValue);
		}
	}

	/**
	 * @see ParameterModificationService#findById(long)
	 */
	@Override
	public Parameter findById(long parameterId) {
		return parameterDao.findById(parameterId);
	}

	/**
	 * @see ParameterModificationService#createParamsForTestCaseSteps(TestCase)
	 */
	public void createParamsForTestCaseSteps(TestCase testCase) {
		for (TestStep step : testCase.getActionSteps()) {
			createParamsForStep(step);
		}

	}

	public static List<String> findUsedParamsNamesTestCaseSteps(TestCase testCase) {
		return null;
	}

}
