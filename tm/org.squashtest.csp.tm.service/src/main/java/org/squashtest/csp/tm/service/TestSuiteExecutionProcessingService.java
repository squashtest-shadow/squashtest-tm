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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;

@Transactional
public interface TestSuiteExecutionProcessingService {

	/**
	 * <p>
	 * returns the execution step were to resume the test suite<br>
	 * or null if no execution step is to be resumed
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return
	 */
	ExecutionStep findExecutionStepWhereToResumeExecutionOfSuite(long testSuiteId);

	/**
	 * <p>
	 * returns the execution step were to start the test suite execution<br>
	 * or null if there is no execution step
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return
	 */
	ExecutionStep relaunchExecution(long testSuiteId);

	/**
	 * <p>
	 * Should start a new execution for the given test suite, ie create an execution for the first test case of this
	 * suite's test plan.
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return the created {@link Execution}
	 */
	Execution startNewExecution(long testSuiteId);

	/**
	 * <p>
	 * tells if a test suite has at least one executable item in its test plan after the given item.
	 * </p>
	 * 
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @return
	 */
	boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId);
}
