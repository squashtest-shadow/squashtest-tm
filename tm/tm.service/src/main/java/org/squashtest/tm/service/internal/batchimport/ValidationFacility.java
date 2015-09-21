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

import static org.squashtest.tm.service.internal.batchimport.Model.Existence.EXISTS;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.TO_BE_CREATED;
import static org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn.REQ_VERSION_MILESTONE;
import static org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn.REQ_VERSION_NUM;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.LogEntry.Builder;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchimport.MilestoneImportHelper.Partition;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.StepType;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageInstruction;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageTarget;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.user.UserAccountService;

/**
 *
 * This implementation solely focuses on validating data. It doesn't perform any
 * operation against the database, nor modifies the model : it justs uses the
 * current data available.
 *
 */
@Component
@Scope("prototype")
public class ValidationFacility implements Facility, ValidationFacilitySubservicesProvider {
	/**
	 * Strategy for validating milestones. Should be specialized in create and update
	 * @author Gregory Fouquet
	 *
	 */
	private abstract class MilestonesValidationStrategy {
		public void validateMilestones(TestCaseInstruction instr, LogTrain logs) {
			TestCaseTarget target = instr.getTarget();

			if (!(milestonesEnabled || instr.getMilestones().isEmpty())) {
				logs.addEntry(logEntry().forTarget(target)
						.withMessage(Messages.ERROR_MILESTONE_FEATURE_DEACTIVATED).build());
			}

			if (milestonesEnabled) {
				Partition existing = milestoneHelper.partitionExisting(instr.getMilestones());
				Partition bindables = milestoneHelper.partitionBindable(existing.passing);
				logs.addEntries(logUnknownMilestones(target, existing.rejected));
				logs.addEntries(logUnbindableMilestones(target, bindables.rejected));
			}
		}

		protected abstract LogEntry.Builder logEntry();

		/**
		 * @param rejected
		 * @return
		 */
		protected List<LogEntry> logUnbindableMilestones(TestCaseTarget target, List<String> rejected) {
			ArrayList<LogEntry> logs = new ArrayList<>(rejected.size());
			for (String name : rejected) {
				logs.add(logEntry().forTarget(target).withMessage(Messages.ERROR_WRONG_MILESTONE_STATUS, name)
						.build());
			}
			return logs;
		}

		/**
		 * @param rejected
		 * @return
		 */
		protected List<LogEntry> logUnknownMilestones(TestCaseTarget target, List<String> rejected) {
			ArrayList<LogEntry> logs = new ArrayList<>(rejected.size());
			for (String name : rejected) {
				logs.add(logEntry().forTarget(target).withMessage(Messages.ERROR_UNKNOWN_MILESTONE, name)
						.build());
			}
			return logs;
		}

	}

	private final class CreationStrategy extends MilestonesValidationStrategy {
		/**
		 * @see org.squashtest.tm.service.internal.batchimport.ValidationFacility.MilestonesValidationStrategy#logEntry()
		 */
		@Override
		protected Builder logEntry() {
			return LogEntry.failure();
		}
	}

	private final class UpdateStrategy extends MilestonesValidationStrategy {
		/**
		 * @see org.squashtest.tm.service.internal.batchimport.ValidationFacility.MilestonesValidationStrategy#logEntry()
		 */
		@Override
		protected Builder logEntry() {
			return LogEntry.warning();
		}

	}

	/**
	 * Strategy for validating milestones. Should be specialized in create and update
	 * @author Gregory Fouquet
	 *
	 */
	private abstract class RequirementMilestonesValidationStrategy {
		public void validateMilestones(RequirementVersionInstruction instr, LogTrain logs) {
			RequirementVersionTarget target = instr.getTarget();

			if (!(milestonesEnabled || instr.getMilestones().isEmpty())) {
				logs.addEntry(logEntry().forTarget(target)
						.withMessage(Messages.ERROR_MILESTONE_FEATURE_DEACTIVATED).build());
			}

			if (milestonesEnabled) {
				Partition existing = milestoneHelper.partitionExisting(instr.getMilestones());
				Partition bindables = milestoneHelper.partitionBindable(existing.passing);
				logs.addEntries(logUnknownMilestones(target, existing.rejected));
				logs.addEntries(logUnbindableMilestones(target, bindables.rejected));
			}
		}

