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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.campaign.TestSuiteStatistics;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.CustomTestSuiteModificationService;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

@Service("CustomTestSuiteModificationService")
public class CustomTestSuiteModificationServiceImpl implements CustomTestSuiteModificationService {

	@Inject
	private TestSuiteDao testSuiteDao;

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long suiteId, String newName) throws DuplicateNameException {
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.rename(newName);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite', 'LINK') or hasRole('ROLE_ADMIN')")
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
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public TestSuite findById(long suiteId) {
		return testSuiteDao.findById(suiteId);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestSuiteTestPlan(long suiteId, Paging paging) {

		List<IterationTestPlanItem> testPlan = testSuiteDao.findAllTestPlanItemsPaged(suiteId, paging);

		long count = testSuiteDao.countTestPlanItems(suiteId);

		return new PagingBackedPagedCollectionHolder<List<IterationTestPlanItem>>(paging, count, testPlan);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public TestSuiteStatistics findTestSuiteStatistics(long suiteId) {
		return testSuiteDao.getTestSuiteStatistics(suiteId);
	}

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','LINK') or hasRole('ROLE_ADMIN')")
	public void changeTestPlanPosition(Long testSuiteId, int newIndex, List<Long> itemIds) {

		TestSuite suite = testSuiteDao.findById(testSuiteId);

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlanPartition(testSuiteId, itemIds);

		suite.reorderTestPlan(newIndex, items);
	}

}
