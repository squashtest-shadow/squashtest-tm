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
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.internal.repository.ItemTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;

@Service("squashtest.tm.service.TestSuiteTestPlanManagerService")
@Transactional
public class TestSuiteTestPlanManagerServiceImpl implements TestSuiteTestPlanManagerService {

	@Inject
	private TestSuiteModificationService delegateTestSuiteModificationService;

	@Inject
	private IterationTestPlanManagerService delegateIterationTestPlanManagerService;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private ItemTestPlanDao itemTestPlanDao;
	
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public TestSuite findTestSuite(long testSuiteId) {
		return testSuiteDao.findById(testSuiteId);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long suiteId, Paging paging) {
		return delegateTestSuiteModificationService.findTestSuiteTestPlan(suiteId, paging);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds, long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);

		Iteration iteration = testSuite.getIteration();

		List<IterationTestPlanItem> listTestPlanItemsToAffectToTestSuite = delegateIterationTestPlanManagerService
				.addTestPlanItemsToIteration(testCaseIds, iteration);

		delegateTestSuiteModificationService.bindTestPlanObj(testSuite, listTestPlanItemsToAffectToTestSuite);
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

		delegateTestSuiteModificationService.unbindTestPlanObj(testSuite, listTestPlanItems);
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

		delegateTestSuiteModificationService.unbindTestPlanObj(testSuite, listTestPlanItems);

		Iteration iteration = testSuite.getIteration();

		return delegateIterationTestPlanManagerService.removeTestPlansFromIterationObj(testPlanIds, iteration);
	}

}