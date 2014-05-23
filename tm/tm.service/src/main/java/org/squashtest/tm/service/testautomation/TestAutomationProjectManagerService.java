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

import java.net.URL;
import java.util.Collection;

import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;


public interface TestAutomationProjectManagerService {

	// *********************** entity management *******************

	void persist(TestAutomationProject newProject);


	TestAutomationProject findProjectById(long projectId);

	boolean hasExecutedTests(long serverId);

	void deleteProject(long projectId);

	void deleteAllForTMProject(long tmProjectId);


	// *********************** Properties mutators ****************************


	void changeLabel(long projectId, String name);

	void changeJobName(long projectId, String jobName);

	/**
	 * Note : the slave list is a semi-column separated list
	 * 
	 */
	void setSlaveNodes(long projectId, String slaveList);



	// *********************** remote calls ************************************


	/**
	 * <p>Given the name of a server, will return the list of
	 * project currently available on it. The credentials will be tested on the fly.</p>
	 * 
	 * @param serverURL
	 * @param login
	 * @param password
	 * 
	 * @return a collection of projects hosted on that server
	 * @throws AccessDenied if the given credentials are invalid
	 */
	Collection<TestAutomationProject> listProjectsOnServer(String serverName);

	/**
	 * see {@link #listProjectsOnServer(URL, String, String)}, using its ID for argument
	 * 
	 * @param server
	 * @return
	 */
	Collection<TestAutomationProject> listProjectsOnServer(Long serverId);


	/**
	 * see {@link #listProjectsOnServer(URL, String, String)}, using a {@link TestAutomationServer} for argument
	 * 
	 * @param server
	 * @return
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server);

}
