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
package squashtm.testautomation.spi;

import java.util.Collection;


import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.domain.TestAutomationTest;
import squashtm.testautomation.spi.exceptions.AccessDenied;
import squashtm.testautomation.spi.exceptions.NotFoundException;
import squashtm.testautomation.spi.exceptions.ServerConnectionFailed;
import squashtm.testautomation.spi.exceptions.TestAutomationException;
import squashtm.testautomation.spi.exceptions.UnreadableResponseException;


public interface TestAutomationConnector {
	
	/**
	 * A String indicating which kind of connector it is
	 * 
	 * @return
	 */
	String getConnectorKind();
	
	
	/**
	 * Checks that the given server configuration (including credentials) actually works.
	 * 
	 * @param server
	 * @return true if the credentials work, false otherwise
	 */
	boolean checkCredentials(TestAutomationServer server);
	
	
	/**
	 * <p>Given a server (that contains everything you need to connect it), returns the collection of {@link TestAutomationProject} 
	 * that it hosts.</p>
	 *  
	 * @param server
	 * @return a Collection that may never be null if success
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or insulted you
	 * @throws NotFoundException if the server could not find its projects
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above. 
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) 
				throws ServerConnectionFailed,
					   AccessDenied,
					   UnreadableResponseException,
					   NotFoundException,
					   TestAutomationException;
	
	
	/**
	 * <p>Given a project (that contains everything you need to connect it), returns the collection of {@link TestAutomationTest}
	 * that are bundled with it</p>
	 *
	 * @param project
	 * @return a Collection possibly empty but never null of TestAutomationTest if success
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or was rude
	 * @throws NotFoundException if the tests in that project cannot be found
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above. 
	 */
	Collection<TestAutomationTest> listTestsInProject(TestAutomationProject project)
			   throws ServerConnectionFailed,
			   		  AccessDenied,
			   		  UnreadableResponseException,
			   		  NotFoundException,
			   		  TestAutomationException;
	
}
