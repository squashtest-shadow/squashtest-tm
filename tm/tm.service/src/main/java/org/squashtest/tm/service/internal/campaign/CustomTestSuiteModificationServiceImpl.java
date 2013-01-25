/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.service.internal.campaign;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.tm.service.internal.testcase.TestCaseCyclicCallChecker;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomTestSuiteModificationService")
public class CustomTestSuiteModificationServiceImpl implements CustomTestSuiteModificationService {

	@Inject
	private TestSuiteDao testSuiteDao;
	
	@Inject
	private AutomatedSuiteDao autoSuiteDao;
	
	@Inject
	private InsecureTestAutomationManagementService automationService;
	
	@Inject
	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;

	@Inject
	private ExecutionDao executionDao;
	
	@Inject
	private ProjectsPermissionFinder projectsPermissionFinder;

	@Inject
	private UserAccountService userService;
	
	
	public void setUserAccountService(UserAccountService service){
		this.userService=service;
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long suiteId, String newName) throws DuplicateNameException {
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.rename(newName);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') or hasRole('ROLE_ADMIN')")
	public void bindTestPlan(long suiteId, List<Long> itemTestPlanIds) {
		// that implementation relies on how the TestSuite will do the job (regarding the checks on whether the itps
		// belong to the
		// same iteration of not
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.bindTestPlanItemsById(itemTestPlanIds);
	}

	@Override
	@PreAuthorize("hasPermission(#testSuite, 'LINK') or hasRole('ROLE_ADMIN')")
	public void bindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		// the test plans have already been associated to the Iteration
		testSuite.bindTestPlanItems(itemTestPlans);
	}

	@Override
	@PreAuthorize("hasPermission(#testSuite, 'LINK') or hasRole('ROLE_ADMIN')")
	public void unbindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		// the test plans have already been associated to the Iteration
		testSuite.unBindTestPlan(itemTestPlans);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public TestSuite findById(long suiteId) {
		return testSuiteDao.findById(suiteId);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestSuiteTestPlan(long suiteId, Paging paging) {

		List<IterationTestPlanItem> testPlan = testSuiteDao.findAllTestPlanItemsPaged(suiteId, paging);

		Long projectId = testSuiteDao.findById(suiteId).getProject().getId();
		String userLogin = userService.findCurrentUser().getLogin();
				
		List<IterationTestPlanItem> filteredTestPlan = this.filterTestSuiteByUser(testPlan, userLogin, projectId);
		
		long count = testSuiteDao.countTestPlanItems(suiteId);

		return new PagingBackedPagedCollectionHolder<List<IterationTestPlanItem>>(paging, count, filteredTestPlan);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public TestPlanStatistics findTestSuiteStatistics(long suiteId) {
		return testSuiteDao.getTestSuiteStatistics(suiteId);
	}

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite','LINK') or hasRole('ROLE_ADMIN')")
	public void changeTestPlanPosition(Long testSuiteId, int newIndex, List<Long> itemIds) {

		TestSuite suite = testSuiteDao.findById(testSuiteId);

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlanPartition(testSuiteId, itemIds);

		suite.reorderTestPlan(newIndex, items);
	}

	@Override	
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public AutomatedSuite createAutomatedSuite(long suiteId) {
				
		TestSuite testSuite = testSuiteDao.findById(suiteId);
		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();
		
		List<IterationTestPlanItem> items = testSuite.getTestPlan();
		
		for (IterationTestPlanItem item : items){
			if (item.isAutomated()){
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}
		}
		
		return newSuite;
	}
	
	//TODO merge code with IterationModificationService.addAutomatedExecution
	private Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {
		
		Execution execution = item.createAutomatedExecution(testCaseCyclicCallChecker);
		
		executionDao.persist(execution);
		item.addExecution(execution);
		
		return execution;
		
	}

	public List<IterationTestPlanItem> filterTestSuiteByUser(List<IterationTestPlanItem> testPlanItems, String userLogin, Long projectId) {

		List<IterationTestPlanItem> testPlanItemsToReturn = new ArrayList<IterationTestPlanItem>();

		if(projectsPermissionFinder.isInPermissionGroup(userLogin, projectId, "squashtest.acl.group.tm.TestRunner")){
			
			for(IterationTestPlanItem testPlanItem: testPlanItems){
					
				if(hasToBeReturned(testPlanItem,userLogin)){
					testPlanItemsToReturn.add(testPlanItem);
				}
			}
		} else {
			testPlanItemsToReturn.addAll(testPlanItems);
		}

		return testPlanItemsToReturn;
	}

	private boolean hasToBeReturned(IterationTestPlanItem testPlanItem, String userLogin){
		
		boolean hasToBeReturned = false;
		
		if(testPlanItem.getUser() != null && (testPlanItem.getUser().getLogin()==userLogin)){
		
			hasToBeReturned = true;
		}
		
		return hasToBeReturned;
	}	
}
