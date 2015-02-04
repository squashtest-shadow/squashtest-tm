/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import static org.squashtest.tm.service.internal.batchimport.Model.Existence.NOT_EXISTS;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.TO_BE_DELETED;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_NAME;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_REFERENCE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.StepSheetColumn;

class EntityValidator {

	private final ValidationFacilitySubservicesProvider subservicesProvider;

	Model getModel() {
		return subservicesProvider.getModel();
	}

	InfoListItemFinderService getInfoListItemService(){
		return subservicesProvider.getInfoListItemService();
	}

	public EntityValidator(ValidationFacilitySubservicesProvider modelProvider) {
		super();
		this.subservicesProvider = modelProvider;
	}

	/**
	 * Prerforms Test Case entity check before modifying a test case.
	 * 
	 * It checks : - the path is well formed (failure) - the test case has a name (failure) - the test case name has
	 * length between 0 and 255 - the project exists (failure) - the size of fields that are restricted in size
	 * (warning)
	 * 
	 * @param target
	 * @param testCase
	 * @return
	 */
	public LogTrain updateTestCaseChecks(TestCaseTarget target, TestCase testCase) {

		LogTrain logs = createTestCaseChecks(target, testCase);

		// 1 - name must be supplied
		String name = testCase.getName();
		if (StringUtils.isBlank(name)) {
			logs.addEntry(LogEntry.failure().forTarget(target)
					.withMessage(Messages.ERROR_FIELD_MANDATORY, TC_NAME.header).build());
		}

		// 2 - natures and types now
		logs.append(checkNatureAndTypeAndFixIfNeeded(target, testCase));

		return logs;
	}

	/**
	 * Performs Test Case entity check before creating a test case.
	 * 
	 * @param target
	 * @param testCase
	 * @return
	 */
	public LogTrain createTestCaseChecks(TestCaseTarget target, TestCase testCase) {
		String name = testCase.getName();
		LogTrain logs = new LogTrain();

		// 1 - path must be supplied and and well formed
		if (!target.isWellFormed()) {
			logs.addEntry(LogEntry.failure().forTarget(target)
					.withMessage(Messages.ERROR_MALFORMED_PATH, target.getPath()).build());
		}

		// 3 - the project actually exists
		if (target.isWellFormed()) {
			TargetStatus projectStatus = getModel().getProjectStatus(target.getProject());
			if (projectStatus.getStatus() != Existence.EXISTS) {
				logs.addEntry(LogEntry.failure().forTarget(target).withMessage(Messages.ERROR_PROJECT_NOT_EXIST)
						.build());
			}
		}

		// 4 - name has length between 0 and 255
		if (name != null && name.length() > TestCase.MAX_NAME_SIZE) {
			logs.addEntry(LogEntry.warning().forTarget(target).withMessage(Messages.ERROR_MAX_SIZE, TC_NAME.header)
					.withImpact(Messages.IMPACT_MAX_SIZE).build());
		}

		// 5 - reference, if exists, has length between 0 and 50
		String reference = testCase.getReference();
		if (!StringUtils.isBlank(reference) && reference.length() > TestCase.MAX_REF_SIZE) {
			logs.addEntry(LogEntry.warning().forTarget(target)
					.withMessage(Messages.ERROR_MAX_SIZE, TC_REFERENCE.header).withImpact(Messages.IMPACT_MAX_SIZE)
					.build());
		}

		// 6 - natures and types now
		logs.append(checkNatureAndTypeAndFixIfNeeded(target, testCase));

		return logs;
	}

