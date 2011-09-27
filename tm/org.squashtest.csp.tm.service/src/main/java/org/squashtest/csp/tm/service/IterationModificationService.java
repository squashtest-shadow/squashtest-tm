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

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Transactional
public interface IterationModificationService {
	/**
	 * Adds an iteration to the list of iterations of a campaign.
	 * 
	 * @param iteration
	 * @param campaignId
	 * @return the index of the added iteration.
	 */
	int addIterationToCampaign(Iteration iteration, long campaignId);

	List<Iteration> findIterationsByCampaignId(long campaignId);
	
	Iteration findById(Long iterationId);
	
	void updateDescription(Long iterationId, String newDescription);

	String delete(Long iterationId);

	void rename(Long iterationId, String newName);
	
	void setScheduledStartDate(Long iterationId, Date scheduledStart);
	
	void setScheduledEndDate(Long iterationId, Date scheduledEnd);
	
	void setActualStartDate(Long iterationId, Date actualStart);
	
	void setActualEndDate(Long iterationId, Date actualEnd);	
	
	void setActualStartAuto(Long iterationId, boolean isAuto);
	
	void setActualEndAuto(Long iterationId, boolean isAuto);	
	
	
	/* ****************************** test plan ********************************************** */
	
	
	void addExecution(Long iterationId, Long testPlanId);
	
	void changeTestPlanPosition(Long iterationId, Long testPlanId, int newIndex);
	
	List<Execution> findAllExecutions(Long iterationId);
	
	List<Execution> findExecutionsByTestPlan(Long iterationId, Long testPlanId);
	
	List<TestCase> findPlannedTestCases(Long iterationId);
	
	FilteredCollectionHolder<List<IterationTestPlanItem>> findIterationTestPlan(Long iterationId, CollectionSorting filter);
	
	/**
	 * that method should investigate the consequences of the deletion request, and return a report
	 * about what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);
	
	/**
	 * that method should delete the nodes. It still takes care of non deletable nodes so
	 * the implementation should filter out the ids who can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @return
	 */
	List<Long> deleteNodes(List<Long> targetIds);
	
}
