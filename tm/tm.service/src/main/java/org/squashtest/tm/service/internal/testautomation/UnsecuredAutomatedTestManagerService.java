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
package org.squashtest.tm.service.internal.testautomation;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.AutomatedTestFinderService;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * This one is called "Unsecured" because many (if not all) methods aren't properly secured. This interface should never
 * be exposed through OSGI, and the methods accessed via secured services instead.
 * 
 * @author bsiri
 * 
 */
public interface UnsecuredAutomatedTestManagerService extends AutomatedTestFinderService {

	// ************************ Entity management **********************

	TestAutomationProject findProjectById(long projectId);

	/**
	 * Will persist this test if really new, or return the persisted instance if this test already exists. Due to this
	 * the calling code should always rely on the returned instance of AutomatedTest.
	 * 
	 * @param newTest
	 */
	AutomatedTest persistOrAttach(AutomatedTest newTest);

	/**
	 * Will remove the test from the database, if and only if no TestCase nor AutomatedExecutionExtender still refer to
	 * it.
	 * 
	 * @param test
	 */
	void removeIfUnused(AutomatedTest test);

	/**
	 * Given the id of an automated test suite, returns the list of executions associated to this automated test suite.
	 * 
	 * @param automatedTestSuiteId
	 * @return
	 */
	List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId);

	// *********************** remote calls ************************************

	/**
	 * Given a collection of {@link TestAutomationProject}, will return the aggregated list of {@link AutomatedTest}
	 * paired with their owner project.
	 * 
	 * @param projects
	 * @return
	 */
	Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects);

	/**
	 * Will start a test suite, by dispatching the tests to the corresponding connectors.
	 * 
	 * @param suite
	 */
	void startAutomatedSuite(AutomatedSuite suite);

}
