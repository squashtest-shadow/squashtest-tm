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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;

/**
 * 
 * Implementation of batch import methods that will actually update the database.
 * 
 */
@Component
@Scope("prototype")
public class FacilityImpl implements Facility {

	private static final String UNEXPECTED_ERROR_WHILE_IMPORTING = "unexpected error while importing ";

	private static final String EXCEL_ERR_PREFIX = "Excel import : ";

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class);

	@Inject
	private ValidationFacility validator;

	@Inject
	private TestCaseLibraryFinderService finderService;

	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseModificationService testcaseModificationService;

	@Inject
	private TestStepModificationService stepModificationService;

	@Inject
	private PrivateCustomFieldValueService cufvalueService;

	@Inject
	private CallStepManagerService callstepService;

	@Inject
	private ParameterModificationService parameterService;

	@Inject
	private DatasetModificationService datasetService;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private DatasetParamValueDao paramvalueDao;

	@Inject
	private ParameterDao paramDao;

	@Inject
	private CustomFieldDao cufDao;

	private FacilityImplHelper helper = new FacilityImplHelper();

	private Map<String, CustomFieldInfos> cufInfosCache = new HashMap<String, CustomFieldInfos>();


	// ************************ public (and nice looking) code **************************************

	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain train = validator.createTestCase(target, testCase, cufValues);

		if (!train.hasCriticalErrors()) {
			train = createTCRoutine(train, target, testCase, cufValues);
		}

		return train;

	}

	private LogTrain updateTCRoutine(LogTrain train, TestCaseTarget target, TestCase testCase,
			Map<String, String> cufValues) {

		try {

			helper.fillNullWithDefaults(testCase);
			helper.truncate(testCase, cufValues);

			doCreateTestcase(target, testCase, cufValues);
			validator.getModel().setExists(target, testCase.getId());

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Test Case \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[] { ex.getClass().getName() }));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}

		return train;
	}

	private LogTrain createTCRoutine(LogTrain train, TestCaseTarget target, TestCase testCase,
			Map<String, String> cufValues) {

		// when creating, tc name might be random crap or blank
		testCase.setName(target.getName());

		return updateTCRoutine(train, target, testCase, cufValues);
	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		TargetStatus status = validator.getModel().getStatus(target);

		LogTrain train = validator.updateTestCase(target, testCase, cufValues);

		if (!train.hasCriticalErrors()) {

			if (status.status == Existence.NOT_EXISTS) {

				train = updateTCRoutine(train, target, testCase, cufValues);

			} else {
				try {

					helper.truncate(testCase, cufValues);
					doUpdateTestcase(target, testCase, cufValues);

					LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Test Case \t'" + target + "'");

				} catch (Exception ex) {
					train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
							new Object[] { ex.getClass().getName() }));
					LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating " + target + " : ", ex);
				}

			}

		}

		return train;
	}

	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {

		LogTrain train = validator.deleteTestCase(target);

		if (!train.hasCriticalErrors()) {
			try {

				doDeleteTestCase(target);
				validator.getModel().setDeleted(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Test Case \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain train = validator.addActionStep(target, testStep, cufValues);

		if (!train.hasCriticalErrors()) {
			try {
				helper.fillNullWithDefaults(testStep);
				helper.truncate(testStep, cufValues);

				doAddActionStep(target, testStep, cufValues);
				validator.getModel().addActionStep(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Action Step \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain train = validator.addCallStep(target, testStep, calledTestCase, paramInfo, actionStepBackup);

		if (!train.hasCriticalErrors()) {
			String mustImportCallAsActionStepErrorI18n = FacilityUtils.mustImportCallAsActionStep(train);
			try {
				if (mustImportCallAsActionStepErrorI18n != null) {
					ActionTestStep actionTestStep = actionStepBackup;
					doAddActionStep(target, actionTestStep, new HashMap<String, String>(0));
					validator.getModel().addActionStep(target);
				} else {

					doAddCallStep(target, calledTestCase, paramInfo);

					validator.getModel().addCallStep(target, calledTestCase, paramInfo);

					LOGGER.debug(EXCEL_ERR_PREFIX + "Created Call Step \t'" + target + "' -> '" + calledTestCase + "'");
				}
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain train = validator.updateActionStep(target, testStep, cufValues);

		if (!train.hasCriticalErrors()) {
			try {
				helper.truncate(testStep, cufValues);
				doUpdateActionStep(target, testStep, cufValues);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Action Step \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain train = validator.updateCallStep(target, testStep, calledTestCase, paramInfo, actionStepBackup);

		if (!train.hasCriticalErrors()) {
			try {
				doUpdateCallStep(target, calledTestCase, paramInfo);
				validator.getModel().updateCallStepTarget(target, calledTestCase, paramInfo);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Call Step \t'" + target + "' -> '" + calledTestCase + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {

		LogTrain train = validator.deleteTestStep(target);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteTestStep(target);
				validator.getModel().remove(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Step \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {

		LogTrain train = validator.createParameter(target, param);

		if (!train.hasCriticalErrors()) {
			try {
				doCreateParameter(target, param);
				validator.getModel().addParameter(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while adding parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {

		LogTrain train = validator.updateParameter(target, param);

		if (!train.hasCriticalErrors()) {
			try {
				doUpdateParameter(target, param);
				validator.getModel().addParameter(target); // create the parameter if didn't exist already.
				// Double-insertion proof.

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteParameter(ParameterTarget target) {

		LogTrain train = validator.deleteParameter(target);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteParameter(target);
				validator.getModel().removeParameter(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Parameter \t'" + target + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value,
			boolean isUpdate) {

		LogTrain train = validator.failsafeUpdateParameterValue(dataset, param, value, isUpdate);

		if (!train.hasCriticalErrors()) {
			try {
				doFailsafeUpdateParameterValue(dataset, param, value);

				validator.getModel().addDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Param Value for param \t'" + param + "' in dataset '"
						+ dataset + "'");
			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while setting parameter " + param + " in dataset "
						+ dataset + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain createDataset(DatasetTarget dataset) {

		LogTrain train = validator.createDataset(dataset);

		if (!train.hasCriticalErrors()) {
			try {
				findOrCreateDataset(dataset);

				validator.getModel().addDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Created Dataset '" + dataset + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while creating dataset " + dataset + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {

		LogTrain train = validator.deleteDataset(dataset);

		if (!train.hasCriticalErrors()) {
			try {
				doDeleteDataset(dataset);

				validator.getModel().removeDataset(dataset);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Dataset '" + dataset + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting dataset " + dataset + " : ", ex);
			}
		}

		return train;
	}

	/**
	 * for all other stuffs that need to be done afterward
	 * 
	 */
	public void postprocess() {
		// NOOP yet
	}

	// ************************* private (and hairy) code *********************************

	// because this time we're not toying around man, this is the real thing
	private void doCreateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(cufValues);

		// case 1 : this test case lies at the root of the project
		if (target.isRootTestCase()) {
			// libraryId is never null because the checks ensured that the project exists
			Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getTestCaseLibraryId();

			Collection<String> siblingNames = navigationService.findNamesInLibraryStartingWith(libraryId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToLibrary(libraryId, testCase, acceptableCufs, target.getOrder());
		}
		// case 2 : this test case exists within a folder
		else {
			Long folderId = navigationService.mkdirs(target.getFolder());
			Collection<String> siblingNames = navigationService.findNamesInFolderStartingWith(folderId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToFolder(folderId, testCase, acceptableCufs, target.getOrder());
		}

	}

	private void renameIfNeeded(TestCase testCase, Collection<String> siblingNames) {
		String newName = LibraryUtils.generateNonClashingName(testCase.getName(), siblingNames, TestCase.MAX_NAME_SIZE);
		if (!newName.equals(testCase.getName())) {
			testCase.setName(newName);
		}
	}

	private void doUpdateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		TestCase orig = validator.getModel().get(target);
		Long origId = orig.getId();

		// update the test case core attributes

		doUpdateTestCaseCoreAttributes(testCase, orig);

		// the custom field values now

		doUpdateCustomFields(cufValues, orig);

		// move the test case if its index says it has to move
		Integer order = target.getOrder();
		if (order != null && order > -1 && order < navigationService.countSiblingsOfNode(origId)) {
			if (target.isRootTestCase()) {
				Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getTestCaseLibraryId();
				navigationService.moveNodesToLibrary(libraryId, new Long[] { origId }, order);
			} else {
				Long folderId = navigationService.findNodeIdByPath(target.getFolder());
				navigationService.moveNodesToFolder(folderId, new Long[] { origId }, order);
			}
		}

	}

	private void doDeleteTestCase(TestCaseTarget target) {
		TestCase tc = validator.getModel().get(target);
		navigationService.deleteNodes(Arrays.asList(tc.getId()));
	}

	private void doAddActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(cufValues);

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		testcaseModificationService.addActionTestStep(tc.getId(), testStep, acceptableCufs);

		// move it if the index was specified
		Integer index = target.getIndex();
		if (index != null && index >= 0 && index < tc.getSteps().size()) {
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Arrays.asList(testStep.getId()));
		}

	}

	private void doAddCallStep(TestStepTarget target, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo) {

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		TestCase called = validator.getModel().get(calledTestCase);

		callstepService.addCallTestStep(tc.getId(), called.getId());
		CallTestStep created = (CallTestStep) tc.getSteps().get(tc.getSteps().size() - 1);

		// handle the parameter assignation
		changeParameterAssignation(created.getId(), calledTestCase, paramInfo);

		// change position if possible and required
		Integer index = target.getIndex();
		if (index != null && index >= 0 && index < tc.getSteps().size()) {
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Arrays.asList(created.getId()));
		}

	}

	private void doUpdateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		// update the step
		ActionTestStep orig = (ActionTestStep) validator.getModel().getStep(target);

		String newAction = testStep.getAction();
		if (!StringUtils.isBlank(newAction) && !orig.getAction().equals(newAction)) {
			orig.setAction(newAction);
		}

		String newResult = testStep.getExpectedResult();
		if (!StringUtils.isBlank(newResult) && !orig.getExpectedResult().equals(newResult)) {
			orig.setExpectedResult(newResult);
		}

		// the custom field values now
		doUpdateCustomFields(cufValues, orig);

	}

	private void doUpdateCallStep(TestStepTarget target, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo) {

		// update the step
		TestStep actualStep = validator.getModel().getStep(target);
		TestCase newCalled = validator.getModel().get(calledTestCase);
		callstepService.checkForCyclicStepCallBeforePaste(actualStep.getTestCase().getId(), newCalled.getId());
		((CallTestStep) actualStep).setCalledTestCase(newCalled);

		// update the parameter assignation
		changeParameterAssignation(actualStep.getId(), calledTestCase, paramInfo);

	}

	private void doDeleteTestStep(TestStepTarget target) {
		TestCase tc = validator.getModel().get(target.getTestCase());
		testcaseModificationService.removeStepFromTestCaseByIndex(tc.getId(), target.getIndex());
	}

	private void doCreateParameter(ParameterTarget target, Parameter param) {

		// according to the spec this is exactly the same thing
		doUpdateParameter(target, param);
	}

	private void doUpdateParameter(ParameterTarget target, Parameter param) {
		if (!validator.getModel().doesParameterExists(target)) {
			Long testcaseId = validator.getModel().getId(target.getOwner());
			helper.fillNullWithDefaults(param);
			helper.truncate(param);
			parameterService.addNewParameterToTestCase(param, testcaseId);
		} else {
			String description = param.getDescription();
			if (description != null) {
				findParameter(target).setDescription(description);
			}
		}

	}

	private void doDeleteParameter(ParameterTarget target) {
		Long testcaseId = validator.getModel().getId(target.getOwner());
		List<Parameter> allparams = parameterService.findAllParameters(testcaseId);

		Parameter param = null;
		for (Parameter p : allparams) {
			if (p.getName().equals(target.getName())) {
				param = p;
				break;
			}
		}

		parameterService.remove(param);
	}

	private void doFailsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value) {
		DatasetParamValue dpv = findParamValue(dataset, param);
		String trValue = helper.truncate(value);
		dpv.setParamValue(trValue);
	}

	private void doDeleteDataset(DatasetTarget dataset) {
		Dataset ds = findOrCreateDataset(dataset);
		TestCase tc = ds.getTestCase();
		tc.removeDataset(ds);
		datasetService.remove(ds);
	}

	// ******************************** support methods ***********************

	private void doUpdateTestCaseCoreAttributes(TestCase testCase, TestCase orig) {

		Long origId = orig.getId();
		String newName = testCase.getName();

		if (!StringUtils.isBlank(newName) && !orig.getName().equals(newName)) {
			testcaseModificationService.rename(origId, newName);
		}

		String newRef = testCase.getReference();
		if (!StringUtils.isBlank(newRef) && !orig.getReference().equals(newRef)) {
			testcaseModificationService.changeReference(origId, newRef);
		}

		String newDesc = testCase.getDescription();
		if (!StringUtils.isBlank(newDesc) && !orig.getDescription().equals(newDesc)) {
			testcaseModificationService.changeDescription(origId, newDesc);
		}

		String newPrereq = testCase.getPrerequisite();
		if (!StringUtils.isBlank(newPrereq) && !orig.getPrerequisite().equals(newPrereq)) {
			testcaseModificationService.changePrerequisite(origId, newPrereq);
		}

		TestCaseImportance newImp = testCase.getImportance();
		if (newImp != null && !orig.getImportance().equals(newImp)) {
			testcaseModificationService.changeImportance(origId, newImp);
		}

		TestCaseNature newNat = testCase.getNature();
		if (newNat != null && !orig.getNature().equals(newNat)) {
			testcaseModificationService.changeNature(origId, newNat);
		}

		TestCaseType newType = testCase.getType();
		if (newType != null && !orig.getType().equals(newType)) {
			testcaseModificationService.changeType(origId, newType);
		}

		TestCaseStatus newStatus = testCase.getStatus();
		if (newStatus != null && !orig.getStatus().equals(newStatus)) {
			testcaseModificationService.changeStatus(origId, newStatus);
		}

		Boolean newImportanceAuto = testCase.isImportanceAuto();
		if (newImportanceAuto != null && orig.isImportanceAuto().equals(newImportanceAuto)) {
			testcaseModificationService.changeImportanceAuto(origId, newImportanceAuto);
		}
	}

	private void doUpdateCustomFields(Map<String, String> cufValues, BoundEntity bindableEntity) {

		List<CustomFieldValue> cufs = cufvalueService.findAllCustomFieldValues(bindableEntity);
		Set<String> codeSet = cufValues.keySet();
		for (CustomFieldValue v : cufs) {
			String code = v.getCustomField().getCode();
			String newValue = cufValues.get(code);
			if (codeSet.contains(code)) {
				v.setValue(newValue);
			}
		}

	}

	private Parameter findParameter(ParameterTarget param) {
		Long testcaseId = validator.getModel().getId(param.getOwner());

		Parameter found = paramDao.findOwnParameterByNameAndTestCase(param.getName(), testcaseId);

		if (found != null) {
			return found;
		} else {
			throw new NoSuchElementException("parameter " + param + " could not be found");
		}
	}

	/**
	 * 
	 * @param dataset
	 * @return the found Dataset or a new one (non null value)
	 */
	private Dataset findOrCreateDataset(DatasetTarget dataset) {
		Long tcid = validator.getModel().getId(dataset.getTestCase());

		String truncated = helper.truncate(dataset.getName());
		Dataset found = datasetDao.findDatasetByTestCaseAndByName(tcid, truncated);

		if (found != null) {
			return found;
		} else {
			Dataset newds = new Dataset();
			newds.setName(dataset.getName());
			helper.fillNullWithDefaults(newds);
			helper.truncate(newds);
			datasetService.persist(newds, tcid);

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Dataset \t'" + dataset + "'");

			return newds;
		}
	}

	/**
	 * 
	 * @param testStep
	 * @param tc
	 * @param paramInfo
	 */
	private void changeParameterAssignation(Long stepId, TestCaseTarget tc, CallStepParamsInfo paramInfo) {
		Long dsId = null;
		ParameterAssignationMode mode = paramInfo.getParamMode();

		if (paramInfo.getParamMode() == ParameterAssignationMode.CALLED_DATASET) {

			Long tcid = validator.getModel().getId(tc);
			String dsname = helper.truncate(paramInfo.getCalledDatasetName());
			Dataset ds = datasetDao.findDatasetByTestCaseAndByName(tcid, dsname);

			// if the dataset exists we can actually bind the step to it.
			// otherwise we fallback to the default mode (nothing).
			// This later case has been dutifully reported by the
			// validator facility of course.
			if (ds != null) {
				dsId = ds.getId();
			} else {
				mode = ParameterAssignationMode.NOTHING;
			}

		}
		callstepService.setParameterAssignationMode(stepId, mode, dsId);
	}

	private DatasetParamValue findParamValue(DatasetTarget dataset, ParameterTarget param) {

		Dataset dbDs = findOrCreateDataset(dataset);
		Parameter dsParam = findParameter(param);

		for (DatasetParamValue dpv : dbDs.getParameterValues()) {
			if (dpv.getParameter().equals(dsParam)) {
				return dpv;
			}
		}

		// else we have to create it. Note that the services do not provide any facility for that
		// so we have to do it from scratch here. Tsss, lazy conception again.
		DatasetParamValue dpv = new DatasetParamValue(dsParam, dbDs);
		paramvalueDao.persist(dpv);
		dbDs.addParameterValue(dpv);

		return dpv;
	}

	/**
	 * because the service identifies cufs by their id, not their code<br/>
	 * also populates the cache (cufIdByCode), and transform the
	 * input data in a single string or a collection of string depending
	 * on the type of the custom field (Tags on non-tags).
	 */
	private Map<Long, RawValue> toAcceptableCufs(Map<String, String> origCufs) {

		Map<Long, RawValue> result = new HashMap<Long, RawValue>(origCufs.size());

		for (Entry<String, String> origCuf : origCufs.entrySet()) {
			String cufCode = origCuf.getKey();

			if (!cufInfosCache.containsKey(cufCode)) {

				CustomField customField = cufDao.findByCode(cufCode);

				// that bit of code checks that if the custom field doesn't exist, the hashmap entry contains
				// a dummy value for this code.
				CustomFieldInfos infos = null;
				if (customField != null) {
					Long id = customField.getId();
					InputType type = customField.getInputType();
					infos = new CustomFieldInfos(id, type);
				}

				cufInfosCache.put(cufCode, infos);
			}

			// now add to our map the id of the custom field, except if null : the custom field
			// does not exist and therefore wont be included.
			CustomFieldInfos infos = cufInfosCache.get(cufCode);
			if (infos != null) {
				switch(infos.getType()){
				case TAG :
					List<String> values = Arrays.asList(origCuf.getValue().split("\\|"));
					result.put(infos.getId(), new RawValue(values));
					break;
				default :
					result.put(infos.getId(), new RawValue(origCuf.getValue()));
					break;
				}
			}
		}

		return result;

	}


	private static final class CustomFieldInfos{
		private Long id;
		private InputType type;

		public Long getId() {
			return id;
		}
		public InputType getType() {
			return type;
		}
		public CustomFieldInfos(Long id, InputType type) {
			super();
			this.id = id;
			this.type = type;
		}

	}

}
