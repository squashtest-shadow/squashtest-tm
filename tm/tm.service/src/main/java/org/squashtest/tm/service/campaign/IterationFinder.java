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

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testcase.TestCase;

public interface IterationFinder {
		
	List<Iteration> findIterationsByCampaignId(long campaignId);

	Iteration findById(long iterationId);

	List<Execution> findAllExecutions(long iterationId);

	List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId);

	List<TestCase> findPlannedTestCases(long iterationId);
	
	List<Iteration> findIterationContainingTestCase(long testCaseId);
	
	TestPlanStatistics getIterationStatistics(long iterationId);
	
	/**
	 * Returns an iteration filtered for a specific user. It returns an iteration
	 * with a test plan containing only the items that are assigned to that user or
	 * have been executed by that user.
	 * @param iterationId
	 * @return the test plan of given iteration filtered by the current user
	 */
	PagedCollectionHolder<List<IterationTestPlanItem>> findAssignedTestPlan(long iterationId, Paging sorting);
	
}
