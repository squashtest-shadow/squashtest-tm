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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageInstruction;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageTarget;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementLibraryNodeDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

/**
 *
 * Implementation of batch import methods that will actually update the
 * database.
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
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseModificationService testcaseModificationService;

	@Inject
	private PrivateCustomFieldValueService cufvalueService;

	@Inject
	private CallStepManagerService callstepService;

	@Inject
	private ParameterModificationService parameterService;

	@Inject
	private DatasetModificationService datasetService;

	@Inject
	private RequirementLibraryNavigationService reqLibNavigationService;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private DatasetParamValueDao paramvalueDao;

	@Inject
	private ParameterDao paramDao;

	@Inject
	private InfoListItemFinderService listItemFinderService;

	@Inject
	private CustomFieldDao cufDao;

	@Inject
	private MilestoneImportHelper milestoneHelper;

	@Inject
	private RequirementLibraryFinderService reqFinderService;

	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;

	@Inject
	private MilestoneMembershipManager milestoneService;

	@Inject
	private RequirementVersionCoverageDao coverageDao;
	
	@Inject
	private HibernateRequirementLibraryNodeDao rlnDao;

	private FacilityImplHelper helper = new FacilityImplHelper();

	private Map<String, CustomFieldInfos> cufInfosCache = new HashMap<String, CustomFieldInfos>();

	private ImportPostProcessHandler postProcessHandler;


	// ************************ public (and nice looking) code
	// **************************************


	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#createTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain createTestCase(TestCaseInstruction instr) {
		LogTrain train = validator.createTestCase(instr);

		if (!train.hasCriticalErrors()) {
			instr.getTestCase().setName(instr.getTarget().getName());
			train = createTCRoutine(train, instr);

		}

		return train;
	}


	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Facility#updateTestCase(org.squashtest.tm.service.internal.batchimport.TestCaseInstruction)
	 */
	@Override
	public LogTrain updateTestCase(TestCaseInstruction instr) {
		TestCaseTarget target = instr.getTarget();
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();

		TargetStatus status = validator.getModel().getStatus(target);

		LogTrain train = validator.updateTestCase(instr);

		if (!train.hasCriticalErrors()) {

			if (status.status == Existence.NOT_EXISTS) {

				train = createTCRoutine(train, instr);

			} else {
				try {

					helper.truncate(testCase, cufValues);
					fixNatureAndType(target, testCase);

					doUpdateTestcase(instr);

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


	/**
	 * May be called either by the create test case scenario, or in an update scenario (business says that
	 * updating a test case that dont exist implies to create it first).
	 *
	 * @param train
	 * @param instruction
	 * @return
	 */
	private LogTrain createTCRoutine(LogTrain train, TestCaseInstruction instruction) {
		TestCase testCase = instruction.getTestCase();
		Map<String, String> cufValues = instruction.getCustomFields();
		TestCaseTarget target = instruction.getTarget();

		try {
			helper.fillNullWithDefaults(testCase);
			helper.truncate(testCase, cufValues);
			fixNatureAndType(target, testCase);

			doCreateTestcase(instruction);
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

	@Override
	public LogTrain createRequirementVersion(RequirementVersionInstruction instr) {
		LogTrain train = validator.createRequirementVersion(instr);
		if (!train.hasCriticalErrors()){
			//CREATE REQUIREMENT VERSION IN DB
			createReqVersionRoutine(train, instr);
			//Assign the create requirement strategy to postProcessHandler
			postProcessHandler = new CreateRequirementVersionPostProcessStrategy();
		}
		return train;
	}

	@Override
	public LogTrain updateRequirementVersion(RequirementVersionInstruction instr) {
		LogTrain train = validator.updateRequirementVersion(instr);
		if (!train.hasCriticalErrors()) {
			updateRequirementVersionRoutine(train, instr);
			postProcessHandler = new UpdateRequirementVersionPostProcessStrategy();
		}
		return train;
	}




	@Override
	public LogTrain deleteRequirementVersion(RequirementVersionInstruction instr) {
		throw new RuntimeException("implement me - must return a Failure : Not implemented in the log train instead of throwing this exception");
	}

	/**
	 * for all other stuffs that need to be done afterward
	 * @param instructions
	 *
	 */
	public void postprocess(List<Instruction<?>> instructions) {
		if (postProcessHandler!=null) {
			postProcessHandler.doPostProcess(instructions);
		}
	}

	// ************************* private (and hairy) code
	// *********************************

	private LogTrain createReqVersionRoutine(LogTrain train, RequirementVersionInstruction instruction) {
		RequirementVersion reqVersion = instruction.getRequirementVersion();
		Map<String, String> cufValues = instruction.getCustomFields();
		RequirementVersionTarget target = instruction.getTarget();

		try {
			helper.fillNullWithDefaults(reqVersion);
			helper.truncate(reqVersion, cufValues);
			fixCategory(target,reqVersion);
			RequirementVersion newVersion = doCreateRequirementVersion(instruction);

			//update model
			validator.getModel().addRequirement(target.getRequirement(),
					new TargetStatus(Existence.EXISTS, newVersion.getRequirement().getId()));

			validator.getModel().addRequirementVersion
				(target, new TargetStatus(Existence.EXISTS, newVersion.getId()));

			//update the instruction, needed for postProcess.
			instruction.setRequirementVersion(newVersion);

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Test Case \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[] { ex.getClass().getName() }));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}

		return train;
	}

	/**
	 * 1 . First create the requirement if not exist in database
			1.1 - Requirement is root (ie under a {@link RequirementLibrary})
					This one is simple, just create the requirement and set the status in requirement tree
			1.2 - Requirement is under another {@link RequirementLibraryNode}
					Must create all the node above the requirement that doesn't exists.
					As specified in 5085 all new nodes above the requirement will be treated as folder
		2 . Create the requirement version :
	 * @param instruction
	 * @return
	 */
	private RequirementVersion doCreateRequirementVersion(
			RequirementVersionInstruction instruction) {
		RequirementVersionTarget target = instruction.getTarget();
		Long reqId = reqFinderService.findNodeIdByPath(target.getPath());
		if (reqId==null) {
			return doCreateRequirementAndVersion(instruction);
		}
		else {
			return doAddingNewVersionToRequirement(instruction,reqId);
		}
	}

	/**
	 * In this method, we assumes that noVersion of the added requirement version is correct
	 * It has been checked and modified if needed by validator
	 * The proccess of creating a new version directly at required position and with correct attributes
	 * is fairly complex, so we follow normal flow in squash TM : create a new requirement version and modify it after
	 * @param instruction
	 */
	private RequirementVersion doAddingNewVersionToRequirement(
			RequirementVersionInstruction instruction,Long reqId) {

		RequirementVersionTarget target = instruction.getTarget();
		Requirement requirement = reqLibNavigationService.findRequirement(reqId);
		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(instruction.getCustomFields());
		RequirementVersion requirementVersion = instruction.getRequirementVersion();
		requirementVersion.setVersionNumber(instruction.getTarget().getVersion());
		//creating new version with service
		requirementVersionManagerService.createNewVersion(reqId);
		//and updating persisted reqVersion
		RequirementVersion requirementVersionPersisted = requirement.getCurrentVersion();
		reqLibNavigationService.initCUFvalues(requirementVersionPersisted, acceptableCufs);
		bindRequirementVersionToMilestones(requirementVersionPersisted, boundMilestonesIds(instruction));
		doUpdateRequirementCoreAttributes(requirementVersion, requirementVersionPersisted);
		doUpdateRequirementMetadata((AuditableMixin)requirementVersion,(AuditableMixin)requirementVersionPersisted);
		fixVersionNumber(requirement, target.getVersion());
		return requirement.findRequirementVersion(target.getVersion());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RequirementVersion doCreateRequirementAndVersion(
			final RequirementVersionInstruction instruction) {
		
		//convenient references as the process is complex...
		final RequirementVersionTarget target = instruction.getTarget();
		String projectName = PathUtils.extractProjectName(target.getPath());
		RequirementVersion requirementVersion = instruction.getRequirementVersion();
		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(instruction.getCustomFields());

		//creating the dto needed for adding new requirement
		final NewRequirementVersionDto dto = new NewRequirementVersionDto(requirementVersion, acceptableCufs);
		dto.setName(PathUtils.unescapePathPartSlashes(dto.getName()));

		//making arrays to avoid immutable problem in visitor inner class
		final Requirement[] finalRequirement = new Requirement[1];
		final Long[] finalParentId = new Long[1];
		
		//now creating the visitor to requirementLibrairyNode
		//this visitor will invoke the good method as parent can be Requirement or RequirementFolder
		RequirementLibraryNodeVisitor visitor = new RequirementLibraryNodeVisitor() {
			
			@Override
			public void visit(Requirement requirement) {
				finalRequirement[0] = reqLibNavigationService.addRequirementToRequirement
						(finalParentId[0], dto, boundMilestonesIds(instruction));
				reqLibNavigationService.moveNodesToRequirement(finalParentId[0], new Long[]{finalRequirement[0].getId()}, target.getRequirement().getOrder());
			}
			
			@Override
			public void visit(RequirementFolder folder) {
				finalRequirement[0] = reqLibNavigationService.addRequirementToRequirementFolder
						(finalParentId[0], dto, boundMilestonesIds(instruction));
				reqLibNavigationService.moveNodesToFolder(finalParentId[0], new Long[]{finalRequirement[0].getId()}, target.getRequirement().getOrder());

			}
		};

		//now creating requirement with following logic :
		//	If requirement is root requirement 
		//		-> addRequirementToRequirementLibrary
		//	Else
		//		If parent doesn't exist in database 
		//			-> Create it and needed hierarchy
		//		-> Now create the imported requirement using visitor polymorphism
		//	-> Do postCreation stuff
		if (target.getRequirement().isRootRequirement()) {
			Long requirementLibrairyId = validator.getModel().getProjectStatus(projectName).getRequirementLibraryId();
			
			finalRequirement[0] = reqLibNavigationService.addRequirementToRequirementLibrary(
					requirementLibrairyId,dto,Collections.EMPTY_LIST);
			reqLibNavigationService.moveNodesToLibrary(requirementLibrairyId, new Long[]{finalRequirement[0].getId()}, target.getRequirement().getOrder());
			milestoneService.bindRequirementVersionToMilestones(finalRequirement[0].getCurrentVersion().getId(), boundMilestonesIds(instruction));
		}
		else {
				List<String> paths = PathUtils.scanPath(target.getPath());
				String parentPath = paths.get(paths.size()-2); //we know that path is composite of at least 3 elements
				finalParentId[0] = reqFinderService.findNodeIdByPath(parentPath);
				//if parent doesn't exist, we must create it and all needed hierarchy above
				if (finalParentId[0]==null) {
					finalParentId[0] = reqLibNavigationService.mkdirs(parentPath);
				}
				RequirementLibraryNode parent = reqLibNavigationService.findRequirementLibraryNodeById(finalParentId[0]);
				parent.accept(visitor);
		}
		
		return doAfterCreationProcess(finalRequirement[0], instruction, requirementVersion);
	}
	
	/**
	 * Here we do all the needed modifications to the freshly created requirement.
	 * @param persistedRequirement
	 * @param instruction
	 * @param requirementVersion
	 * @return the current version, needed for global post process
	 */
	private RequirementVersion doAfterCreationProcess(Requirement persistedRequirement, RequirementVersionInstruction instruction, RequirementVersion requirementVersion){
		RequirementVersionTarget target = instruction.getTarget();
		//bind milestone for import
		bindRequirementVersionToMilestones(persistedRequirement.getCurrentVersion(), boundMilestonesIds(instruction));
		//updating attributes that creation process haven't set (Category... )
		doUpdateRequirementCategory(requirementVersion,persistedRequirement.getCurrentVersion());
		doUpdateRequirementMetadata((AuditableMixin)requirementVersion,(AuditableMixin)persistedRequirement.getCurrentVersion());
		//setting the version number correctly as we can add version number non sequentially with import process
		fixVersionNumber(persistedRequirement, target.getVersion());
		return persistedRequirement.getCurrentVersion();//we have only one version in the new requirement...
	}



	private void doUpdateRequirementMetadata(AuditableMixin requirementVersion,
			AuditableMixin persistedVersion) {
		persistedVersion.setCreatedBy(requirementVersion.getCreatedBy());
		persistedVersion.setCreatedOn(requirementVersion.getCreatedOn());
	}


	private void updateRequirementVersionRoutine(LogTrain train,
			RequirementVersionInstruction instruction) {

		RequirementVersion reqVersion = instruction.getRequirementVersion();
		Map<String, String> cufValues = instruction.getCustomFields();
		RequirementVersionTarget target = instruction.getTarget();


		try {
			helper.fillNullWithDefaults(reqVersion);
			helper.truncate(reqVersion, cufValues);
			fixCategory(target,reqVersion);
			RequirementVersion newVersion = doUpdateRequirementVersion(instruction,cufValues);

			//update the instruction with persisted one, needed for postProcess.
			instruction.setRequirementVersion(newVersion);

			//update model
			validator.getModel().bindMilestonesToRequirementVersion(target,instruction.getMilestones());

			LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Requirement Version \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[] { ex.getClass().getName() }));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}
	}


	private RequirementVersion doUpdateRequirementVersion(
			RequirementVersionInstruction instruction, Map<String, String> cufValues) {

		RequirementVersionTarget target = instruction.getTarget();
		RequirementVersion reqVersion = instruction.getRequirementVersion();

		Requirement req = reqLibNavigationService.
				findRequirement(target.getRequirement().getId());

		RequirementVersion orig = req.findRequirementVersion(target.getVersion());

		doUpdateRequirementCoreAttributes(reqVersion, orig);
		doUpdateRequirementCategory(reqVersion, orig);
		
		//Feat 5169, unbind all milestones if cell is empty in import file.
		//Else, bind milestones if possible
		if (CollectionUtils.isEmpty(instruction.getMilestones())) {
			orig.getMilestones().clear();
		}
		else {
			bindRequirementVersionToMilestones(orig, boundMilestonesIds(instruction));
		}
		doUpdateCustomFields(cufValues,orig);
		doUpdateRequirementMetadata((AuditableMixin)reqVersion,(AuditableMixin)orig);
		moveRequirement(target.getRequirement(), req, target.getRequirement().getOrder());
		//we return the persisted RequirementVersion for post process
		return orig;
	}
	
	@SuppressWarnings("rawtypes")
	private void moveRequirement(RequirementTarget target, final Requirement req, final int newPosition){
		if (target.isRootRequirement()) {
			reqLibNavigationService.moveNodesToLibrary(req.getLibrary().getId(),new Long[]{req.getId()}, newPosition);
		}
		else {
			List<Long> ids = rlnDao.getParentsIds(req.getId());
			Long firstParentId = ids.get(ids.size()-2);
			final RequirementLibraryNode parent = reqLibNavigationService.findRequirementLibraryNodeById(firstParentId);
			
			//creating addhoc visitor
			RequirementLibraryNodeVisitor visitor = new RequirementLibraryNodeVisitor() {
				
				@Override
				public void visit(Requirement requirement) {
					reqLibNavigationService.moveNodesToRequirement(parent.getId(),new Long[]{req.getId()}, newPosition);
				}
				
				@Override
				public void visit(RequirementFolder folder) {
					reqLibNavigationService.moveNodesToFolder(parent.getId(),new Long[]{req.getId()}, newPosition);
				}
			};
			parent.accept(visitor);
		}
	}

	private void doUpdateRequirementCategory(
			RequirementVersion reqVersion, RequirementVersion orig) {
		Long idOrig = orig.getId();

		InfoListItem oldCategory = orig.getCategory();
		InfoListItem newCategory = reqVersion.getCategory();

		if (newCategory!=null && !oldCategory.references(newCategory)) {
			requirementVersionManagerService.changeCategory(idOrig, newCategory.getCode());
		}
	}

	private void doUpdateRequirementCoreAttributes(
			RequirementVersion reqVersion, RequirementVersion orig) {

		Long idOrig = orig.getId();

		String newReference = reqVersion.getReference();
		if (!StringUtils.isBlank(newReference) && !newReference.equals(orig.getReference())) {
			requirementVersionManagerService.changeReference(idOrig, newReference);
		}

		String newDescription = reqVersion.getDescription();
		if (!StringUtils.isBlank(newDescription) && !newDescription.equals(orig.getReference())) {
			requirementVersionManagerService.changeDescription(idOrig, newDescription);
		}

		RequirementCriticality newCriticality = reqVersion.getCriticality();
		if (newCriticality!=null && !newCriticality.equals(orig.getCriticality())) {
			requirementVersionManagerService.changeCriticality(idOrig, newCriticality);
		}
		
		InfoListItem newCategory = reqVersion.getCategory();
		if (newCategory!=null&&!newCategory.equals(orig.getCategory())) {
			requirementVersionManagerService.changeCategory(idOrig, newCategory.getCode());
		}
	}

	private void fixVersionNumber(Requirement requirement, Integer version) {
		reqLibNavigationService.changeCurrentVersionNumber(requirement, version);
	}


	/**
	 * This method ensure that multiple milestone binding to several {@link RequirementVersion} of
	 * the same {@link Requirement} is forbidden. The method in service can't prevent this for import as we are
	 * in a unique transaction for all import lines. So the n-n relationship between milestones and requirementVersion isn't
	 * fixed until transaction is closed and {@link MilestoneMembershipManager#bindRequirementVersionToMilestones(long, Collection)}
	 * will let horrible things appends if this list isn't up to date
	 */
	private void bindRequirementVersionToMilestones(RequirementVersion requirementVersionPersisted,
			List<Long> boundMilestonesIds) {
		List<RequirementVersion> allVersion = requirementVersionPersisted.getRequirement().getRequirementVersions();
		Set<Milestone> milestoneBinded = new HashSet<Milestone>();
		Set<Long> milestoneBindedId = new HashSet<Long>();
		Set<Long> checkedMilestones = new HashSet<Long>();

		for (RequirementVersion requirementVersion : allVersion) {
			milestoneBinded.addAll(requirementVersion.getMilestones());
		}

		for (Milestone milestone : milestoneBinded) {
			milestoneBindedId.add(milestone.getId());
		}

		for (Long id : boundMilestonesIds) {
			if (!milestoneBindedId.contains(id)) {
				checkedMilestones.add(id);
			}
		}

		requirementVersionManagerService.bindMilestones(requirementVersionPersisted.getId(), checkedMilestones);

	}


	// because this time we're not toying around man, this is the real thing
	private void doCreateTestcase(TestCaseInstruction instr) {
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();
		TestCaseTarget target = instr.getTarget();

		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(cufValues);

		// case 1 : this test case lies at the root of the project
		if (target.isRootTestCase()) {
			// libraryId is never null because the checks ensured that the
			// project exists
			Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getTestCaseLibraryId();

			Collection<String> siblingNames = navigationService.findNamesInLibraryStartingWith(libraryId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToLibrary(libraryId, testCase, acceptableCufs, target.getOrder(),
					new ArrayList<Long>());
		}
		// case 2 : this test case exists within a folder
		else {
			Long folderId = navigationService.mkdirs(target.getFolder());
			Collection<String> siblingNames = navigationService.findNamesInFolderStartingWith(folderId,
					testCase.getName());
			renameIfNeeded(testCase, siblingNames);

			List<Long> msids = boundMilestonesIds(instr);

			navigationService.addTestCaseToFolder(folderId, testCase, acceptableCufs, target.getOrder(),msids);
		}

		bindMilestones(instr, testCase);

	}




	private void renameIfNeeded(TestCase testCase, Collection<String> siblingNames) {
		String newName = LibraryUtils.generateNonClashingName(testCase.getName(), siblingNames, TestCase.MAX_NAME_SIZE);
		if (!newName.equals(testCase.getName())) {
			testCase.setName(newName);
		}
	}

	private void doUpdateTestcase(TestCaseInstruction instr) {
		TestCaseTarget target = instr.getTarget();
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();

		TestCase orig = validator.getModel().get(target);
		Long origId = orig.getId();

		// update the test case core attributes

		doUpdateTestCaseCoreAttributes(testCase, orig);

		// the custom field values now

		doUpdateCustomFields(cufValues, orig);

		if (validator.areMilestoneValid(instr)){
			bindMilestones(instr, orig);
		}

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
		navigationService.deleteNodes(Arrays.asList(tc.getId()), null);
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
		if (!StringUtils.isBlank(newAction) && !newAction.equals(orig.getAction())) {
			orig.setAction(newAction);
		}

		String newResult = testStep.getExpectedResult();
		if (!StringUtils.isBlank(newResult) && !newResult.equals(orig.getExpectedResult())) {
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

		if (!StringUtils.isBlank(newName) && !newName.equals(orig.getName())) {
			testcaseModificationService.rename(origId, newName);
		}

		String newRef = testCase.getReference();
		if (!StringUtils.isBlank(newRef) && !newRef.equals(orig.getReference())){
			testcaseModificationService.changeReference(origId, newRef);
		}

		String newDesc = testCase.getDescription();
		if (!StringUtils.isBlank(newDesc) && !newDesc.equals(orig.getDescription())) {
			testcaseModificationService.changeDescription(origId, newDesc);
		}

		String newPrereq = testCase.getPrerequisite();
		if (!StringUtils.isBlank(newPrereq) && !newPrereq.equals(orig.getPrerequisite())) {
			testcaseModificationService.changePrerequisite(origId, newPrereq);
		}

		TestCaseImportance newImp = testCase.getImportance();
		if (newImp != null && !orig.getImportance().equals(newImp)) {
			testcaseModificationService.changeImportance(origId, newImp);
		}

		InfoListItem newNat = testCase.getNature();
		if (newNat != null && !newNat.references(orig.getNature())) {
			testcaseModificationService.changeNature(origId, newNat.getCode());
		}

		InfoListItem newType = testCase.getType();
		if (newType != null && !newType.references(orig.getType())) {
			testcaseModificationService.changeType(origId, newType.getCode());
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

		// else we have to create it. Note that the services do not provide any
		// facility for that
		// so we have to do it from scratch here. Tsss, lazy conception again.
		DatasetParamValue dpv = new DatasetParamValue(dsParam, dbDs);
		paramvalueDao.persist(dpv);
		dbDs.addParameterValue(dpv);

		return dpv;
	}

	/**
	 * because the service identifies cufs by their id, not their code<br/>
	 * also populates the cache (cufIdByCode), and transform the input data in a
	 * single string or a collection of string depending on the type of the
	 * custom field (Tags on non-tags).
	 */
	private Map<Long, RawValue> toAcceptableCufs(Map<String, String> origCufs) {

		Map<Long, RawValue> result = new HashMap<Long, RawValue>(origCufs.size());

		for (Entry<String, String> origCuf : origCufs.entrySet()) {
			String cufCode = origCuf.getKey();

			if (!cufInfosCache.containsKey(cufCode)) {

				CustomField customField = cufDao.findByCode(cufCode);

				// that bit of code checks that if the custom field doesn't
				// exist, the hashmap entry contains
				// a dummy value for this code.
				CustomFieldInfos infos = null;
				if (customField != null) {
					Long id = customField.getId();
					InputType type = customField.getInputType();
					infos = new CustomFieldInfos(id, type);
				}

				cufInfosCache.put(cufCode, infos);
			}

			// now add to our map the id of the custom field, except if null :
			// the custom field
			// does not exist and therefore wont be included.
			CustomFieldInfos infos = cufInfosCache.get(cufCode);
			if (infos != null) {
				switch (infos.getType()) {
				case TAG:
					List<String> values = Arrays.asList(origCuf.getValue().split("\\|"));
					result.put(infos.getId(), new RawValue(values));
					break;
				default:
					result.put(infos.getId(), new RawValue(origCuf.getValue()));
					break;
				}
			}
		}

		return result;

	}

	private void fixNatureAndType(TestCaseTarget target, TestCase testCase) {

		// at this point of the process the target is assumed to be safe for
		// use,
		// no need to defensively check that the project exists and such
		TargetStatus projectStatus = validator.getModel().getProjectStatus(target.getProject());

		InfoListItem nature = testCase.getNature();
		if (nature != null) {
			if (!listItemFinderService.isNatureConsistent(projectStatus.getId(), nature.getCode())) {
				testCase.setNature(listItemFinderService.findDefaultTestCaseNature(projectStatus.getId()));
			}
		}

		InfoListItem type = testCase.getType();
		if (type != null) {
			if (!listItemFinderService.isTypeConsistent(projectStatus.getId(), type.getCode())) {
				testCase.setType(listItemFinderService.findDefaultTestCaseType(projectStatus.getId()));
			}
		}

	}

	private void fixCategory(RequirementVersionTarget target, RequirementVersion requirementVersion) {
		TargetStatus projectStatus = validator.getModel().getProjectStatus(target.getProject());

		InfoListItem category = requirementVersion.getCategory();
		//if category is null or inconsistent for project, setting to default project category
		if (category==null||!listItemFinderService.isCategoryConsistent(projectStatus.getId(), category.getCode())) {
				requirementVersion.setCategory(listItemFinderService.findDefaultRequirementCategory(projectStatus.getId()));
		}
	}

	private static final class CustomFieldInfos {
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


	/**
	 * Returnd the ids of the milestones to be bound as per test case instruction
	 * @param instr the instruction holding the names of candidate milestones
	 * @return
	 */
	private List<Long> boundMilestonesIds(TestCaseInstruction instr) {
		return boundMilestonesIds(instr.getMilestones());
	}

	/**
	 * Returnd the ids of the milestones to be bound as per requirement version instruction
	 * @param instr the instruction holding the names of candidate milestones
	 * @return
	 */
	private List<Long> boundMilestonesIds(RequirementVersionInstruction instr) {
		return boundMilestonesIds(instr.getMilestones());
	}


	private List<Long> boundMilestonesIds(List<String> milestones){
		if (milestones.isEmpty()) {
			return Collections.emptyList();
		}

		List<Milestone> ms =  milestoneHelper.findBindable(milestones);
		List<Long> msids = new ArrayList<>(ms.size());
		for (Milestone m : ms) {
			msids.add(m.getId());
		}
		return msids;
	}


	/**
	 * @param instr instruction read from import file, pointing to a TRANSIENT test case template
	 * @param persistentSource the PERSISTENT test case
	 */
	private void bindMilestones(TestCaseInstruction instr, TestCase persistentSource) {
		if (!instr.getMilestones().isEmpty()) {
			List<Milestone> ms = milestoneHelper.findBindable(instr.getMilestones());
			persistentSource.getMilestones().clear();
			persistentSource.bindAllMilsetones(ms);
		}
		//feat 5169 if milestone cell is empty in xls import file, unbind all milestones
		else {
			persistentSource.getMilestones().clear();
		}

	}

	private interface ImportPostProcessHandler {
		void doPostProcess(List<Instruction<?>> instructions);
	}

	private class CreateRequirementVersionPostProcessStrategy implements ImportPostProcessHandler {

		@Override
		public void doPostProcess(List<Instruction<?>> instructions) {
			for (Instruction<?> instruction : instructions) {
				if (instruction instanceof RequirementVersionInstruction) {
				RequirementVersionInstruction rvi = (RequirementVersionInstruction) instruction;
					if (!rvi.isFatalError()) {
						changeRequirementVersionStatus(rvi);
					}
				}
			}
		}
	}

	private class UpdateRequirementVersionPostProcessStrategy implements ImportPostProcessHandler {

		@Override
		public void doPostProcess(List<Instruction<?>> instructions) {
			for (Instruction<?> instruction : instructions) {
				RequirementVersionInstruction rvi = (RequirementVersionInstruction) instruction;
				if (!rvi.isFatalError()) {
					renameRequirementVersion(rvi);
					changeRequirementVersionStatus(rvi);
				}
			}
		}
	}

	private void renameRequirementVersion(RequirementVersionInstruction rvi) {
		String unconsistentName = rvi.getTarget().getUnconsistentName();
		if (unconsistentName!=null && !StringUtils.isEmpty(unconsistentName)) {
			String newName = PathUtils.unescapePathPartSlashes(unconsistentName);
			requirementVersionManagerService.rename(rvi.getRequirementVersion().getId(), newName);
		}
	}

	private void changeRequirementVersionStatus(
			RequirementVersionInstruction rvi) {
		RequirementStatus newstatus = rvi.getTarget().getImportedRequirementStatus();
		RequirementStatus oldStatus = rvi.getRequirementVersion().getStatus();

		if (newstatus == null || newstatus.equals(oldStatus)) {
			return;
		}

		//The only forbidden transition is from WORK_IN_PROGRESS to APPROVED,
		// so we need to update to UNDER_REVIEW before updating to APPROVED
		if (newstatus == RequirementStatus.APPROVED && oldStatus == RequirementStatus.WORK_IN_PROGRESS) {
			requirementVersionManagerService.changeStatus
				(rvi.getRequirementVersion().getId(), RequirementStatus.UNDER_REVIEW);
		}

		requirementVersionManagerService.changeStatus(rvi.getRequirementVersion().getId(), newstatus);
	}

	@Override
	public LogTrain createCoverage(CoverageInstruction instr) {

		LogTrain train = validator.createCoverage(instr);

		if (!train.hasCriticalErrors()) {
		CoverageTarget target = instr.getTarget();
		Long reqId = reqFinderService.findNodeIdByPath(target.getReqPath());
		Requirement req = reqLibNavigationService.findRequirement(reqId);
		RequirementVersion reqVersion = req.findRequirementVersion(target.getReqVersion());

		Long tcId = navigationService.findNodeIdByPath(target.getTcPath());
		TestCase tc = testcaseModificationService.findById(tcId);

		RequirementVersionCoverage coverage = instr.getCoverage();
		coverage.setVerifiedRequirementVersion(reqVersion);
		coverage.setVerifyingTestCase(tc);

			coverageDao.persist(coverage);
		}

		return train;
	}
	
}

