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

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.StepType;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;

@Component
@Scope("prototype")
public class SimulationFacility implements Facility{
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String PERM_CREATE = "CREATE";
	private static final String PERM_WRITE = "WRITE";
	private static final String PERM_DELETE = "DELETE";
	private static final String PERM_READ	=	"READ";
	private static final String LIBRARY_CLASSNAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";
	

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private PermissionEvaluationService permissionService;
	
	@Inject
	private TestCaseDao tcDao;
	
	
	private Model model;
	private EntityValidator entityValidator = new EntityValidator();
	private CustomFieldValidator cufValidator = new CustomFieldValidator(); 
	
	
	public void setModel(Model model){
		this.model = model;
		entityValidator.setModel(model);
		cufValidator.setModel(model);
	}
	
	
	public Model getModel(){
		return model;
	}



	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {
		
		LogTrain logs ;
		String path = target.getPath();
		String name = testCase.getName();
		TargetStatus status = model.getStatus(target);


		// 1 - basic verifications
		logs = entityValidator.basicTestCaseChecks(target, testCase) ;
		
		// 2 - custom fields (create)
		logs.append( cufValidator.checkCreateCustomFields(target, cufValues, model.getTestCaseCufs(target)) );
		
		// 3 - other checks 
		// 3-1 : names clash
		if ( status.status != Existence.NOT_EXISTS){
			logs.addEntry( new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_TC_ALREADY_EXISTS, new String[]{target.getPath()}, Messages.IMPACT_TC_WITH_SUFFIX, null));
		}
		
		// 3-2 : permissions. 
		LogEntry hasntPermission = checkPermissionOnProject(PERM_CREATE, target);
		if ( hasntPermission != null){
			logs.addEntry( hasntPermission );
		}
		
