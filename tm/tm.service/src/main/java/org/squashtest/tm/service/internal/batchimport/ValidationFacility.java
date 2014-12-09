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

import static org.squashtest.tm.service.internal.batchimport.Model.Existence.EXISTS;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.TO_BE_CREATED;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.StepType;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

/**
 * 
 * This implementation solely focuses on validating data. It doesn't perform any operation against the database, nor
 * modifies the model : it justs uses the current data available.
 * 
 */
@Component
@Scope("prototype")
public class ValidationFacility implements Facility, ValidationFacilitySubservicesProvider {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String PERM_CREATE = "CREATE";
	private static final String PERM_WRITE = "WRITE";
	private static final String PERM_DELETE = "DELETE";
	private static final String PERM_READ = "READ";
	private static final String LIBRARY_CLASSNAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private InfoListItemFinderService infoListItemService;

	@Inject
	private Model model;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private UserDao userDao;

	private EntityValidator entityValidator = new EntityValidator(this);
	private CustomFieldValidator cufValidator = new CustomFieldValidator();

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public InfoListItemFinderService getInfoListItemService(){
		return infoListItemService;
	}

	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain logs;
		String path = target.getPath();
		String name = testCase.getName();
		TargetStatus status = model.getStatus(target);

		// 1 - basic verifications
		logs = entityValidator.createTestCaseChecks(target, testCase);

		// 2 - custom fields (create)
		logs.append(cufValidator.checkCreateCustomFields(target, cufValues, model.getTestCaseCufs(target)));


		// 3 - other checks
		// 3-1 : names clash
		if (status.getStatus() != Existence.NOT_EXISTS) {
			logs.addEntry(LogEntry.warning().forTarget(target)
					.withMessage(Messages.ERROR_TC_ALREADY_EXISTS, target.getPath())
					.withImpact(Messages.IMPACT_TC_WITH_SUFFIX).build());
		}

