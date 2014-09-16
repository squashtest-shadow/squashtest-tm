/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.batchimport;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * 
 * @Implementation of batch import method that won't update the database. It justs updates an internal model
 * (held by a ValidationFacility) when it sees fit, depending on the sanity of the data and the operation being
 * simulated.
 * 
 */
@Component
@Scope("prototype")
public class SimulationFacility implements Facility {

	@Inject
	private ValidationFacility validator;


	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain logs = validator.createTestCase(target, testCase, cufValues);

		/*
		 * Create TestCase : how to update the model.
		 * 
		 * If there were no errors, the target is set to be created. If there is at least one failure the target wont be
		 * created and is considered as non existent, and further operations referring to that test case will
		 * consequently fail (import on steps etc).
		 * 
		 * Note that in case of path clash, we consider that the imported test case (target) shadows the one that
		 * already exists in the DB. We update the model according to the success or failure of the create operation
		 * only and the status of an already existent test case is irrelevant.
		 */
		if (!logs.hasCriticalErrors()) {
			validator.getModel().setToBeCreated(target);
		} else {
			validator.getModel().setNotExists(target);
		}

		return logs;

	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain logs = validator.updateTestCase(target, testCase, cufValues);

		/*
		 * In case of an update, we don't need to change the model regardless of the success or failure of the
		 * operation.
		 */

		return logs;

	}

	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {

		LogTrain logs = validator.deleteTestCase(target);

		// if no fatal error, update the model
		if (!logs.hasCriticalErrors()) {
			validator.getModel().setToBeDeleted(target);
		}

		return logs;
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain logs = validator.addActionStep(target, testStep, cufValues);

		// if no fatal errors, update the model
		if (!logs.hasCriticalErrors()) {
			validator.getModel().addActionStep(target);
		}

		return logs;

	}


	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo, ActionTestStep actionBackupStep) {

		LogTrain logs = validator.addCallStep(target, testStep, calledTestCase, paramInfo, actionBackupStep);

		// update the model if no fatal flaws were detected
		if (!logs.hasCriticalErrors()) {
			String mustImportCallAsActionStepErrorI18n = FacilityUtils.mustImportCallAsActionStep(logs);
			if (mustImportCallAsActionStepErrorI18n != null) {
				validator.getModel().addActionStep(target);
			} else {
				validator.getModel().addCallStep(target, calledTestCase, paramInfo);
			}
		}
		return logs;
	}


	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain logs = validator.updateActionStep(target, testStep, cufValues);

		// no need to update the model

		return logs;

	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain logs = validator.updateCallStep(target, testStep, calledTestCase, paramInfo, actionStepBackup);

		// if all is ok, update the target of this call step then return
		if (!logs.hasCriticalErrors()) {
			validator.getModel().updateCallStepTarget(target, calledTestCase, paramInfo);
		}

		return logs;
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {

		LogTrain logs = validator.deleteTestStep(target);

		// if all went well, we can remove that step from the model
		if (!logs.hasCriticalErrors()) {
			validator.getModel().remove(target);
		}

		return logs;
	}

	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {

		LogTrain logs = validator.createParameter(target, param);

		// if no problems, add it to the model (the model is double-insertion proof)
		if (!logs.hasCriticalErrors()) {
			validator.getModel().addParameter(target);
		}

		return logs;
	}

	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {

		LogTrain logs = validator.updateParameter(target, param);

		// if the parameter didn't exist, it is created on the fly
		if (! logs.hasCriticalErrors()){
			validator.getModel().addParameter(target);
		}

		return logs;

	}

	@Override
	public LogTrain deleteParameter(ParameterTarget target) {

		LogTrain logs = validator.deleteParameter(target);

		// if all is ok let's proceed
		if (!logs.hasCriticalErrors()) {
			validator.getModel().removeParameter(target);
		}

		return logs;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value, boolean isUpdate) {

		LogTrain logs = validator.failsafeUpdateParameterValue(dataset, param, value, isUpdate);

		if (!logs.hasCriticalErrors()) {
			// when a parameter is created or updated, the dataset must be created on the fly
			// note that this operation can be invoked multiple times, a given dataset will be created only once
			validator.getModel().addDataset(dataset);
		}

		return logs;
	}

	@Override
	public LogTrain createDataset(DatasetTarget dataset) {

		LogTrain logs = validator.createDataset(dataset);

		// if ok, update the model
		if (!logs.hasCriticalErrors()) {
			validator.getModel().addDataset(dataset);
		}

		return logs;

	}

	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {

		LogTrain logs = validator.deleteDataset(dataset);

		// 4 - if ok, update the model
		if (!logs.hasCriticalErrors()) {
			validator.getModel().removeDataset(dataset);
		}

		return logs;

	}

}
