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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.testcase.TestCase;


public interface IterationFinder {
	
	@Transactional(readOnly = true)
	List<Iteration> findIterationsByCampaignId(long campaignId);

	@Transactional(readOnly = true)
	Iteration findById(long iterationId);

	@Transactional(readOnly = true)
	List<Execution> findAllExecutions(long iterationId);

	@Transactional(readOnly = true)
	List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId);

	@Transactional(readOnly = true)
	List<TestCase> findPlannedTestCases(long iterationId);
}
