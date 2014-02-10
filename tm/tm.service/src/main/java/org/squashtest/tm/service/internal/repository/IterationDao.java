/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;

public interface IterationDao extends EntityDao<Iteration> {

	List<Iteration> findAllByCampaignId(long campaignId);

	void removeFromCampaign(Iteration iteration);

	List<Execution> findOrderedExecutionsByIterationId(long iterationId);

	List<Execution> findOrderedExecutionsByIterationAndTestCase(long iterationId, long testCaseId);

	List<Execution> findOrderedExecutionsByIterationAndTestPlan(long iterationId, long testPlanId);

	Iteration findAndInit(long iterationId);

	List<Iteration> findAllIterationContainingTestCase(long testCaseId);
	
	
	List<IterationTestPlanItem> findTestPlan(long iterationId, PagingAndMultiSorting sorting, Filtering filter, ColumnFiltering columnFiltering);
	
	
	/**
	 * Returns the paged list of [index, IterationTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 * 
	 * @param iterationId
	 * @param sorting
	 * @param filtering
	 * @return
	 */
	List<IndexedIterationTestPlanItem> findIndexedTestPlan(long iterationId, PagingAndMultiSorting sorting, Filtering filtering, ColumnFiltering columnFiltering);
	
	/**
	 * Returns the paged list of [index, IterationTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 * 
	 * @param iterationId
	 * @param sorting
	 * @param filtering
	 * @return
	 */
	List<IndexedIterationTestPlanItem> findIndexedTestPlan(long iterationId, PagingAndSorting sorting, Filtering filter, ColumnFiltering columnFiltering);
		
	long countTestPlans(Long iterationId, Filtering filtering, ColumnFiltering columnFiltering);

	long countTestPlans(Long iterationId, Filtering filtering);
	

	List<TestSuite> findAllTestSuites(long iterationId);

	void persistIterationAndTestPlan(Iteration iteration);

	List<Execution> findAllExecutionByIterationId(long iterationId);

	/**
	 * 
	 * @param iterationId
	 *            the id of the concerned {@link Iteration}
	 * @return the {@link TestPlanStatistics} computed out of each iteration's test-plan-items
	 */
	TestPlanStatistics getIterationStatistics(long iterationId);

	long countRunningOrDoneExecutions(long iterationId);
}
