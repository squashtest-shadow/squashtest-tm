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
package org.squashtest.tm.service.campaign;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * 
 * @author François Gaillard
 */
@Transactional
public interface TestSuiteTestPlanManagerService {

	/**
	 * Find a iteration using its id
	 * 
	 * @param testSuiteId
	 */
	@Transactional(readOnly = true)
	TestSuite findTestSuite(long testSuiteId);

	@Transactional(readOnly = true)
	PagedCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long testSuiteId, Paging paging);

	void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds, long suiteId);

	void detachTestPlanFromTestSuite(List<Long> testPlanIds, long suiteId);

	boolean detachTestPlanFromTestSuiteAndRemoveFromIteration(List<Long> testPlanIds, long suiteId);

}
