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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;

public interface CustomTestSuiteDao extends EntityDao<TestSuite>{

	List<TestSuite> findAllByIterationId(long iterationId);

	/**
	 * <p>
	 * return a list of ordered iteration_test_plan_items that are linked to a test case or have an execution<br>
	 * making it the launchable test plan of the test suite
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return
	 */
	List<IterationTestPlanItem> findLaunchableTestPlan(long testSuiteId);

	TestPlanStatistics getTestSuiteStatistics(long testSuitId);

	List<IterationTestPlanItem> findTestPlanPartition(long testSuiteId,
			List<Long> testPlanItemIds);
	
	List<Execution> findAllExecutionByTestSuite(long testSuiteId);
	
	List<IterationTestPlanItem> findAllTestPlanItemsPaged(long testSuiteId, Paging paging);
}