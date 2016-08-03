/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;

public interface ExecutionDao extends EntityDao<Execution> {

	List<ExecutionStep> findExecutionSteps(long executionId);

	List<ExecutionStep> findExecutionSteps(Collection<Long> executionIds);

	List<ActionTestStep> findOriginalSteps(long executionId);

	List<Long> findOriginalStepIds(long executionId);

	Execution findAndInit(long executionId);

	int findExecutionRank(long executionId);

	ExecutionStatusReport getStatusReport(long executionId);

	long countSuccess(long executionId);

	long countReady(long executionId);

	boolean exists(long executionId);

	// ************** special execution status deactivation section ***************

	List<ExecutionStep> findStepsFiltered(Long executionId, Paging filter);

	List<ExecutionStep> findAllExecutionStepsWithStatus(Long projectId, ExecutionStatus source);

	List<Long> findAllExecutionIdHavingStepWithStatus(Long projectId, ExecutionStatus source);

	List<IterationTestPlanItem> findAllIterationTestPlanItemsWithStatus(Long projectId, ExecutionStatus source);

	boolean projectUsesExecutionStatus(long projectId, ExecutionStatus executionStatus);

	void replaceExecutionStepStatus(long projectId, ExecutionStatus oldStatus, ExecutionStatus newStatus);

	void replaceTestPlanStatus(long projectId, ExecutionStatus oldStatus, ExecutionStatus newStatus);


	// ************* /special execution status deactivation section ***************


	List<IssueDetector> findAllIssueDetectorsForExecution(Long execId);

	long countExecutionSteps(long executionId);

	/**
	 * @param testCaseId
	 * @param paging
	 * @return
	 */
	List<Execution> findAllByTestCaseIdOrderByRunDate(long testCaseId, Paging paging);

	/**
	 * Returns the executions which ran the given test case using the given paging and sorting data
	 * 
	 * @param testCaseId
	 * @param pas
	 *            non null paging and sorting data
	 * @return non null list of executions
	 */
	List<Execution> findAllByTestCaseId(long testCaseId, PagingAndSorting pas);

	/**
	 * Returns the count of executions which ran a given test case.
	 * 
	 * @param testCaseId
	 * @return
	 */
	long countByTestCaseId(long testCaseId);


	/**
	 * Tells whether the execution is fresh new or not. Namely, that all its steps have a status
	 * READY.
	 * 
	 * @param executionId
	 * @return
	 */
	boolean wasNeverRan(Long executionId);


}
