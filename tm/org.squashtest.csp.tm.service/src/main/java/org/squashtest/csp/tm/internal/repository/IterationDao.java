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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface IterationDao extends EntityDao<Iteration> {

	/**
	 * Returns the initialized list of the iterations of a campaign. Returned list order is the same as
	 * Campaign.iterations
	 * 
	 * @param campaignId
	 * @return
	 */
	List<Iteration> findAllInitializedByCampaignId(long campaignId);

	void removeFromCampaign(Iteration iteration);

	List<Execution> findOrderedExecutionsByIterationId(long iterationId);

	List<Execution> findOrderedExecutionsByIterationAndTestCase(long iterationId, long testCaseId);

	List<Execution> findOrderedExecutionsByIterationAndTestPlan(long iterationId, long testPlanId);

	Iteration findAndInit(long iterationId);

	List<IterationTestPlanItem> findTestPlanFiltered(long iterationId, CollectionSorting filter);

	long countTestPlans(Long iterationId);

	List<Iteration> findAllByIdList(List<Long> iterationIds);

	List<TestSuite> findAllTestSuites(long iterationId);

	void persistIterationAndTestPlan(Iteration iteration);

}
