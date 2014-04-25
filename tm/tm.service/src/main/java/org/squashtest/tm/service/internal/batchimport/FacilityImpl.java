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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
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

	private Map<String, Long> cufIdByCode = new HashMap<String, Long>();

	private Collection<Long> modifiedTestCases = new HashSet<Long>();


	// ************************ public (and nice looking) code **************************************

	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain train = validator.createTestCase(target, testCase, cufValues);

		if (!train.hasCriticalErrors()) {
			train = createTCRoutine(train, target, testCase, cufValues);
		}

		return train;

	}

	private LogTrain createTCRoutine(LogTrain train, TestCaseTarget target, TestCase testCase, Map<String, String> cufValues){

		try {

			helper.fillNullWithDefaults(testCase);
			helper.truncate(testCase, cufValues);

			doCreateTestcase(target, testCase, cufValues);
			validator.getModel().setExists(target, testCase.getId());

			remember(target);

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[] { ex.getClass().getName() }));
			validator.getModel().setNotExists(target);
			LOGGER.error("Excel import : unexpected error while importing " + target + " : ", ex);
		}

		return train;
	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		TargetStatus status = validator.getModel().getStatus(target);

		LogTrain train = validator.updateTestCase(target, testCase, cufValues);

		if (!train.hasCriticalErrors()) {

			if (status.status == Existence.NOT_EXISTS) {

				train = createTCRoutine(train, target, testCase, cufValues);

			} else {
				try {

					helper.truncate(testCase, cufValues);
					doUpdateTestcase(target, testCase, cufValues);

					remember(target);
				}
				catch (Exception ex) {
					train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
							new Object[] { ex.getClass().getName() }));
					LOGGER.error("Excel import : unexpected error while updating " + target + " : ", ex);
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

			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error("Excel import : unexpected error while deleting " + target + " : ", ex);
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

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error("Excel import : unexpected error while creating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {

		LogTrain train = validator.addCallStep(target, testStep, calledTestCase);

		if (!train.hasCriticalErrors()) {
			try {
				doAddCallStep(target, testStep, calledTestCase);

				remember(target.getTestCase());
				validator.getModel().addCallStep(target, calledTestCase);

			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while creating step " + target + " : ", ex);
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
			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while updating step " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {

		LogTrain train = validator.updateCallStep(target, testStep, calledTestCase);

		if (!train.hasCriticalErrors()) {
			try {
				doUpdateCallStep(target, testStep, calledTestCase);
				validator.getModel().updateCallStepTarget(target, calledTestCase);
			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while updating step " + target + " : ", ex);
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

			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while deleting step " + target + " : ", ex);
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
				remember(target.getOwner());
			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while adding parameter " + target + " : ", ex);
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
			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while updating parameter " + target + " : ", ex);
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
			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));

				LOGGER.error("Excel import : unexpected error while deleting parameter " + target + " : ", ex);
			}
		}

		return train;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value) {

		LogTrain train = validator.failsafeUpdateParameterValue(dataset, param, value);

		if (!train.hasCriticalErrors()) {
			try {
				doFailsafeUpdateParameterValue(dataset, param, value);

				validator.getModel().addDataset(dataset);
				remember(dataset.getTestCase());
				remember(param.getOwner());

			}
			catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while setting parameter " + param + " in dataset "
						+ dataset + " : ", ex);
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
				remember(dataset.getTestCase());

			} catch (Exception ex) {
				train.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[] { ex.getClass().getName() }));
				LOGGER.error("Excel import : unexpected error while deleting dataset " + dataset + " in dataset "
						+ dataset + " : ", ex);
			}
		}

		return train;
	}

	/**
	 * for all other stuffs that need to be done afterward
	 * 
	 */
	public void postprocess() {

		for (Long id : modifiedTestCases) {
			datasetService.updateDatasetParameters(id);
		}

	}

	// ************************* private (and hairy) code *********************************

	// because this time we're not toying around man, this is the real thing
	private void doCreateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues)
			throws Exception {

		Map<Long, String> acceptableCufs = toAcceptableCufs(cufValues);

		// backup the audit log
		AuditableSupport metadata = helper.saveAuditMetadata((AuditableMixin) testCase);

		// case 1 : this test case lies at the root of the project
		if (target.isRootTestCase()) {
			Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getId(); // never null because the checks
			// ensured that the project exists
			Collection<String> siblingNames = navigationService.findNamesInLibraryStartingWith(libraryId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToLibrary(libraryId, testCase, acceptableCufs);
		}
		// case 2 : this test case exists within a folder
		else {
			Long folderId = navigationService.mkdirs(target.getFolder());
			Collection<String> siblingNames = navigationService.findNamesInFolderStartingWith(folderId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToFolder(folderId, testCase, acceptableCufs);
		}

		// restore the audit log
		helper.restoreMetadata((AuditableMixin) testCase, metadata);
	}

	private void renameIfNeeded(TestCase testCase, Collection<String> siblingNames) {
		String newName = LibraryUtils.generateNonClashingName(testCase.getName(), siblingNames, TestCase.MAX_NAME_SIZE);
		if (!newName.equals(testCase.getName())) {
			testCase.setName(newName);
		}
	}

	private void doUpdateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues)
			throws Exception {

		TestCase orig = validator.getModel().get(target);
		Long origId = orig.getId();

		// backup the audit log
		AuditableSupport metadata = helper.saveAuditMetadata((AuditableMixin) testCase);

		// update the test case core attributes

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
		if (newImportanceAuto != null && orig.isImportanceAuto() != newImportanceAuto) {
			testcaseModificationService.changeImportanceAuto(origId, newImportanceAuto);
		}

		// the custom field values now

		List<CustomFieldValue> cufs = cufvalueService.findAllCustomFieldValues(orig);
		for (CustomFieldValue v : cufs) {
			String code = v.getCustomField().getCode();
			String newValue = cufValues.get(code);
			if (!StringUtils.isBlank(newValue)) {
				v.setValue(newValue);
			}
		}

		// restore the audit log
		helper.restoreMetadata((AuditableMixin) testCase, metadata);

	}

	private void doDeleteTestCase(TestCaseTarget target) throws Exception {
		TestCase tc = validator.getModel().get(target);
		navigationService.deleteNodes(Arrays.asList(tc.getId()));
	}

	private void doAddActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues)
			throws Exception {

		Map<Long, String> acceptableCufs = toAcceptableCufs(cufValues);

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		testcaseModificationService.addActionTestStep(tc.getId(), testStep, acceptableCufs);

		// move it if the index was specified
		Integer index = target.getIndex();
		if (index != null && index >= 0 && index < tc.getSteps().size()) {
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Arrays.asList(testStep.getId()));
		}

	}

	private void doAddCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {

		// add the step
		TestCase tc = validator.getModel().get(target.getTestCase());
		TestCase called = validator.getModel().get(calledTestCase);

		callstepService.addCallTestStep(tc.getId(), called.getId());
		CallTestStep created = (CallTestStep) tc.getSteps().get(tc.getSteps().size() - 1);

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

		List<CustomFieldValue> cufs = cufvalueService.findAllCustomFieldValues(orig);
		for (CustomFieldValue v : cufs) {
			String code = v.getCustomField().getCode();
			String newValue = cufValues.get(code);
			if (!StringUtils.isBlank(newValue)) {
				v.setValue(newValue);
			}
		}

	}

	private void doUpdateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {

		// update the step
		TestStep actualStep = validator.getModel().getStep(target);
		TestCase newCalled = validator.getModel().get(calledTestCase);
		callstepService.checkForCyclicStepCallBeforePaste(newCalled.getId(), Arrays.asList(actualStep.getId()));
		((CallTestStep) actualStep).setCalledTestCase(newCalled);

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

			findParameter(target).setDescription(param.getDescription());
		}

	}

	private void doDeleteParameter(ParameterTarget target) {
		Long testcaseId = validator.getModel().getId(target.getOwner());
		List<Parameter> allparams = parameterService.findAllforTestCase(testcaseId);

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
		String trValue = helper.truncate(value, 255);
		dpv.setParamValue(trValue);
	}

	private void doDeleteDataset(DatasetTarget dataset) {
		Dataset ds = findDataset(dataset);
		datasetService.remove(ds);
	}

	// ******************************** support methods ***********************

	private Parameter findParameter(ParameterTarget param) {
		Long testcaseId = validator.getModel().getId(param.getOwner());

		Parameter found = paramDao.findParameterByNameAndTestCase(param.getName(), testcaseId);

		if (found != null) {
			return found;
		} else {
			throw new NoSuchElementException("parameter " + param + " could not be found");
		}
	}

	private Dataset findDataset(DatasetTarget dataset) {
		Long tcid = validator.getModel().getId(dataset.getTestCase());

		Dataset found = datasetDao.findDatasetByTestCaseAndByName(tcid, dataset.getName());

		if (found == null) {
			return found;
		} else {
			Dataset newds = new Dataset();
			newds.setName(dataset.getName());
			helper.fillNullWithDefaults(newds);
			helper.truncate(newds);
			datasetService.persist(newds, tcid);
			return newds;
		}
	}

	private DatasetParamValue findParamValue(DatasetTarget dataset, ParameterTarget param) {

		Dataset dbDs = findDataset(dataset);
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
	 * also populates the cache (cufIdByCode)
	 */
	private Map<Long, String> toAcceptableCufs(Map<String, String> origCufs) {

		Map<Long, String> result = new HashMap<Long, String>(origCufs.size());

		for (Entry<String, String> origCuf : origCufs.entrySet()) {
			String cufCode = origCuf.getKey();

			if (!cufIdByCode.containsKey(cufCode)) {

				CustomField customField = cufDao.findByCode(cufCode);

				// that bit of code checks that if the custom field doesn't exist, the hashmap entry contains
				// a dummy value for this code.
				Long id = null;
				if (customField != null) {
					id = customField.getId();
				}

				cufIdByCode.put(cufCode, id);
			}

			// now add to our map the id of the custom field, except if null : the custom field
			// does not exist and therefore wont be included.
			Long cufId = cufIdByCode.get(cufCode);
			if (cufId != null) {
				result.put(cufId, origCuf.getValue());
			}
		}

		return result;

	}

	private void remember(TestCaseTarget target) {
		TestCase tc = validator.getModel().get(target);
		if (tc.getId() != null) {
			modifiedTestCases.add(tc.getId());
		}
	}

}
