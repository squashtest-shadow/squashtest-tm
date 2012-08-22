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
package squashtm.testautomation.service;

import java.net.URL;
import java.util.Collection;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.spi.exceptions.AccessDenied;


public interface TestAutomationFinderService {

	
	/**
	 * <p>Given the URL of the test automation server, and the credentials required to connect it, will return the list of 
	 * project currently available on it. The credentials will be tested on the fly.</p>
	 * 
	 * @param serverURL
	 * @param login
	 * @param password
	 * 
	 * @return a collection of projects hosted on that server
	 * @throws AccessDenied if the given credentials are invalid
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
	

	

}