		// 3-2 : permissions.
		LogEntry hasntPermission = checkPermissionOnProject(PERM_CREATE, target, target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 3-3 : name and path must be consistent, only if the name is not empty
		if (! StringUtils.isBlank(name) && !PathUtils.arePathsAndNameConsistents(path, name)) {
			logs.addEntry(LogEntry.warning().forTarget(target)
					.withMessage(Messages.ERROR_INCONSISTENT_PATH_AND_NAME, path, name == null ? "" : name).build());
		}


		// 3-4 : fix test case metadatas
		List<LogEntry> logEntries = fixMetadatas(target, (AuditableMixin) testCase, ImportMode.CREATE);
		logs.addEntries(logEntries);
		return logs;

	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {
		LogTrain logs = new LogTrain();
		String name = testCase.getName();

		TargetStatus status = model.getStatus(target);

		// if the test case doesn't exist
		if (status.getStatus() == Existence.NOT_EXISTS) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_NOT_FOUND));
		} else {

			// 1 - basic verifications
			logs.append(entityValidator.updateTestCaseChecks(target, testCase));

			// 2 - custom fields (create)
			logs.append(cufValidator.checkUpdateCustomFields(target, cufValues, model.getTestCaseCufs(target)));

			// 3 - other checks
			// 3-1 : check if the test case is renamed and would induce a potential name clash.
			// arePathsAndNameConsistent() will tell us if the test case is renamed
			checkPathForUpdate(target, name, logs);

			// 3-2 : permissions. note about the following 'if' : the case where the project doesn't exist (and thus has
			// no id) is already covered in the basic checks.
			LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target, target);
			if (hasntPermission != null) {
				logs.addEntry(hasntPermission);
			}
			// 3-3 : check audit datas
			// backup the audit log
			List<LogEntry> logEntries = fixMetadatas(target, (AuditableMixin) testCase, ImportMode.UPDATE);
			logs.addEntries(logEntries);

		}

		return logs;

	}

	private void checkPathForUpdate(TestCaseTarget target, String name, LogTrain logs) {
		if (StringUtils.isBlank(name)) {
			// no name means no rename means we're good -> bail out
			return;
		}

		String path = target.getPath();

		if (!PathUtils.arePathsAndNameConsistents(path, name)) {

			String newPath = PathUtils.rename(path, name);
			TestCaseTarget newTarget = new TestCaseTarget(newPath);
			TargetStatus newStatus = model.getStatus(newTarget);
			if (newStatus.status != Existence.NOT_EXISTS) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_CANT_RENAME,
						new String[] { path, newPath }));
			}
		}
	}

	/**
	 * Will replace {@code mixin.createdBy} and {@code mixin.createdOn} if the values are invalid :
	 * <ul>
	 * <li>{@code mixin.createdBy} will be replaced by the current user's login</li>
	 * <li>{@code mixin.createdOn} will be replaced by the import date.</li>
	 * </ul>
	 * 
	 * @param target
	 * 
	 * @param testCase
	 * @param create
	 * @return a list of logEntries
	 */
	private List<LogEntry> fixMetadatas(TestCaseTarget target, AuditableMixin testCase, ImportMode importMode) {
		// init vars
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		String login = testCase.getCreatedBy();
		boolean fixUser = false;
		if (StringUtils.isBlank(login)) {
			// no value for created by
			fixUser = true;
		} else {
			User user = userDao.findUserByLogin(login);
			if (user == null || !user.getActive()) {
				// user not found or not active
				String impactMessage = null;
				switch (importMode) {
				case CREATE:
					impactMessage = Messages.IMPACT_USE_CURRENT_LOGIN;
					break;
				case UPDATE:
					impactMessage = Messages.IMPACT_NO_CHANGE;
					break;
				default:
					impactMessage = Messages.IMPACT_NO_CHANGE;
					break;
				}
				LogEntry logEntry = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_TC_USER_NOT_FOUND,
						impactMessage);
				logEntries.add(logEntry);
				fixUser = true;
			}
		}
		if (fixUser) {
			testCase.setCreatedBy(userAccountService.findCurrentUser().getLogin());
		}

		if (testCase.getCreatedOn() == null) {
			testCase.setCreatedOn(new Date());
		}
		return logEntries;
	}

	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {

		LogTrain logs = new LogTrain();

		TargetStatus status = model.getStatus(target);

		// 1 - does the target exist
		if (status.getStatus() == Existence.NOT_EXISTS) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_NOT_FOUND));
		}

		// 2 - can the user actually do it ?
		LogEntry hasntPermission = checkPermissionOnProject(PERM_DELETE, target, target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 3 - is the test case called by another test case ?
		if (model.isCalled(target)) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_REMOVE_CALLED_TC));
		}

		return logs;
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target, testStep);

		// 2 - custom fields (create)
		logs.append(cufValidator.checkCreateCustomFields(target, cufValues, model.getTestStepCufs(target)));

		// 3 - the user must be approved
		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase(), target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 4 - check the index
		TestCaseTarget testCase = target.getTestCase();
		TargetStatus tcStatus = getModel().getStatus(testCase);
		if (tcStatus.status == TO_BE_CREATED || tcStatus.status == EXISTS) {
			LogEntry indexCheckLog = checkStepIndex(ImportMode.CREATE, target, ImportStatus.WARNING,
					Messages.IMPACT_STEP_CREATED_LAST);
			if (indexCheckLog != null) {
				logs.addEntry(indexCheckLog);
			}
		}

		return logs;

	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfo, ActionTestStep actionStepBackup) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target, testStep);

		// 2 - call step specific checks
		logs.append(entityValidator.validateCallStep(target, testStep, calledTestCase, paramInfo, ImportMode.CREATE));

		// 3 - cufs : call steps have no cufs -> skip

		// 4.1 - the user must be approved on the source test case
		LogEntry hasntOwnerPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase(), target);
		if (hasntOwnerPermission != null) {
			logs.addEntry(hasntOwnerPermission);
		}

		// 4.2 - the user must be approved on the target test case
		LogEntry hasntCallPermission = checkPermissionOnProject(PERM_READ, calledTestCase, target);
		if (hasntCallPermission != null) {
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_CALL_NOT_READABLE, Messages.IMPACT_CALL_AS_ACTION_STEP));
		}

		// 5 - check the index
		LogEntry indexCheckLog = checkStepIndex(ImportMode.CREATE, target, ImportStatus.WARNING,
				Messages.IMPACT_STEP_CREATED_LAST);
		if (indexCheckLog != null) {
			logs.addEntry(indexCheckLog);
		}

		return logs;
	}

	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);

		// 2 - custom fields (create)
		logs.append(cufValidator.checkUpdateCustomFields(target, cufValues, model.getTestStepCufs(target)));

		// 3 - the user must be approved
		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase(), target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 4 - the step must exist
		boolean exists = model.stepExists(target);
		if (!exists) {
			if (target.getIndex()== null){
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_STEPINDEX_EMPTY));
			} else if (target.getIndex() < 0){
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_STEPINDEX_NEGATIVE));
			} else {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_STEP_NOT_EXISTS));
			}
		} else {
			// 5 - the step must be actually an action step
			StepType type = model.getType(target);
			if (type != StepType.ACTION) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NOT_AN_ACTIONSTEP));
			}
		}

		return logs;
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfos, ActionTestStep actionStepBackup) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);

		// 2 - call step specific checks
		logs.append(entityValidator.validateCallStep(target, testStep, calledTestCase, paramInfos, ImportMode.UPDATE));

		// 3 - cufs : call steps have no cufs -> skip

		// 4.1 - the user must be approved on the source test case
		LogEntry hasntOwnerPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase(), target);
		if (hasntOwnerPermission != null) {
			logs.addEntry(hasntOwnerPermission);
		}

		// 4.2 - the user must be approved on the target test case
		LogEntry hasntCallPermission = checkPermissionOnProject(PERM_READ, calledTestCase, target);
		if (hasntCallPermission != null) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CALL_NOT_READABLE));

		}

		// 5 - the step must exist
		boolean exists = model.stepExists(target);
		if (!exists) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_STEP_NOT_EXISTS));
		} else {
			// 6 - check that this is a call step
			StepType type = model.getType(target);
			if (type != StepType.CALL) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NOT_A_CALLSTEP));
			}

			// 7 - no call step cycles allowed
			if (model.wouldCreateCycle(target, calledTestCase)) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CYCLIC_STEP_CALLS,
						new Object[] { target.getTestCase().getPath(), calledTestCase.getPath() }));
			}
		}

		return logs;
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);

		// 2 - can the user do it
		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase(), target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 3 - can that step be identified precisely ?
		LogEntry indexCheckLog = checkStepIndex(ImportMode.DELETE, target, ImportStatus.FAILURE, null);
		if (indexCheckLog != null) {
			logs.addEntry(indexCheckLog);
		}

		return logs;
	}

	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicParameterChecks(target);

		// 2 - does it already exists ?
		if (model.doesParameterExists(target)) {
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_PARAMETER_ALREADY_EXISTS,
					Messages.IMPACT_PARAM_UPDATED));
		}

		// 3 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner(), target);
		if (hasNoPermission != null) {
			logs.addEntry(hasNoPermission);
		}

		return logs;
	}

	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {

		LogTrain logs;

		// 1 - basic checks
		logs = entityValidator.basicParameterChecks(target);

		// 2 - does it exists ?
		if (!model.doesParameterExists(target)) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PARAMETER_NOT_FOUND));
		}

		// 3 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner(), target);
		if (hasNoPermission != null) {
			logs.addEntry(hasNoPermission);
		}

		return logs;

	}

	@Override
	public LogTrain deleteParameter(ParameterTarget target) {

		LogTrain logs = new LogTrain();

		// 1 - does it exists ?
		if (!model.doesParameterExists(target)) {
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PARAMETER_NOT_FOUND));
		}

		// 2 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner(), target);
		if (hasNoPermission != null) {
			logs.addEntry(hasNoPermission);
		}

		return logs;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value, boolean isUpdate) {

		/*
		 * Feat 3695 in this method we must assume that all the checks on the dataset were already logged.
		 * For our purpose here we still need to check if the dataset is correct (because the rest depends on it),
		 * but we don't need to log it twice. That's why we keep ther log trains appart.
		 * 
		 */
		LogTrain logs;
		LogTrain junk;

		// 0 - is the dataset correctly identifed ?
		junk = entityValidator.basicDatasetCheck(dataset);

		// 1 - is the parameter correctly identified ?
		logs = (entityValidator.basicParameterValueChecks(param));

		// in this context specifically we set the target explicitly as being the dataset, not the parameter
		// (or the logs will be reported at the wrong place)
		logs.setForAll(dataset);

		// go further if no blocking errors are detected
		if (!(logs.hasCriticalErrors() || junk.hasCriticalErrors())) {

			// 2 - is such parameter available for this dataset ?
			if (!model.isParamInDataset(param, dataset)) {
				logs.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_DATASET_PARAMETER_MISMATCH));
			}

			// 3 - is the user allowed to do so ?
			LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, dataset.getTestCase(), dataset);
			if (hasNoPermission != null) {
				logs.addEntry(hasNoPermission);
			}
		}
		return logs;
	}

	@Override
	public LogTrain createDataset(DatasetTarget dataset) {

		LogTrain logs;

		// 1 - is the dataset correctly identifed ?
		logs = entityValidator.basicDatasetCheck(dataset);


		// 2 - is the user allowed to do so ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, dataset.getTestCase(), dataset);
		if (hasNoPermission != null) {
			logs.addEntry(hasNoPermission);
		}

		return logs;
	}

	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {

		LogTrain logs;

		// 1 - is the dataset correctly identified ?
		logs = entityValidator.basicDatasetCheck(dataset);

		// 2 - does the dataset exists ?
		if (!model.doesDatasetExists(dataset)) {
			logs.addEntry(new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_DATASET_NOT_FOUND));
		}

		// 3 - has the user the required privilege ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, dataset.getTestCase(), dataset);
		if (hasNoPermission != null) {
			logs.addEntry(hasNoPermission);
		}

		return logs;

	}

	// **************************** private utilities *****************************************

	/**
	 * checks permission on a project that may exist or not. <br/>
	 * the case where the project doesn't exist (and thus has no id) is already covered in the basic checks.
	 */
	private LogEntry checkPermissionOnProject(String permission, TestCaseTarget target, Target checkedTarget) {

		LogEntry entry = null;

		Long libid = model.getProjectStatus(target.getProject()).testCaseLibraryId;
		if ((libid != null)
				&& (!permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, permission, libid, LIBRARY_CLASSNAME))) {
			entry = new LogEntry(checkedTarget, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[] { permission,
					target.getPath() });
		}

		return entry;
	}

	private LogEntry checkStepIndex(ImportMode mode, TestStepTarget target, ImportStatus importStatus,
			String optionalImpact) {
		Integer index = target.getIndex();
		LogEntry entry = null;

		if (index == null) {
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_EMPTY, optionalImpact);
		} else if (index < 0) {
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_NEGATIVE, optionalImpact);

		} else if (!model.stepExists(target)
				&& (!model.indexIsFirstAvailable(target) || !mode.equals(ImportMode.CREATE))) {
			// when index doesn't match a step in the target model
			// this error message is not needed for creation when the target index is the first one available
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_OVERFLOW, optionalImpact);
		}

		return entry;
	}
}
