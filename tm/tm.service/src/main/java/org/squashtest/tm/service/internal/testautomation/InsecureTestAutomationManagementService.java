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

import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.TestAutomationFinderService;


/**
 * That private interface contains methods that cannot be secured because the current ACL system doesn't encompass
 * the TestAutomationX entities. That service should always be called through a front service being itself secured.
 * 
 * That interface won't be published as OSGI service.
 * 
 * @author bsiri
 *
 */
public interface InsecureTestAutomationManagementService extends TestAutomationFinderService{


	void persist(AutomatedTest newTest);


	TestAutomationProject findProjectById(long projectId);


	AutomatedTest findTestById(long testId);




	/**
	 * Will (attempt to) retrieve the result url for all the executions that belongs to a given automated suite,
	 * and of which tests belongs to a given project.
	 * 
	 * @param project
	 * @param suite
	 */
	void fetchAllResultURL(TestAutomationProject project, AutomatedSuite suite);

	/**
	 * Will start a test suite, by dispatching the tests to the corresponding connectors.
	 * 
	 * @param suite
	 */
	void startAutomatedSuite(AutomatedSuite suite);


}
