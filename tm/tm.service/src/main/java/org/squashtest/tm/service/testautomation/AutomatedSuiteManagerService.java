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
package org.squashtest.tm.service.testautomation;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

public interface AutomatedSuiteManagerService {

	/**
	 * Finds a suite given its id
	 * 
	 * @param id
	 * @return
	 */
	AutomatedSuite findById(String id);


	/**
	 * Creates a new AutomatedSuite based on the whole test plan of an {@link Iteration}, given its ID. Only automated tests planned in the
	 * test plan will be included. The automated executions are ordered according to the test plan.
	 * 
	 * @param iterationId
	 * @return
	 */
	AutomatedSuite createFromIterationTestPlan(long iterationId);

	/**
	 * Creates a new AutomatedSuite based on the whole test plan of a {@link TestSuite}, given its ID. Only automated tests planned in the
	 * test plan will be included. The automated executions are ordered according to the test plan.
	 * 
	 * @param iterationId
	 * @return
	 */
	AutomatedSuite createFromTestSuiteTestPlan(long testSuiteId);

	/**
	 * Creates a new AutomatedSuite based on a collection of {@link IterationTestPlanItem}, given their ID. Only automated tests will
	 * be included. The automated executions are ordered according to the order of the argument.
	 * 
	 * @param testPlanIds
	 * @return
	 */
	AutomatedSuite createFromItemIds(List<Long> testPlanIds);

	/**
	 * Given the id of an {@link AutomatedSuite}, returns its content as tests grouped by projects.
	 * 
	 * @param autoSuiteId
	 * @return
	 */
	Collection<TestAutomationProjectContent> sortByProject(String autoSuiteId);

	/**
	 * Given an {@link AutomatedSuite}, returns its content as tests grouped by projects.
	 * 
	 * @param suite
	 * @return
	 */
	Collection<TestAutomationProjectContent> sortByProject(AutomatedSuite suite);

	/**
	 * Runs the given AutomatedSuite.
	 * 
	 * @param suite
	 */
	void start(AutomatedSuite suite);

	/**
	 * Runs an AutomatedSuite given its ID.
	 * 
	 * @param autoSuiteId
	 */
	void start(String autoSuiteId);


	/**
	 * Given the id of an automated test suite, returns the list of executions associated to this automated test suite.
	 * 
	 * @param automatedTestSuiteId
	 * @return
	 */
	List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId);

}
