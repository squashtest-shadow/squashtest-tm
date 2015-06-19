/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;

/**
 * Iteration modification services which cannot be dynamically generated.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface CustomIterationModificationService extends IterationFinder {

	/**
	 * Adds an iteration to the list of iterations of a campaign.
	 * 
	 * @param iteration
	 * @param campaignId
	 * @return the index of the added iteration.
	 */
	int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan);

	String delete(long iterationId);

	void rename(long iterationId, String newName);

	Execution addExecution(long testPlanItemId);

	/**
	 * that method should investigate the consequences of the deletion request, and return a report about what will
	 * happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);

	/**
	 * that method should delete the nodes. It still takes care of non deletable nodes so the implementation should
	 * filter out the ids who can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @return
	 */
	OperationReport deleteNodes(List<Long> targetIds);

	void addTestSuite(long iterationId, TestSuite suite);

	List<TestSuite> findAllTestSuites(long iterationId);

	/**
	 * <p>
	 * That method will remove each test suite, leaving it's test plan items linked to no test_suite
	 * </p>
	 * 
	 * @param suitesIds
	 * @return
	 */
	OperationReport removeTestSuites(List<Long> suitesIds);

	/**
	 * <p>
	 * Will create a copy of the test suite and it's test plan , then associate it to the given iteration<br>
	 * will rename test suite if there is name conflict at destination
	 * </p>
	 * 
	 * @param testSuiteId
	 *            = test suite to copy
	 * @param iterationId
	 *            = iteration where to add the copy of the test suite
	 * @return the copy of the test suite
	 */
	TestSuite copyPasteTestSuiteToIteration(long testSuiteId, long iterationId);

	/**
	 * <p>
	 * will create a copy of the test suites and their test plan , then associate them to the given iteration<br>
	 * will rename test suites if there is name conflict at destination
	 * </p>
	 * 
	 * @param testSuiteIds
	 *            = list of test suites to copy
	 * @param iterationId
	 *            = iteration where to add the copy of the test suite
	 * @return the list containing all the copies of the test suites
	 */
	List<TestSuite> copyPasteTestSuitesToIteration(Long[] testSuiteIds, long iterationId);


	IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId);


}