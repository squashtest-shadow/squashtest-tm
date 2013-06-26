/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import org.apache.commons.lang.StringUtils;
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
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomTestSuiteModificationService")
public class CustomTestSuiteModificationServiceImpl implements CustomTestSuiteModificationService {
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";
	private static final String HAS_WRITE_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') ";
	private static final String HAS_LINK_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') ";
	private static final String HAS_LINK_PERMISSION_OBJECT = "hasPermission(#testSuite, 'LINK') ";
	private static final String HAS_EXECUTE_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE') ";
	private static final String HAS_READ_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite','READ') ";
	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private AutomatedSuiteDao autoSuiteDao;

	@Inject
	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ProjectsPermissionFinder projectsPermissionFinder;

	@Inject
	private UserAccountService userService;

	@Inject
	private InsecureTestAutomationManagementService testAutomationService;
	
	@Inject
	private IterationTestPlanManager iterationTestPlanManager;

	public void setUserAccountService(UserAccountService service) {
		this.userService = service;
	}

	@Override
	@PreAuthorize(HAS_WRITE_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void rename(long suiteId, String newName) throws DuplicateNameException {
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.rename(newName);
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void bindTestPlan(long suiteId, List<Long> itemTestPlanIds) {
		// that implementation relies on how the TestSuite will do the job (regarding the checks on whether the itps
		// belong to the
		// same iteration of not
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.bindTestPlanItemsById(itemTestPlanIds);
	}

	@Override()
	public void bindTestPlanToMultipleSuites(List<Long> suiteIds, List<Long> itemTestPlanIds) {

		for (Long id : suiteIds) {
			bindTestPlan(id, itemTestPlanIds);
		}
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_OBJECT + OR_HAS_ROLE_ADMIN)
	public void bindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		// the test plans have already been associated to the Iteration
		testSuite.bindTestPlanItems(itemTestPlans);
	}

	@Override()
	public void bindTestPlanToMultipleSuitesObj(List<TestSuite> testSuites, List<IterationTestPlanItem> itemTestPlans) {

		for (TestSuite suite : testSuites) {
			bindTestPlanObj(suite, itemTestPlans);
		}
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_OBJECT + OR_HAS_ROLE_ADMIN)
	public void unbindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		// the test plans have already been associated to the Iteration
		testSuite.unBindTestPlan(itemTestPlans);
	}

	@Override
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public TestSuite findById(long suiteId) {
		return testSuiteDao.findById(suiteId);
	}

	@Override
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestSuiteTestPlan(long suiteId, Paging paging) {

		List<IterationTestPlanItem> testPlan = testSuiteDao.findAllTestPlanItemsPaged(suiteId, paging);

		Long projectId = testSuiteDao.findById(suiteId).getProject().getId();
		String userLogin = userService.findCurrentUser().getLogin();

		List<IterationTestPlanItem> filteredTestPlan = this.filterTestSuiteByUser(testPlan, userLogin, projectId);

		long count = testSuiteDao.countTestPlanItems(suiteId);

		return new PagingBackedPagedCollectionHolder<List<IterationTestPlanItem>>(paging, count, filteredTestPlan);
	}

	@Override
	@PreAuthorize(HAS_READ_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public TestPlanStatistics findTestSuiteStatistics(long suiteId) {
		return testSuiteDao.getTestSuiteStatistics(suiteId);
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long suiteId, int newIndex, List<Long> itemIds) {

		TestSuite suite = testSuiteDao.findById(suiteId);

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlanPartition(suiteId, itemIds);

		suite.reorderTestPlan(newIndex, items);
	}

	@Override
	@PreAuthorize(HAS_EXECUTE_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAndStartAutomatedSuite(long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);
		
		List<IterationTestPlanItem> items = testSuite.getTestPlan();

		return iterationTestPlanManager.createAndStartAutomatedSuite(items);
	}

	@Override
	@PreAuthorize(HAS_EXECUTE_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAndStartAutomatedSuite(long suiteId, List<Long> testPlanIds) {
		return iterationTestPlanManager.createAndStartAutomatedSuiteByITPIsIds(testPlanIds);
	}

	

	public List<IterationTestPlanItem> filterTestSuiteByUser(List<IterationTestPlanItem> testPlanItems,
			String userLogin, Long projectId) {

		List<IterationTestPlanItem> testPlanItemsToReturn = new ArrayList<IterationTestPlanItem>();

		if (projectsPermissionFinder.isInPermissionGroup(userLogin, projectId, "squashtest.acl.group.tm.TestRunner")) {

			for (IterationTestPlanItem testPlanItem : testPlanItems) {

				if (hasToBeReturned(testPlanItem, userLogin)) {
					testPlanItemsToReturn.add(testPlanItem);
				}
			}
		} else {
			testPlanItemsToReturn.addAll(testPlanItems);
		}

		return testPlanItemsToReturn;
	}

	// TODO this is copypasta from CustomIterationModificationServiceImpl
	private boolean hasToBeReturned(IterationTestPlanItem testPlanItem, String userLogin) {

		boolean hasToBeReturned = false;

		User testPlanUser = testPlanItem.getUser();

		if (testPlanUser != null && StringUtils.equals(testPlanUser.getLogin(), userLogin)) {
			hasToBeReturned = true;
		}

		return hasToBeReturned;
	}
}