		protected abstract LogEntry.Builder logEntry();

		/**
		 * @param rejected
		 * @return
		 */
		protected List<LogEntry> logUnbindableMilestones(RequirementVersionTarget target, List<String> rejected) {
			ArrayList<LogEntry> logs = new ArrayList<>(rejected.size());
			for (String name : rejected) {
				logs.add(logEntry().forTarget(target).withMessage(Messages.ERROR_WRONG_MILESTONE_STATUS, name)
						.build());
				target.rejectedMilestone();
			}
			return logs;
		}

		/**
		 * @param rejected
		 * @return
		 */
		protected List<LogEntry> logUnknownMilestones(RequirementVersionTarget target, List<String> rejected) {
			ArrayList<LogEntry> logs = new ArrayList<>(rejected.size());
			for (String name : rejected) {
				logs.add(logEntry().forTarget(target).withMessage(Messages.ERROR_UNKNOWN_MILESTONE, name)
						.build());
				target.rejectedMilestone();
			}
			return logs;
		}

	}

	private final class RequirementVersionCreationStrategy extends RequirementMilestonesValidationStrategy {
		/**
		 * @see org.squashtest.tm.service.internal.batchimport.ValidationFacility.MilestonesValidationStrategy#logEntry()
		 */
		@Override
		protected Builder logEntry() {
			return LogEntry.failure();
		}
	}

