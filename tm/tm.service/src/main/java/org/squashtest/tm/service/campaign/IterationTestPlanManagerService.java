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

import java.util.Date;
import java.util.List;

import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.ExecutionStatus;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * 
 * @author Agnes Durand
 */
public interface IterationTestPlanManagerService extends IterationTestPlanFinder {

	/**
	 * Adds a list of test cases to an iteration.
	 * 
	 * @param testCaseIds
	 * @param iterationId
	 */
	void addTestCasesToIteration(List<Long> testCaseIds, long iterationId);

	/**
	 * Adds a list of test cases to an iteration.
	 * 
	 * @param testCaseIds
	 * @param iteration
	 */
	List<IterationTestPlanItem> addTestPlanItemsToIteration(List<Long> testCaseIds, Iteration iteration);

	/**
	 * Removes a list of test cases from a campaign excepted the test plans which were executed
	 * 
	 * @param testPlanIds
	 *            the ids of the test plan managing that test case for that iteration
	 * @param iterationId
	 *            the id of the iteration
	 * @return true if at least one test plan was already executed and therefore not deleted
	 */
	boolean removeTestPlansFromIteration(List<Long> testPlanIds, long iterationId);

	/**
	 * Removes a list of test cases from an iteration excepted the test plans which were executed
	 * 
	 * @param testPlanIds
	 *            the ids of the test plan managing that test case for that iteration
	 * @param iteration
	 *            the iteration
	 * @return true if at least one test plan was already executed and therefore not deleted
	 */
	boolean removeTestPlansFromIterationObj(List<Long> testPlanIds, Iteration iteration);

	/**
	 * Removes a test case from an iteration except if the test plans was executed
	 * 
	 * @param testPlanId
	 *            the id of the test plan managing that test case for that iteration
	 * @param iterationId
	 * @return true if the test plan was already executed and therefore not deleted
	 */
	boolean removeTestPlanFromIteration(long testPlanItemId);

	/**
	 * Update item test plan lastExecuted data (by and on) (for the moment they're constants)
	 * 
	 * @param givenTestPlan
	 *            : the test plan to update
	 * @param executionDate
	 *            : the execution date
	 */
	@Deprecated
	void updateTestCaseLastExecutedByAndOn(IterationTestPlanItem givenTestPlan, Date lastExecutedOn,
			String lastExecutedBy);

	/**
	 * Will update the item test plan execution metadata using the last execution data.
	 * 
	 * @param execution
	 */
	void updateExecutionMetadata(IterationTestPlanItem item);
	
	/**
	 * Assign User with Execute Access to a TestPlan item.
	 * @param testCaseId
	 * @param campaignId
	 */
	void assignUserToTestPlanItem(long testPlanItemId, long userId);

	/**
	 * Assign User with Execute Access to a multiple TestPlan items.
	 * 
	 * @param testPlanIds
	 * @param campaignId
	 */
	void assignUserToTestPlanItems(List<Long> testPlanIds, long userId);

	/**
	 * <p>
	 * persist each iteration_test_plan_item and add it to iteration
	 * </p>
	 * 
	 * @param testPlan
	 * @param iterationId
	 */
	void addTestPlanToIteration(List<IterationTestPlanItem> testPlan, long iterationId);

	/**
	 * 
	 * @return the list of defined execution statuses
	 */
	List<ExecutionStatus> getExecutionStatusList();

	/**
	 * Assigns an execution status to a test plan item Overrides the current execution status
	 * 
	 * @param iterationTestPlanItemId
	 * @param statusName
	 */
	void assignExecutionStatusToTestPlanItem(long iterationTestPlanItemId, String statusName);

}
