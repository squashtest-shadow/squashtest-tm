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
package org.squashtest.csp.tm.service;

import java.net.URL;
import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;


@Transactional
public interface TestAutomationManagementService {

	
	/**
	 * <p>Given the URL of the test automation server, and the credentials required to connect it, will return the list of 
	 * project currently available on it.</p>
	 * 
	 * @param serverURL
	 * @param login
	 * @param password
	 */
	//no security needed here : the real endpoint of that operation is a remote test automation server, not Squash TM.
	Collection<TestAutomationProject> listProjectsOnServer(URL serverURL, String login, String password);
	
	/**
	 * see {@link #listProjectsOnServer(URL, String, String)}, using a {@link TestAutomationServer} for argument
	 * 
	 * @param server
	 * @return
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server);
	
	
	/**
	 * <p>
	 * 	Will persist the supplied remote TestAutomationProject. The argument must be attached to a {@link TestAutomationServer} 
	 * 	and return it using {@link TestAutomationProject#getServer()}.
	 * </p> 
	 * 
	 * <p>
	 * 	The service will check if both the project and the server are unknown prior to persist it (them). A Server is considered unknown 
	 * 	if there are no such server in the database having the same exact properties. Same goes for the projects : a project is unknown
	 *  if there are no such project in the database having the same exact properties (including server instance).
	 *  Their ID won't be considered in the process.
	 * </p>
	 * 
	 * <p>
	 * 	Whenever one or both is (are) known from the database the persistent instance will be used in place of the argument.
	 * In other words transient arguments will be merged if possible with persistent arguments.
	 * </p>
	 * 
	 * <p>
	 * 	That method returns the persisted instance of TestAutomationProject, that should be used in place of the one supplied in arguments 
	 * 	from now on by the client code.
	 * </p>
	 * 
	 * @param remoteProject
	 * @param TMprojectId
	 */
	//@PreAuthorize(some expression) TODO : harass someone who can make specs for that.
	TestAutomationProject fetchOrPersist(TestAutomationProject newProject); 
	
}
