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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * @author François Gaillard
 */
public interface TestSuiteTestPlanManagerService {

	/**
	 * Find a iteration using its id
	 * @param testSuiteId
	 */
	TestSuite findTestSuite(long testSuiteId);
	
	/**
	 * Returns a collection of {@link TestCaseLibrary}, the test cases of
	 * which may be added to the campaign
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();


	PagedCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long testSuiteId, Paging paging);

}
