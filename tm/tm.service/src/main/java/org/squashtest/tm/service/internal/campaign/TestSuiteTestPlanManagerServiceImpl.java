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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.user.UserAccountService;

@Service("squashtest.tm.service.TestSuiteTestPlanManagerService")
@Transactional
public class TestSuiteTestPlanManagerServiceImpl implements TestSuiteTestPlanManagerService {

	private static final String HAS_LINK_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') ";
	private static final String HAS_LINK_PERMISSION_OBJECT = "hasPermission(#testSuite, 'LINK') ";

	@Inject
	private IterationTestPlanManagerService delegateIterationTestPlanManagerService;

	@Inject
	private TestSuiteDao testSuiteDao;
	

	@Inject
	private UserAccountService userService;

	@Inject
	private IterationTestPlanDao itemTestPlanDao;
	
	@Inject
	private ProjectsPermissionFinder projectsPermissionFinder;
	
	
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public TestSuite findTestSuite(long testSuiteId) {
		return testSuiteDao.findById(testSuiteId);
	}
	

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void bindTestPlan(long suiteId, List<Long> itemTestPlanIds) {
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
		testSuite.unBindTestPlan(itemTestPlans);
	}
	


	@Override
	public PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findAssignedTestPlan(long iterationId, PagingAndMultiSorting sorting) {

		String userLogin = userService.findCurrentUser().getLogin();
		TestSuite testSuite = testSuiteDao.findById(iterationId);
		Long projectId = testSuite.getProject().getId();
		
		//configure the filter, in case the test plan must be restricted to what the user can see.
		Filtering filtering = DefaultFiltering.NO_FILTERING;
		if (projectsPermissionFinder.isInPermissionGroup(userLogin, projectId, "squashtest.acl.group.tm.TestRunner")) {
			filtering = new DefaultFiltering("User.login", userLogin);
		}

		List<IndexedIterationTestPlanItem> indexedItems = testSuiteDao.findIndexedTestPlan(iterationId, sorting, filtering);
		long testPlanSize = testSuiteDao.countTestPlans(iterationId, filtering);

		return new PagingBackedPagedCollectionHolder<List<IndexedIterationTestPlanItem>>(sorting, testPlanSize, indexedItems);
	}


	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long suiteId, int newIndex, List<Long> itemIds) {

		TestSuite suite = testSuiteDao.findById(suiteId);

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlanPartition(suiteId, itemIds);

		suite.reorderTestPlan(newIndex, items);
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'WRITE') "
			+ OR_HAS_ROLE_ADMIN)	
	public void reorderTestPlan(long suiteId, MultiSorting newSorting) {
		
		Paging noPaging = Pagings.NO_PAGING;
		PagingAndMultiSorting sorting = new DelegatePagingAndMultiSorting(noPaging, newSorting);
		Filtering filtering = DefaultFiltering.NO_FILTERING;
		
		List<IterationTestPlanItem> items = testSuiteDao.findTestPlan(suiteId, sorting, filtering);
		
		TestSuite testSuite = testSuiteDao.findById(suiteId);
		
		testSuite.getTestPlan().clear();
		testSuite.getTestPlan().addAll(items);
	}


	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds, long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);

		Iteration iteration = testSuite.getIteration();

		List<IterationTestPlanItem> listTestPlanItemsToAffectToTestSuite = delegateIterationTestPlanManagerService
				.addTestPlanItemsToIteration(testCaseIds, iteration);

		bindTestPlanObj(testSuite, listTestPlanItemsToAffectToTestSuite);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void detachTestPlanFromTestSuite(List<Long> testPlanIds, long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);
		List<IterationTestPlanItem> listTestPlanItems = new ArrayList<IterationTestPlanItem>();

		for (long testPlanId : testPlanIds) {
			IterationTestPlanItem iterTestPlanItem = itemTestPlanDao.findById(testPlanId);
			listTestPlanItems.add(iterTestPlanItem);
		}

		unbindTestPlanObj(testSuite, listTestPlanItems);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public boolean detachTestPlanFromTestSuiteAndRemoveFromIteration(List<Long> testPlanIds, long suiteId) {
		TestSuite testSuite = testSuiteDao.findById(suiteId);
		List<IterationTestPlanItem> listTestPlanItems = new ArrayList<IterationTestPlanItem>();

		for (long testPlanId : testPlanIds) {
			IterationTestPlanItem iterTestPlanItem = itemTestPlanDao.findById(testPlanId);
			listTestPlanItems.add(iterTestPlanItem);
		}

		unbindTestPlanObj(testSuite, listTestPlanItems);

		Iteration iteration = testSuite.getIteration();

		return delegateIterationTestPlanManagerService.removeTestPlansFromIterationObj(testPlanIds, iteration);
	}
	

}