		// 3-3 : name and path must be consistent
		if (! Utils.arePathsAndNameConsistents(path, name)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_INCONSISTENT_PATH_AND_NAME));
		}

		/*
		 * Create TestCase : how to update the model.
		 * 
		 * If there were no errors, the target is set to be created. 
		 * If there is at least one failure the target wont be created and is considered as non existent, and further operations referring 
		 * to that test case will consequently fail (import on steps etc).   
		 * 
		 * Note that in case of path clash, we consider that the imported test case (target) shadows the one that already exists in the DB. 
		 * We update the model according to the success or failure of the create operation only and the status of an already existent test case
		 * is irrelevant.
		 * 
		 */
		if (! logs.hasCriticalErrors()){
			model.setToBeCreated(target);
		}
		else{
			model.setNotExists(target);
		}
		
		return logs;
		
	}
	
	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase,
			Map<String, String> cufValues) {

		LogTrain logs = new LogTrain();
		String path = target.getPath();
		String name = testCase.getName();
		
		TargetStatus status = model.getStatus(target);
		
		// if the test case doesn't exist
		if (status.status == Existence.NOT_EXISTS){
			logs.addEntry( new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_TC_NOT_FOUND, Messages.IMPACT_TC_CREATED));
			logs.append( createTestCase(target, testCase, cufValues) );
		}
		else{
		
			// 1 - basic verifications
			logs.append( entityValidator.basicTestCaseChecks(target, testCase) );
			
			// 2 - custom fields (create)
			logs.append( cufValidator.checkUpdateCustomFields(target, cufValues, model.getTestCaseCufs(target)) );
			
			// 3 - other checks 
			// 3-1 : check if the test case is renamed and would induce a potential name clash. arePathsAndNameConsistent() will tell us if the test case is renamed
			if (! Utils.arePathsAndNameConsistents(path, name)){	
				String newPath = Utils.rename(path, name);
				TestCaseTarget newTarget = new TestCaseTarget(newPath);
				TargetStatus newStatus = model.getStatus(newTarget);
				if (newStatus.status != Existence.NOT_EXISTS){
					logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_CANT_RENAME, new String[]{path, newPath} ));
				}
			}
			// 3-2 : permissions. note about the following 'if' : the case where the project doesn't exist (and thus has no id) is already covered in the basic checks.
			LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target);
			if ( hasntPermission != null){
				logs.addEntry( hasntPermission );
			}

		}

		/*
		 * In case of an update, we don't need to change the model regardless of the success or failure of the operation.
		 */
		
		return logs;
		
	}


	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {
		
		LogTrain logs = new LogTrain();
		
		TargetStatus status = model.getStatus(target);
		
		// 1 - does the target exist
		if (status.status == Existence.NOT_EXISTS){
			logs.addEntry (new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_TC_NOT_FOUND));
		}
		
		// 2 - can the user actually do it ?
		LogEntry hasntPermission = checkPermissionOnProject(PERM_DELETE, target);
		if ( hasntPermission != null){
			logs.addEntry( hasntPermission );
		}
		
		// 3 - is the test case called by another test case ?
		if (model.isCalled(target)){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_REMOVE_CALLED_TC));
		}
		
		// if no fatal error, update the model
		if (! logs.hasCriticalErrors()){
			model.setToBeDeleted(target);
		}
		
		return logs;
	}

	
	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {
		
		LogTrain logs;
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target, testStep);
		
		// 2 - custom fields (create)
		logs.append( cufValidator.checkCreateCustomFields(target, cufValues, model.getTestStepCufs(target)) );
				
		// 3 - the user must be approved
		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase());
		if ( hasntPermission != null){
			logs.addEntry( hasntPermission );
		}
		
		// 4 - check the index
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.WARNING, Messages.IMPACT_STEP_CREATED_LAST);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		
		// if no fatal errors, update the model
		if (! logs.hasCriticalErrors()){
			model.addActionStep(target);
		}

		return logs;

	}
	
	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {
		
		LogTrain logs;		
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target, testStep);
		
		// 2 - call step specific checks
		logs.append( entityValidator.validateCallStep(target, testStep, calledTestCase));
		
		// 3 - cufs : call steps have no cufs -> skip
		
		// 4.1 - the user must be approved on the source test case
		LogEntry hasntOwnerPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase());
		if ( hasntOwnerPermission != null){
			logs.addEntry( hasntOwnerPermission );
		}	
		
		// 4.2 - the user must be approved on the target test case
		LogEntry hasntCallPermission = checkPermissionOnProject(PERM_READ, calledTestCase);
		if ( hasntCallPermission != null){
			logs.addEntry( hasntCallPermission );
		}	
		
		// 5 - check the index
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.WARNING, Messages.IMPACT_STEP_CREATED_LAST);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		// 6 - no call step cycles allowed
		if (model.wouldCreateCycle(target, calledTestCase)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CYCLIC_STEP_CALLS, new Object[]{ target.getTestCase().getPath(), calledTestCase.getPath() } ));
		}
		
		// update the model if no fatal flaws were detected
		if (! logs.hasCriticalErrors()){
			model.addCallStep(target, calledTestCase);
		}
		
		return logs;
	}


	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) {
		
		LogTrain logs ;
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);
		
		// 2 - custom fields (create)
		logs.append( cufValidator.checkUpdateCustomFields(target, cufValues, model.getTestStepCufs(target)) );
		
		// 3 - the user must be approved
		LogEntry hasntPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase());
		if ( hasntPermission != null){
			logs.addEntry( hasntPermission );
		}
		
		// 4 - check the index
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.FAILURE, null);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		// 5 - the step must be actually an action step
		StepType type = model.getType(target);
		if (type != StepType.ACTION){
			logs.addEntry (new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NOT_AN_ACTIONSTEP) );
		}
		
		// no need to update the model 
		
		return logs;
		
	}


	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {
		
		LogTrain logs;		
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);
		
		// 2 - call step specific checks
		logs.append( entityValidator.validateCallStep(target, testStep, calledTestCase));
		
		// 3 - cufs : call steps have no cufs -> skip
		
		// 4.1 - the user must be approved on the source test case
		LogEntry hasntOwnerPermission = checkPermissionOnProject(PERM_WRITE, target.getTestCase());
		if ( hasntOwnerPermission != null){
			logs.addEntry( hasntOwnerPermission );
		}	
		
		// 4.2 - the user must be approved on the target test case
		LogEntry hasntCallPermission = checkPermissionOnProject(PERM_READ, calledTestCase);
		if ( hasntCallPermission != null){
			logs.addEntry( hasntCallPermission );
		}	
		
		// 5 - check the index
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.FAILURE, null);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		// 6 - check that this is a call step
		StepType type = model.getType(target);
		if (type != StepType.CALL){
			logs.addEntry (new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NOT_A_CALLSTEP) );
		}		
		
		// 7 - no call step cycles allowed
		if (model.wouldCreateCycle(target, calledTestCase)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CYCLIC_STEP_CALLS, new Object[]{ target.getTestCase().getPath(), calledTestCase.getPath() } ));
		}
		
		// if all is ok, update the target of this call step then return
		if (! logs.hasCriticalErrors()){
			model.updateCallStepTarget(target, calledTestCase);
		}
		
		return logs;
	}


	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {
		
		LogTrain logs ;
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target);
		
		// 2 - can the user do it
		LogEntry hasntPermission = checkPermissionOnProject(PERM_DELETE, target.getTestCase());
		if ( hasntPermission != null){
			logs.addEntry( hasntPermission );
		}		
		
		// 3 - can that step be identified precisely ?
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.FAILURE, null);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		// if all went well, we can remove that step from the model
		if (! logs.hasCriticalErrors()){
			model.remove(target);
		}
		
		return logs;
	}
	
	
	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {
		
		LogTrain logs;		

		// 1 - basic checks
		logs = entityValidator.basicParameterChecks(target);

		// 2 - does it already exists ?
		if (model.doesParameterExists(target)){
			logs.addEntry( new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_PARAMETER_ALREADY_EXISTS, Messages.IMPACT_PARAM_UPDATED) );			
		}
		
		// 3 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner());
		if ( hasNoPermission != null){
			logs.addEntry( hasNoPermission );
		}	
		
		// if no problems, add it to the model (the model is double-insertion proof)
		if (! logs.hasCriticalErrors()){
			model.addParameter(target);
		}
		
		return logs;
	}


	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {
		
		LogTrain logs;		

		// 1 - basic checks
		logs = entityValidator.basicParameterChecks(target);

		// 2 - does it exists ?
		if (! model.doesParameterExists(target)){
			logs.addEntry( new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_PARAMETER_NOT_FOUND, Messages.IMPACT_PARAM_CREATED) );	
		}
		
		// 3 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner());
		if ( hasNoPermission != null){
			logs.addEntry( hasNoPermission );
		}	
		
		// no need to update the model because because the existence of that parameter is unaffected
		
		return logs;
		
	}


	@Override
	public LogTrain deleteParameter(ParameterTarget target) {
		
		LogTrain logs = new LogTrain(); 		

		// 1 - does it exists ?
		if (! model.doesParameterExists(target)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_PARAMETER_NOT_FOUND) );
		}
		
		// 2 - is the user approved ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, target.getOwner());
		if ( hasNoPermission != null){
			logs.addEntry( hasNoPermission );
		}
		
		// if all is ok let's proceed
		if (! logs.hasCriticalErrors()){
			model.removeParameter(target);
		}
		
		return logs;
	}


	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset,
			ParameterTarget param, String value) {
		
		LogTrain logs;
		boolean createsDSonthefly=false;
		
		// 1 - is the dataset correctly identifed ? 
		logs = entityValidator.basicDatasetCheck(dataset);
		
		// 2 - is the parameter correctly identified ?
		logs.append( entityValidator.basicParameterChecks(param) );
		
		// 3 - does the dataset exists ?
		if (! model.doesDatasetExists(dataset)){
			logs.addEntry( new LogEntry(dataset, ImportStatus.WARNING, Messages.ERROR_DATASET_NOT_FOUND, Messages.IMPACT_DATASET_CREATED) );
		}
		
		// 4 - is such parameter available for this dataset ?
		if (! model.isParamInDataset(param, dataset)){
			logs.addEntry (new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_DATASET_PARAMETER_MISMATCH));
		}
		
		// 5 - is the user allowed to do so ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, dataset.getTestCase());
		if ( hasNoPermission != null){
			logs.addEntry( hasNoPermission );
		}
		
		if (!logs.hasCriticalErrors()){
			if (createsDSonthefly){
				model.addDataset(dataset);
			}
		}
		
		return logs;
	}


	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {
		
		LogTrain logs;
		
		// 1 - is the dataset correctly identified ?
		logs = entityValidator.basicDatasetCheck(dataset);
		
		// 2 - does the dataset exists ? 
		if (! model.doesDatasetExists(dataset)){
			logs.addEntry( new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_DATASET_NOT_FOUND) );
		}
		
		// 3 - has the user the required privilege ?
		LogEntry hasNoPermission = checkPermissionOnProject(PERM_WRITE, dataset.getTestCase());
		if ( hasNoPermission != null){
			logs.addEntry( hasNoPermission );
		}
		
		// 4 - if ok, update the model
		if (! logs.hasCriticalErrors()){
			model.removeDataset(dataset);
		}

		return logs;
	
	}

	
	// **************************** private utilities *****************************************
	
	
	// checks permission on a project that may exist or not.
	// the case where the project doesn't exist (and thus has no id) is already covered in the basic checks.
	private LogEntry checkPermissionOnProject(String permission, TestCaseTarget target){
		
		LogEntry entry = null;
		
		Long libid = model.getProjectStatus(target.getProject()).id;
		if ( (libid != null) && ( ! permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, permission, libid, LIBRARY_CLASSNAME) ) ){
			entry = new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[]{permission, target.getPath()}) ;
		}
		
		return entry;
		
	}
	
	private LogEntry checkStepIndex(TestStepTarget target, ImportStatus importStatus, String optionalImpact ){
		Integer index = target.getIndex();
		LogEntry entry = null; 
		
		if (index == null){
			 entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_EMPTY, optionalImpact);
		}
		else if (index < 0){
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_NEGATIVE, optionalImpact);		
		}
		else if (! model.stepExists(target)){
			entry = new LogEntry(target, importStatus, Messages.ERROR_STEPINDEX_OVERFLOW, optionalImpact);		
		}
		
		return entry;
	}



	
}
