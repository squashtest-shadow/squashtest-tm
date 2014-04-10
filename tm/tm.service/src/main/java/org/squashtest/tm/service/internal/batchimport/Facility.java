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

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;

public interface Facility {
	
	Model getModel();

	LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues);
	LogTrain updateTestCase(TestCaseTarget target, TestCase testCaseData, Map<String, String> cufValues);
	LogTrain deleteTestCase(TestCaseTarget target);
	
	
	LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues);
	LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase);
	LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues);
	LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase);
	LogTrain deleteTestStep(TestStepTarget target);
	
	LogTrain createParameter(ParameterTarget target, Parameter param);
	LogTrain updateParameter(ParameterTarget target, Parameter param);
	LogTrain deleteParameter(ParameterTarget target);
	
	
	/**
	 * Will update the value for the given parameter in the given dataset. If the dataset doesn't exist for this dataset, it will be created.
	 * If the parameter doesn't exist or is not available to this dataset the method fails. In all cases the methods returns a log.
	 * 
	 * @param dataset
	 * @param param
	 * @param value
	 * @return
	 */
	LogTrain failsafeUpdateParameterValue (DatasetTarget dataset, ParameterTarget param, String value);
	
	
	
	/**
	 * Deletes a dataset.
	 * 
	 * @param dataset
	 * @return
	 */
	LogTrain deleteDataset(DatasetTarget dataset);
}

