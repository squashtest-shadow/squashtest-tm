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
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * 
 * @author Agnes Durand
 */
@Transactional
public interface IterationTestPlanManagerService extends IterationTestPlanFinder {

	/**
	 * Find a iteration using its id
	 * 
	 * @param iterationId
	 */
	Iteration findIteration(long iterationId);

	/**
	 * Returns a collection of {@link TestCaseLibrary}, the test cases of which may be added to the campaign
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	/**
	 * Adds a list of test cases to an iteration.
	 * @param testCaseIds
	 * @param iterationId
	 */
	void addTestCasesToIteration(List<Long> testCaseIds, long iterationId);
	
	/**
	 * Adds a list of test cases to an iteration.
	 * @param testCaseIds
	 * @param iteration
	 */
	List<IterationTestPlanItem> addTestPlanItemsToIteration(List<Long> testCaseIds, Iteration iteration);

	/**
	 * Adds a list of test cases to a campaign.
	 * 
	 * @param testCaseIdss
	 * @param campaignId
	 */
	IterationTestPlanItem findTestPlanItemByTestCaseId(long iterationId, long testCaseId);

	IterationTestPlanItem findTestPlanItem(Long iterationId, Long itemTestPlanId);

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
	boolean removeTestPlanFromIteration(Long testPlanId, long iterationId);

	List<TestCase> findPlannedTestCases(Long iterationId);

	/**
	 * Update item test plan lastExecuted data (by and on) (for the moment they're constants)
	 * 
	 * @param givenTestPlan
	 *            : the test plan to update
	 * @param executionDate
	 *            : the execution date
	 */
	void updateTestCaseLastExecutedByAndOn(IterationTestPlanItem givenTestPlan, Date lastExecutedOn,
			String lastExecutedBy);

	/**
	 * Get Users with Write Access for an Iteration and its TestPlan.
	 * 
	 * @param testCaseId
	 * @param campaignId
	 */
	List<User> findAssignableUserForTestPlan(long iterationId);

	/**
	 * Assign User with Write Access to a TestPlan item.
	 * 
	 * @param testCaseId
	 * @param campaignId
	 */
	void assignUserToTestPlanItem(Long testPlanId, long iterationId, Long userId);

	/**
	 * Assign User with Write Access to a multiple TestPlan items.
	 * 
	 * @param testPlanIds
	 * @param campaignId
	 */
	void assignUserToTestPlanItems(List<Long> testPlanIds, long iterationId, Long userId);

	/**
	 * <p>
	 * persist each iteration_test_plan_item and add it to iteration
	 * </p>
	 * 
	 * @param testPlan
	 * @param iterationId
	 */
	void addTestPlanToIteration(List<IterationTestPlanItem> testPlan, long iterationId);

}
