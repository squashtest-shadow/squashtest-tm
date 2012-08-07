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
package squashtm.automatest.spi;

import java.util.Collection;

import org.squashtest.csp.tm.domain.automatest.TestAutomationProject;
import org.squashtest.csp.tm.domain.automatest.TestAutomationServer;

import squashtm.automatest.spi.exceptions.AccessDenied;
import squashtm.automatest.spi.exceptions.ServerConnectionFailed;
import squashtm.automatest.spi.exceptions.TestAutomationException;
import squashtm.automatest.spi.exceptions.UnreadableResponseException;


public interface TestAutomationConnector {

	/**
	 * A String indicating which kind of connector it is
	 * 
	 * @return
	 */
	String getConnectorKind();
	
	
	/**
	 * <p>Given a server (that contains everything you need to connect it), returns the collection of {@link TestAutomationProject} 
	 * that it hosts.</p>
	 * 
	 * <p>That method must be synchronous and perform its task within the calling thread.</p>
	 * 
	 * @param server
	 * @return a Collection that may never be null if success
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response.
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above. 
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) 
				throws ServerConnectionFailed,
					   AccessDenied,
					   UnreadableResponseException,
					   TestAutomationException;
	
}
