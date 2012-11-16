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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

public interface ExecutionDao extends EntityDao<Execution> {

	List<ExecutionStep> findExecutionSteps(long executionId);

	Execution findAndInit(long executionId);

	int findExecutionRank(long executionId);

	ExecutionStatusReport getStatusReport(long executionId);

	long countSuccess(long executionId);

	long countReady(long executionId);

	List<ExecutionStep> findStepsFiltered(Long executionId, Paging filter);

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