	private final class RequirementVersionUpdateStrategy extends RequirementMilestonesValidationStrategy {
		/**
		 * @see org.squashtest.tm.service.internal.batchimport.ValidationFacility.MilestonesValidationStrategy#logEntry()
		 */
		@Override
		protected Builder logEntry() {
			return LogEntry.warning();
		}

	}

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String PERM_CREATE = "CREATE";
	private static final String PERM_WRITE = "WRITE";
	private static final String PERM_DELETE = "DELETE";
	private static final String PERM_READ = "READ";
	private static final String LIBRARY_CLASSNAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";
	private static final String REQUIREMENT_VERSION_LIBRARY_CLASSNAME = "org.squashtest.tm.domain.requirement.RequirementLibrary";


	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationFacility.class);


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

	@Inject
	@Value("#{" + Authorizations.MILESTONE_FEAT_ENABLED + "}")
	private boolean milestonesEnabled;

	@Inject
	private MilestoneImportHelper milestoneHelper;

	@Inject
	private RequirementLibraryNavigationService reqLibNavigationService;

	@Inject
	private TestCaseLibraryNavigationService tcLibNavigationService;

	@Inject
	private RequirementLibraryFinderService reqFinderService;

	@Inject
	private RequirementVersionCoverageDao coverageDao;

	private EntityValidator entityValidator = new EntityValidator(this);
	private CustomFieldValidator cufValidator = new CustomFieldValidator();
	private CreationStrategy creationStrategy = new CreationStrategy();
	private UpdateStrategy updateStrategy = new UpdateStrategy();
	private RequirementVersionCreationStrategy requirementVersionCreationStrategy =  new RequirementVersionCreationStrategy();
	private RequirementVersionUpdateStrategy requirementVersionUpdateStrategy =  new RequirementVersionUpdateStrategy();


	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public InfoListItemFinderService getInfoListItemService() {
		return infoListItemService;
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
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_CANT_RENAME, new String[] {
						path, newPath }));
			}
		}
	}
	
	/**
	 * Will replace {@code mixin.createdBy} and {@code mixin.createdOn} if the
	 * values are invalid :
	 * <ul>
	 * <li>{@code mixin.createdBy} will be replaced by the current user's login</li>
	 * <li>{@code mixin.createdOn} will be replaced by the import date.</li>
	 * </ul>
	 *
	 * @param target
	 *
	 * @param auditable
	 * @param create
	 * @return a list of logEntries
	 */
	private List<LogEntry> fixMetadatas(Target target, AuditableMixin auditable, ImportMode importMode, EntityType type) {
		// init vars
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		String login = auditable.getCreatedBy();
		boolean fixUser = false;
		if (StringUtils.isBlank(login)) {
			// no value for created by
			fixUser = true;
		} else {
			User user = userDao.findUserByLogin(login);
			if (user == null || !user.getActive()) {
				// user not found or not active
				String warningMessage = null;
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
				switch (type) {
				case REQUIREMENT_VERSION:
					warningMessage = Messages.ERROR_REQ_USER_NOT_FOUND;
					break;
				case TEST_CASE:
					warningMessage = Messages.ERROR_TC_USER_NOT_FOUND;
					break;
				default:
					break;
				}
				LogEntry logEntry = new LogEntry(target, ImportStatus.WARNING,warningMessage,
						impactMessage);
				logEntries.add(logEntry);
				fixUser = true;
			}
		}
		if (fixUser) {
			auditable.setCreatedBy(userAccountService.findCurrentUser().getLogin());
		}

		if (auditable.getCreatedOn() == null) {
			auditable.setCreatedOn(new Date());
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

		// 4 - milestone lock ?
		if (model.isTestCaseLockedByMilestones(target)){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
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

		// 4 - the test case must not be locked by a milestone
		if (model.isTestCaseLockedByMilestones(target.getTestCase())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
		}

		// 5 - check the index
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
			logs.addEntry(new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_CALL_NOT_READABLE,
					Messages.IMPACT_CALL_AS_ACTION_STEP));
		}

		// 4.3 - the test case must not be locked by a milestone
		if (model.isTestCaseLockedByMilestones(target.getTestCase())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
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

		// 4 - the test case must not be locked by a milestone
		if (model.isTestCaseLockedByMilestones(target.getTestCase())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
		}

		// 5 - the step must exist
		boolean exists = model.stepExists(target);
		if (!exists) {
			if (target.getIndex() == null) {
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_STEPINDEX_EMPTY));
			} else if (target.getIndex() < 0) {
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
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase,
			CallStepParamsInfo paramInfos, ActionTestStep actionStepBackup) {

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

		// 4.3 - the test case must not be locked by a milestone
		if (model.isTestCaseLockedByMilestones(target.getTestCase())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
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

		// 3 - the test case must not be locked by a milestone
		if (model.isTestCaseLockedByMilestones(target.getTestCase())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
		}

		// 4 - can that step be identified precisely ?
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
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value,
			boolean isUpdate) {

		/*
		 * Feat 3695 in this method we must assume that all the checks on the
		 * dataset were already logged. For our purpose here we still need to
		 * check if the dataset is correct (because the rest depends on it), but
		 * we don't need to log it twice. That's why we keep ther log trains
		 * appart.
		 */
		LogTrain logs;
		LogTrain junk;

		// 0 - is the dataset correctly identifed ?
		junk = entityValidator.basicDatasetCheck(dataset);

		// 1 - is the parameter correctly identified ?
		logs = (entityValidator.basicParameterValueChecks(param));

		// in this context specifically we set the target explicitly as being
		// the dataset, not the parameter
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

	// **************************** private utilities
	// *****************************************

	/**
	 * checks permission on a project that may exist or not. <br/>
	 * the case where the project doesn't exist (and thus has no id) is already
	 * covered in the basic checks.
	 */
	private LogEntry checkPermissionOnProject(String permission, TestCaseTarget target, Target checkedTarget) {

		LogEntry entry = null;

		Long libid = model.getProjectStatus(target.getProject()).getTestCaseLibraryId();
		if ((libid != null)
				&& (!permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, permission, libid, LIBRARY_CLASSNAME))) {
			entry = new LogEntry(checkedTarget, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[] {
					permission, target.getPath() });
		}

		return entry;
	}

	private LogEntry checkPermissionOnProject(String permission, RequirementVersionTarget target, Target checkedTarget) {

		LogEntry entry = null;

		Long libid = model.getProjectStatus(target.getProject()).getRequirementLibraryId();
		if ((libid != null)
				&& (!permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, permission, libid, REQUIREMENT_VERSION_LIBRARY_CLASSNAME))) {
			entry = new LogEntry(checkedTarget, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[] {
					permission, target.getPath() });
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
			// this error message is not needed for creation when the target
			// index is the first one available
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_OVERFLOW, optionalImpact);
		}

		return entry;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#createTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain createTestCase(TestCaseInstruction instr) {
		TestCaseTarget target = instr.getTarget();
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();

		LogTrain logs;
		String path = target.getPath();
		String name = testCase.getName();
		TargetStatus status = model.getStatus(target);

		// 1 - basic verifications
		logs = entityValidator.createTestCaseChecks(target, testCase);

		// 2 - custom fields (create)
		logs.append(cufValidator.checkCreateCustomFields(target, cufValues,
				model.getTestCaseCufs(target)));

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
		if (!StringUtils.isBlank(name) && !PathUtils.arePathsAndNameConsistents(path, name)) {
			logs.addEntry(LogEntry.warning().forTarget(target)
					.withMessage(Messages.ERROR_INCONSISTENT_PATH_AND_NAME, path, name == null ? "" : name).build());
		}

		// 3-4 : check the milestones
		creationStrategy.validateMilestones(instr, logs);

		// 3-5 : fix test case metadatas
		List<LogEntry> logEntries = fixMetadatas(target, (AuditableMixin) testCase, ImportMode.CREATE,EntityType.TEST_CASE);
		logs.addEntries(logEntries);
		return logs;

	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#updateTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain updateTestCase(TestCaseInstruction instr) {
		TestCase testCase = instr.getTestCase();
		TestCaseTarget target = instr.getTarget();
		Map<String, String> cufValues = instr.getCustomFields();

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
			logs.append(cufValidator.checkUpdateCustomFields(target, cufValues,
					model.getTestCaseCufs(target)));

			// 3 - other checks
			// 3-1 : check if the test case is renamed and would induce a
			// potential name clash.
			// arePathsAndNameConsistent() will tell us if the test case is
			// renamed
			checkPathForUpdate(target, name, logs);

			// 3-2 : permissions. note about the following 'if' : the case where
			// the project doesn't exist (and thus has
			// no id) is already covered in the basic checks.
			LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target, target);
			if (hasntPermission != null) {
				logs.addEntry(hasntPermission);
			}

			// 3-3 : milestone lock ?
			if (model.isTestCaseLockedByMilestones(target)){
				logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_MILESTONE_LOCKED));
			}

			// 3-4 : check audit datas
			// backup the audit log
			List<LogEntry> logEntries = fixMetadatas(target, (AuditableMixin) testCase, ImportMode.UPDATE, EntityType.TEST_CASE);
			logs.addEntries(logEntries);

			updateStrategy.validateMilestones(instr, logs);
		}

		return logs;

	}

	@Override
	public LogTrain createRequirementVersion(RequirementVersionInstruction instr) {

		RequirementVersionTarget target = instr.getTarget();
		RequirementTarget reqTarget = target.getRequirement();
		RequirementVersion reqVersion = instr.getRequirementVersion();
		Map<String, String> cufValues = instr.getCustomFields();

		LogTrain logs;
		LOGGER.debug("Req-Import - In Validation Facility for create " + target.getPath() + " version " + target.getVersion());

		// 1 - basic verifications
		logs = entityValidator.createRequirementVersionChecks(target, reqVersion);

		//   - Check conflict between folder already created by previous import
		// and the path the line we are presently importing
		checkFolderConflict(instr,logs);

		//   - Check status and put the requirement version status to WIP if needed
		//   - The required status will be affected to requirement version in post process
		checkImportedRequirementVersionStatus(target, reqVersion);

		// 2 - custom fields (create)
		logs.append(cufValidator.checkCreateCustomFields(target, cufValues,
				model.getRequirementVersionCufs(target)));

		// 3 - Check permissions
		LogEntry hasntPermission = checkPermissionOnProject(PERM_CREATE, target, target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		// 4 - Check version number
		//		-> change it to the last index +1 if problem and keep trace of the user version number
		checkAndFixRequirementVersionNumber(target, reqVersion, logs);

		// 5 - Check and fix name consistency between path and req name version
		checkAndFixNameConsistency(target, reqVersion);

		// 6 - Check milestone validity
		requirementVersionCreationStrategy.validateMilestones(instr, logs);

		// 7 - Check milestone already used by other requirement version of the same requirement
		checkMilestonesAlreadyUsedInRequirement(instr, logs);

		// 8 - Fix createdOn and createdBy
		logs.addEntries(fixMetadatas(target, (AuditableMixin) reqVersion, ImportMode.CREATE,EntityType.REQUIREMENT_VERSION));

		// 9 - Now update model if the requirement version has passed all check
		if (!logs.hasCriticalErrors()) {
			model.addRequirementVersion(target, new TargetStatus(TO_BE_CREATED), instr.getMilestones());
			//if requirement not exists then pass it to status TO_BE_CREATED
			//with actual implementation of addNode in requirement tree, parent node should already have the good status
			if (model.getStatus(reqTarget).getStatus()==Existence.NOT_EXISTS) {
				model.addRequirement(reqTarget, new TargetStatus(TO_BE_CREATED));
			}
		}
		else {
			//this instruction smell bad..
			instr.fatalError();
		}
		return logs;
	}

	/**
	 * Set the requirementVersionStatus to {@link RequirementStatus#WORK_IN_PROGRESS} to avoid
	 * illegal modification exception on {@link RequirementVersion} with other status
	 * and save the modified {@link RequirementStatus} in target to reassign it in postprocess
	 * @param target
	 * @param reqVersion
	 */
	private void checkImportedRequirementVersionStatus(RequirementVersionTarget target,
			RequirementVersion reqVersion) {
		RequirementStatus requirementVersionStatus = reqVersion.getStatus();
		if (requirementVersionStatus!=null && requirementVersionStatus!=RequirementStatus.WORK_IN_PROGRESS) {
			target.setImportedRequirementStatus(requirementVersionStatus);
			reqVersion.setStatus(RequirementStatus.WORK_IN_PROGRESS);
		}

	}

	/**
	 * Check if an existing requirement version has a status which forbid modifications.
	 * Used for update
	 * @param target
	 * @param logs
	 */
	private void checkExistingRequirementVersionStatus(
			RequirementVersionTarget target, LogTrain logs) {


		if (!logs.hasCriticalErrors()) {

			Requirement requirement = reqLibNavigationService.findRequirement(target.getRequirement().getId());
			RequirementVersion persistedReqVersion = requirement.findRequirementVersion(target.getVersion());
			RequirementStatus persistedStatus = persistedReqVersion.getStatus();

			if (!persistedStatus.isRequirementModifiable()) {
				logs.addEntry(LogEntry.failure().forTarget(target).
						withMessage(Messages.ERROR_REQUIREMENT_VERSION_STATUS).build());
			}
		}
	}

	@Override
	public LogTrain updateRequirementVersion(RequirementVersionInstruction instr) {
		RequirementVersionTarget target = instr.getTarget();
		RequirementTarget reqTarget = target.getRequirement();
		RequirementVersion reqVersion = instr.getRequirementVersion();
		Map<String, String> cufValues = instr.getCustomFields();

		LogTrain logs;

		// 1 - basic verifications
		logs = entityValidator.updateRequirementChecks(target, reqVersion);
		checkImportedRequirementVersionStatus(target, reqVersion);


		logs.append(cufValidator.checkUpdateCustomFields(target, cufValues,
				model.getRequirementVersionCufs(target)));

		// 2 - Check if target requirement version exists in database (targetStatus == EXISTS) and id isn't null
		checkRequirementVersionExists(target, logs);
		// if something is wrong at this stage, either the requirement or the version are unknown or locked,
		// return now to avoid nasty exception in next checks
		if (logs.hasCriticalErrors()) {
			instr.fatalError();
			return logs;
		}

		checkExistingRequirementVersionStatus(target,logs);

		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target, target);
		if (hasntPermission != null) {
			logs.addEntry(hasntPermission);
		}

		checkAndFixNameConsistency(target, reqVersion);

		requirementVersionUpdateStrategy.validateMilestones(instr, logs);

		checkMilestonesAlreadyUsedInRequirement(instr, logs);

		logs.addEntries(fixMetadatas(target, (AuditableMixin) reqVersion, ImportMode.UPDATE, EntityType.REQUIREMENT_VERSION));

		if (logs.hasCriticalErrors()) {
			instr.fatalError();
		}

		return logs;
	}



	private void checkRequirementVersionExists(RequirementVersionTarget target,
			LogTrain logs) {

		TargetStatus status = model.getStatus(target);
		TargetStatus reqStatus = model.getStatus(target.getRequirement());

		//checking requirement and loading his id
		if (reqStatus.getStatus()!=Existence.EXISTS || reqStatus.getId()==null) {
			logs.addEntry(LogEntry.failure().forTarget(target).
					withMessage(Messages.ERROR_REQUIREMENT_VERSION_NOT_EXISTS).build());
		}

		//checking requirement version and loading his id
		else if (status.getStatus()!=Existence.EXISTS || status.getId()==null) {
			logs.addEntry(LogEntry.failure().forTarget(target).
					withMessage(Messages.ERROR_REQUIREMENT_VERSION_NOT_EXISTS).build());
		}

		else {
			//setting the id also in the instruction target, more convenient for updating operations
			target.getRequirement().setId(reqStatus.getId());
		}
	}

	private void checkFolderConflict(RequirementVersionInstruction instr, LogTrain logs) {
		RequirementVersionTarget target = instr.getTarget();
		if (model.isRequirementFolder(target)) {
			logs.addEntry(LogEntry.warning().forTarget(target).
					withMessage(Messages.WARN_REQ_PATH_IS_FOLDER).withImpact(Messages.IMPACT_REQ_RENAMED).build());
			//now changing the path of target
			fixPathFolderConflict(instr);
		}
	}

	private void fixPathFolderConflict(RequirementVersionInstruction instr) {
		RequirementVersionTarget target = instr.getTarget();

		//changing path
		target.getRequirement().setPath(appendReqNameSuffix(target.getPath()));

		//changing name to avoid further conflict when postProcessRenaming occurs
		//so already renamed requirement for conflict will not be renamed again...
		String name = PathUtils.extractName(target.getPath());
		instr.getRequirementVersion().setName(name);
	}

	private String appendReqNameSuffix(String name) {
		StringBuffer pathBuffer = new StringBuffer(name);
		pathBuffer.append(Messages.REQ_RENAME_SUFFIX);
		return pathBuffer.toString();
	}

	private void checkAndFixNameConsistency(RequirementVersionTarget target,
			RequirementVersion reqVersion) {
		String reqName = PathUtils.extractName(target.getPath());
		String reqVersionName = reqVersion.getName();

		if (!reqName.equals(reqVersionName)) {
			target.setUnconsistentName(reqVersionName);
			reqVersion.setName(reqName);
		}
	}

	private void checkMilestonesAlreadyUsedInRequirement(
			RequirementVersionInstruction instr, LogTrain logs) {

		List<String> milestones = instr.getMilestones();
		RequirementVersionTarget target = instr.getTarget();

		for (String milestone : milestones) {
			if (model.checkMilestonesAlreadyUsedInRequirement(milestone,target)) {
				logs.addEntry(LogEntry.warning().forTarget(target).withMessage(Messages.WARN_MILESTONE_USED, REQ_VERSION_MILESTONE.header)
						.withImpact(Messages.IMPACT_MILESTONE_NOT_BINDED).build());
			}
		}
	}

	private void checkAndFixRequirementVersionNumber(
			RequirementVersionTarget target, RequirementVersion reqVersion, LogTrain logs) {
		//fixing null/0 values from user -> setting versionNumber to 1 (default value for new requirement)
		if (target.getVersion()==null||target.getVersion()<=0) {
			target.setVersion(1);//setting to 0 as fixVersionNumber will increment this value
			fixVersionNumber(target);
			logs.addEntry(LogEntry.warning().forTarget(target).withMessage(Messages.ERROR_REQUIREMENT_VERSION_NULL, REQ_VERSION_NUM.header)
					.withImpact(Messages.IMPACT_VERSION_NUMBER_MODIFIED).build());
		}
		//check if requirement exist
		Existence requirementVersionStatus = model.getStatus(target).getStatus();
		Existence requirementStatus = model.getStatus(target.getRequirement()).getStatus();

		LOGGER.debug("ReqImport Checking for version number : " + target.getVersion());
		LOGGER.debug("ReqImport Status of version : " + target.getVersion() + " " + requirementVersionStatus);

		if (requirementStatus != Existence.NOT_EXISTS
				&& requirementVersionStatus != Existence.NOT_EXISTS) {
			fixVersionNumber(target);
			logs.addEntry(LogEntry.warning().forTarget(target).withMessage(Messages.ERROR_REQUIREMENT_VERSION_COLLISION, REQ_VERSION_NUM.header)
					.withImpact(Messages.IMPACT_VERSION_NUMBER_MODIFIED).build());
		}
		
	}

	private void fixVersionNumber(RequirementVersionTarget target) {
		Existence requirementVersionStatus = model.getStatus(target).getStatus();
		if (requirementVersionStatus == Existence.NOT_EXISTS) {
			return;
		}
		target.setVersion(target.getVersion() + 1);
		fixVersionNumber(target);
	}



	@Override
	public LogTrain deleteRequirementVersion(RequirementVersionInstruction instr) {
		throw new RuntimeException("Implement me");
	}

	boolean areMilestoneValid(TestCaseInstruction instr){
		LogTrain dummy = new LogTrain();
		updateStrategy.validateMilestones(instr, dummy);
		return dummy.hasNoErrorWhatsoever();
	}

	@Override
	public LogTrain createCoverage(CoverageInstruction instr) {
		LogTrain logs = new LogTrain();

		CoverageTarget target = instr.getTarget();

		Long tcId = checkTcForCoverage(target, logs);
		Long reqVersionId = checkRequirementVersionForCoverage(target, logs);
		checkCoverageAlreadyExist(target, logs, tcId, reqVersionId);


		return logs;
	}

	private void checkCoverageAlreadyExist(CoverageTarget target, LogTrain logs, Long tcId, Long reqVersionId) {
		if (tcId != null && reqVersionId != null
				&& coverageDao.byRequirementVersionAndTestCase(reqVersionId, tcId) != null) {
			logs.addEntry(createLogFailure(target, Messages.ERROR_COVERAGE_ALREADY_EXIST));
		}
	}

	private Long checkRequirementVersionForCoverage(CoverageTarget target, LogTrain logs) {
		boolean requirementPathAndVersionValid = checkRequirementVersionDataValidity(target, logs);

		if (requirementPathAndVersionValid) {
			return checkRequirementVersionExist(target, logs);
		}

		return null;
	}

	private Long checkRequirementVersionExist(CoverageTarget target, LogTrain logs) {

		Long reqId = reqFinderService.findNodeIdByPath(target.getReqPath());

		if (reqId != null) {
			Requirement req = reqLibNavigationService.findRequirement(reqId);
			RequirementVersion reqVersion = req.findRequirementVersion(target.getReqVersion());
			if (reqVersion != null) {
				if (!req.getStatus().isRequirementLinkable()) {
					logs.addEntry(createLogFailure(target, Messages.ERROR_REQUIREMENT_VERSION_STATUS));
				}
				return reqVersion.getId();
			} else {
				logs.addEntry(createLogFailure(target, Messages.ERROR_REQUIREMENT_VERSION_NOT_EXISTS));
			}
		} else {
			logs.addEntry(createLogFailure(target, Messages.ERROR_REQUIREMENT_NOT_EXISTS));
		}


		return null;
	}

	private boolean checkRequirementVersionDataValidity(CoverageTarget target, LogTrain logs) {

		boolean reqPathValid = target.isReqPathWellFormed();
		boolean reqVersionValid = target.getReqVersion() > 0;

		if (!reqPathValid) {
			logs.addEntry(createLogFailure(target, Messages.ERROR_MALFORMED_PATH, target.getReqPath()));
		}

		if (!reqVersionValid) {
			logs.addEntry(createLogFailure(target, Messages.ERROR_REQUIREMENT_VERSION_INVALID));
		}

		return reqPathValid && reqVersionValid;

	}

	private Long checkTcForCoverage(CoverageTarget target, LogTrain logs) {
		boolean tcPathValid = target.isTcPathWellFormed();
		String tcPath = target.getTcPath();

		if (!tcPathValid) {
			logs.addEntry(createLogFailure(target, Messages.ERROR_MALFORMED_PATH, tcPath));

		} else {
			Long id = tcLibNavigationService.findNodeIdByPath(tcPath);
			if (id == null) {
				logs.addEntry(createLogFailure(target, Messages.ERROR_TC_NOT_FOUND, tcPath));
			} else {
				return id;
			}
		}
		return null;
	}

	private LogEntry createLogFailure(Target target, String msg, Object... msgArgs) {
		return LogEntry.failure().forTarget(target).withMessage(msg, msgArgs).build();
	}


}
