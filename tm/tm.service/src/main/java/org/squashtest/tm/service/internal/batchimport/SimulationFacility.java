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
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
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
	public LogTrain addActionStep(TestStepTarget target, TestStep testStep, Map<String, String> cufValues) {
		
		LogTrain logs ;
		
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
	public LogTrain addCallStep(TestStepTarget target, TestStep testStep, TestCaseTarget calledTestCase) {
		
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
	public LogTrain updateActionStep(TestStepTarget target, TestStep testStep, Map<String, String> cufValues) {
		
		LogTrain logs ;
		
		// 1 - basic checks
		logs = entityValidator.basicTestStepChecks(target, testStep);
		
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
		
		// no need to update the model 
		
		return logs;
		
	}


	@Override
	public LogTrain updateCallStep(TestStepTarget target, TestStep testStep, TestCaseTarget calledTestCase) {
		
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
		LogEntry indexCheckLog = checkStepIndex(target, ImportStatus.FAILURE, null);
		if (indexCheckLog != null){
			logs.addEntry(indexCheckLog);
		}
		
		// 6 - no call step cycles allowed
		if (model.wouldCreateCycle(target, calledTestCase)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_CYCLIC_STEP_CALLS, new Object[]{ target.getTestCase().getPath(), calledTestCase.getPath() } ));
		}
		
		
	}


	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {
		throw new UnsupportedOperationException("not implemented yet");
	}
	
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
