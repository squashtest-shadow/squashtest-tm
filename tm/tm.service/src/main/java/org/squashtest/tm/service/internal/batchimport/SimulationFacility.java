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
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.security.PermissionEvaluationService;

@Component
@Scope("prototype")
public class SimulationFacility implements Facility{
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String PERM_CREATE = "CREATE";
	private static final String PERM_WRITE = "WRITE";
	private static final String LIBRARY_CLASSNAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";
	

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private PermissionEvaluationService permissionService;
	
	
	private Model model;
	private TestCaseValidator testCaseValidator = new TestCaseValidator();
	private CustomFieldValidator cufValidator = new CustomFieldValidator(); 
	
	
	public void setModel(Model model){
		this.model = model;
		testCaseValidator.setModel(model);
		cufValidator.setModel(model);
	}
	
	
	public Model getModel(){
		return model;
	}


	/**
	 * <p>Must check the following errors : </p>
	 * <ul>
	 * 	<li>mandatory fields path, name have a value</li>
	 * 	<li>path is well formed</li>
	 * 	<li>mandatory cufs must have a value if no default is available</li>
	 * 	<li>the project actually exists</li>
	 * 	<li>can create in the project</li>
	 * 	<li>there won't be no name clash</li>
	 * 	<li>name and path are consistent</li>
	 * 	<li>format for a given field (especially lists, checkbox and dates for custom fields) are correct
	 * </ul>
	 * 
	 * <p>Must check the following warnings :</p>
	 * <ul>
	 * 	<li>which fields having a size limit 
	 * 	<li>mandatory cufs of type list will default to the default value when no value is given</li>
	 * 	<li>cufs of type list will default to the default value when the imported value is invalid</li>
	 * 	<li>when some fields have a size exceeding the size, they will be truncated to that limit</li>
	 * 	<li>mandatory cufs will default to default value if no value is imported</li>
	 * </ul>
	 */
	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {
		
		LogTrain logs = new LogTrain();
		String path = target.getPath();
		String name = testCase.getName();
		
		// 1 - basic verifications
		logs.append( testCaseValidator.basicTestCaseChecks(target, testCase, cufValues) );
		
		// 2 - custom fields (create)
		logs.append( cufValidator.checkCreateCustomFields(target, cufValues, model.getTestCaseCufs(target)) );
		
		// 3 - other checks 
		// 3-1 : names clash
		TargetStatus status = model.getStatus(target);
		if ( status.status != Existence.NOT_EXISTS){
			logs.addEntry( new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_TC_ALREADY_EXISTS, new String[]{target.getPath()}, Messages.IMPACT_TC_WITH_SUFFIX, null));
		}
		// 3-2 : permissions. note about the following 'if' : the case where the project doesn't exist (and thus has no id) is already covered in the basic checks.
		Long libid = model.getProjectStatus(target.getProject()).id;
		if ( (libid != null) && ( ! permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, PERM_CREATE, libid, LIBRARY_CLASSNAME) ) ){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[]{PERM_CREATE, target.getPath()}) );
		}
		
		// 3-3 : name and path must be consistent
		if (! Utils.arePathsAndNameConsistents(path, name)){
			logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_INCONSISTENT_PATH_AND_NAME));
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
			logs.append(createTestCase(target, testCase, cufValues));
		}
		else{
		
			// 1 - basic verifications
			logs.append( testCaseValidator.basicTestCaseChecks(target, testCase, cufValues) );
			
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
			Long libid = model.getProjectStatus(target.getProject()).id;
			if ( (libid != null) && ( ! permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, PERM_CREATE, libid, LIBRARY_CLASSNAME) ) ){
				logs.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_NO_PERMISSION, new String[]{PERM_WRITE, target.getPath()}) );
			}

		}
		
		return logs;
		
	}

	@Override
	public LogTrain deleteTestCase(long testCaseId) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestCase(TestCase testCase) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain addTestStep(TestStepTarget target, TestStep testStep,
			Map<String, String> cufValues) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain updateTestStep(long testStepId, TestStep testStepData) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestStep(long testStepId) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	@Override
	public LogTrain deleteTestStep(TestStep testStep) {
		throw new UnsupportedOperationException("not implemented yet"); 
	}

	
	
	
	
	
	
	
	// *********************** utils ********************
	

	
	
}
