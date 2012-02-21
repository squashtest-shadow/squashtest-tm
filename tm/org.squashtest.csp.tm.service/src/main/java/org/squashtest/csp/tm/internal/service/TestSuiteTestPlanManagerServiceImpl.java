/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager;
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;
import org.squashtest.csp.tm.service.TestSuiteModificationService;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;

@Service("squashtest.tm.service.TestSuiteTestPlanManagerService")
@Transactional
public class TestSuiteTestPlanManagerServiceImpl implements
		TestSuiteTestPlanManagerService {

	@Inject
	private TestSuiteModificationService delegateTestSuiteModificationService;

	@Inject
	private IterationTestPlanManagerService delegateIterationTestPlanManagerService;

	@Inject
	private IterationTestPlanManager testPlanManager;

	@Inject
	private TestSuiteDao testSuiteDao;
	
	@Inject
	private ItemTestPlanDao itemTestPlanDao;

	// FIXME : security
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public TestSuite findTestSuite(long testSuiteId) {
		return testSuiteDao.findById(testSuiteId);
	}

	// FIXME : security
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestPlan(
			long suiteId, Paging paging) {
		return delegateTestSuiteModificationService.findTestSuiteTestPlan(
				suiteId, paging);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds,
			long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);

		Iteration iteration = testSuite.getIteration();

		List<IterationTestPlanItem> listTestPlanItemsToAffectToTestSuite = delegateIterationTestPlanManagerService
				.addTestPlanItemsToIteration(testCaseIds, iteration);

		delegateTestSuiteModificationService.bindTestPlanObj(testSuite,
				listTestPlanItemsToAffectToTestSuite);
	}

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Execution startNewExecution(long testSuiteId) {
		TestSuite suite = testSuiteDao.findById(testSuiteId);
		IterationTestPlanItem firstItem = suite.getFirstTestPlanItem();

		return testPlanManager.addExecution(firstItem);
	}
	

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void detachTestPlanFromTestSuite(List<Long> testPlanIds, long suiteId){
		
		TestSuite testSuite = testSuiteDao.findById(suiteId);
		List<IterationTestPlanItem> listTestPlanItems = new ArrayList<IterationTestPlanItem>();
		
		for (long testPlanId : testPlanIds) {
			IterationTestPlanItem iterTestPlanItem = itemTestPlanDao.findById(testPlanId);
			listTestPlanItems.add(iterTestPlanItem);
		}

		delegateTestSuiteModificationService.unbindTestPlanObj(testSuite, listTestPlanItems);
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public boolean detachTestPlanFromTestSuiteAndRemoveFromIteration(List<Long> testPlanIds, long suiteId){
		TestSuite testSuite = testSuiteDao.findById(suiteId);
		List<IterationTestPlanItem> listTestPlanItems = new ArrayList<IterationTestPlanItem>();
		
		for (long testPlanId : testPlanIds) {
			IterationTestPlanItem iterTestPlanItem = itemTestPlanDao.findById(testPlanId);
			listTestPlanItems.add(iterTestPlanItem);
		}
		
		delegateTestSuiteModificationService.unbindTestPlanObj(testSuite, listTestPlanItems);
		
		Iteration iteration = testSuite.getIteration();
		
		return delegateIterationTestPlanManagerService.removeTestPlansFromIterationObj(testPlanIds, iteration);
	}
}