	/**
	 * those checks are run for a test step for any type of operations.
	 * 
	 * It checks : - the path of the test case is well formed (failure) - the project exists (failure) - the format of
	 * the custom fields (lists, dates and checkbox) (warning)
	 * 
	 * 
	 * 
	 * @param target
	 * @param testStep
	 * @return
	 */
	LogTrain basicTestStepChecks(TestStepTarget target) {

		LogTrain logs = new LogTrain();

		TestCaseTarget testCase = target.getTestCase();

		// 1 - test case owner path must be supplied and and well formed
		if (!testCase.isWellFormed()) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MALFORMED_PATH,
					new String[] { testCase.getPath() }));
		}

		// 2 - the test case must exist
		TargetStatus tcStatus = getModel().getStatus(testCase);
		if (tcStatus.status == TO_BE_DELETED || tcStatus.status == NOT_EXISTS) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_NOT_FOUND));
		}

		// 3 - the project actually exists
		if (target.isWellFormed()) {
			TargetStatus projectStatus = getModel().getProjectStatus(target.getProject());
			if (projectStatus.getStatus() != Existence.EXISTS) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PROJECT_NOT_EXIST));
			}
		}

		return logs;

	}

	LogTrain basicTestStepChecks(TestStepTarget target, TestStep testStep) {

		// for now nothing much more to do with the TestStep
		return basicTestStepChecks(target);

	}

	LogTrain validateCallStep(TestStepTarget target, TestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfos, ImportMode mode) {

		LogTrain logs = new LogTrain();


		// 1 - the target must exist and be valid
		String errorMessage = checkTestCaseExists(calledTestCase);

		if (errorMessage != null){
			logMustExistAndBeValidCalledTest(target, mode, logs, errorMessage);
		}
		else{
			// 2 - there must be no cyclic calls
			if (getModel().wouldCreateCycle(target, calledTestCase)) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CYCLIC_STEP_CALLS,
						new Object[] { target.getTestCase().getPath(), calledTestCase.getPath() }));
			}

			// 3 - check a called dataset
			if (paramInfos.getParamMode() == ParameterAssignationMode.CALLED_DATASET) {
				String dsname = paramInfos.getCalledDatasetName();

				// 3.1 - if a dataset is specified, the name must not exceed the max limit
				if (dsname.length() > FacilityImplHelper.STD_TRUNCATE_SIZE) {
					logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE,
							new String[] { StepSheetColumn.TC_STEP_CALL_DATASET.name() }, Messages.IMPACT_MAX_SIZE,
							null));
				}

				// 3.2 - if a dataset is specified, it must be owned by the called test case
				DatasetTarget dsTarget = new DatasetTarget(calledTestCase, dsname);
				if (!getModel().doesDatasetExists(dsTarget)) {
					logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_DATASET_NOT_FOUND_ST,
							Messages.IMPACT_NO_CALL_DATASET));
				}
			}
		}

		return logs;

	}

	private String checkTestCaseExists(TestCaseTarget calledTestCase){

		TargetStatus calledStatus = getModel().getStatus(calledTestCase);

		String mustExistAndBeValidMessage = null;
		if (calledStatus.status == NOT_EXISTS || calledStatus.status == TO_BE_DELETED) {
			mustExistAndBeValidMessage = Messages.ERROR_CALLED_TC_NOT_FOUND;
		}
		else if (!calledTestCase.isWellFormed()) {
			mustExistAndBeValidMessage = Messages.ERROR_CALLED_STEP_WRONG_FORMAT;
		}

		return mustExistAndBeValidMessage;

	}

	private void logMustExistAndBeValidCalledTest(TestStepTarget target, ImportMode mode, LogTrain logs, String message) {
		switch (mode) {
		case CREATE:
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, message, Messages.IMPACT_CALL_AS_ACTION_STEP));
			break;
		case UPDATE: // do default
		default:
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, message));
			break;
		}
	}

	LogTrain basicParameterChecks(ParameterTarget target) {
		String[] fieldPathErrorArgs = new String[] { "TC_OWNER_PATH" }; // that variable is simple convenience for
		// logging
		return basicParameterChecks(target, fieldPathErrorArgs, Messages.ERROR_PARAMETER_OWNER_NOT_FOUND);
	}

	public LogTrain basicParameterValueChecks(ParameterTarget target) {

		String[] fieldPathErrorArgs = new String[] { "TC_PARAMETER_OWNER_PATH" }; // that variable is simple convenience
		// for
		// logging
		return basicParameterChecks(target, fieldPathErrorArgs, Messages.ERROR_DATASET_PARAM_OWNER_NOT_FOUND);

	}

	private LogTrain basicParameterChecks(ParameterTarget target, String[] fieldPathErrorArgs,
			String ownerNotFoundMessage) {
		LogTrain logs = new LogTrain();
		String[] fieldNameErrorArgs = new String[] { "TC_PARAM_NAME" }; // that variable is simple convenience for

		// logging
		TestCaseTarget testCase = target.getOwner();

		basicTestCaseTargetCheck(testCase, logs, fieldPathErrorArgs, ownerNotFoundMessage, target);

		basicParameterChecksValidateName(target, logs, fieldNameErrorArgs);

		return logs;
	}


	LogTrain basicDatasetCheck(DatasetTarget target) {

		LogTrain logs = new LogTrain();
		String[] fieldNameErrorArgs = new String[] { "TC_DATASET_NAME" }; // that variable is simple convenience for
		// logging

		TestCaseTarget testCase = target.getTestCase();

		basicTestCaseTargetCheck(testCase, logs,  new String[] { testCase.getPath() }, Messages.ERROR_TC_NOT_FOUND, target);

		// 4 - name has length between 1 and 255
		String name = target.getName();
		if (name != null && name.length() > 255) {
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, fieldNameErrorArgs,
					Messages.IMPACT_MAX_SIZE, null));
		}
		if (StringUtils.isBlank(name)) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_FIELD_MANDATORY, fieldNameErrorArgs));
		}

		return logs;

	}


	// ************************* private stuffs ****************************************


	private void basicTestCaseTargetCheck(TestCaseTarget testCase, LogTrain logs, String[] fieldPathErrorArgs,
			String tcNotFoundMessage, Target target) {
		// 1 - test case owner path must be supplied and and well formed
		if (!testCase.isWellFormed()) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MALFORMED_PATH, fieldPathErrorArgs));
		}

		// 2 - the test case must exist
		TargetStatus tcStatus = getModel().getStatus(testCase);
		if (tcStatus.status == TO_BE_DELETED || tcStatus.status == NOT_EXISTS) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, tcNotFoundMessage));
		}

		// 3 - the project actually exists
		if (testCase.isWellFormed()) {
			TargetStatus projectStatus = getModel().getProjectStatus(target.getProject());
			if (projectStatus.getStatus() != Existence.EXISTS) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PROJECT_NOT_EXIST));
			}
		}
	}


	private void basicParameterChecksValidateName(ParameterTarget target, LogTrain logs, String[] fieldNameErrorArgs) {
		String name = target.getName();
		if (StringUtils.isBlank(name)) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_FIELD_MANDATORY, fieldNameErrorArgs));
		} else {

			// 4 - name has length between 1 and 255
			if ( name.length() > 255) {
				logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, fieldNameErrorArgs,
						Messages.IMPACT_MAX_SIZE, null));
			}

			// 5 - name does not contain forbidden characters
			String regex = Parameter.NAME_REGEXP;
			name = name.trim();
			target.setName(name);
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(name);
			if (!StringUtils.isBlank(name) && !m.matches() && name.length() < 256) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE,
						Messages.ERROR_PARAMETER_CONTAINS_FORBIDDEN_CHARACTERS, fieldNameErrorArgs));
			}
		}
	}



	/*
	 * This method will check that, in case a nature and/or a type were supplied,
	 * this element is consistent with the set of natures/types available in the
	 * given project.
	 * 
	 */
	private LogTrain checkNatureAndTypeAndFixIfNeeded(TestCaseTarget target, TestCase testCase){

		LogTrain logs = new LogTrain();

		if (target.isWellFormed()){

			TargetStatus projectStatus = getModel().getProjectStatus(target.getProject());
			if (projectStatus.getStatus() == Existence.EXISTS) {

				// 2-1 nature, if specified, must be consistent with the natures of the target project
				if (! natureDefinedAndConsistent(projectStatus, testCase)){
					logs.addEntry(
							LogEntry.warning().forTarget(target)
							.withMessage(Messages.ERROR_INVALID_NATURE, target.getPath())
							.withImpact(Messages.IMPACT_DEFAULT_VALUE)
							.build()
							);
				}

				if (! typeDefinedAndConsistent(projectStatus, testCase)){
					logs.addEntry(
							LogEntry.warning().forTarget(target)
							.withMessage(Messages.ERROR_INVALID_TYPE, target.getPath())
							.withImpact(Messages.IMPACT_DEFAULT_VALUE)
							.build()
							);
				}
			}
		}

		return logs;
	}

	private boolean natureDefinedAndConsistent(TargetStatus projectStatus, TestCase testCase){
		boolean isConsistent = true;
		InfoListItem nature = testCase.getNature();

		if (nature == null){
			isConsistent = true;
		}
		else{
			isConsistent = getInfoListItemService().isNatureConsistent(projectStatus.getId(), nature.getCode());
		}

		return isConsistent;
	}


	private boolean typeDefinedAndConsistent(TargetStatus projectStatus, TestCase testCase){
		boolean isConsistent = true;
		InfoListItem type = testCase.getType();

		if (type == null){
			isConsistent = true;
		}
		else{
			isConsistent = getInfoListItemService().isTypeConsistent(projectStatus.getId(), type.getCode());
		}

		return isConsistent;
	}


}
