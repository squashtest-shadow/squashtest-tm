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
package squashtm.automatest.jenkins;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.automatest.TestAutomationProject;
import org.squashtest.csp.tm.domain.automatest.TestAutomationServer;

import squashtm.automatest.spi.TestAutomationConnector;
import squashtm.automatest.spi.exceptions.AccessDenied;
import squashtm.automatest.spi.exceptions.ServerConnectionFailed;
import squashtm.automatest.spi.exceptions.TestAutomationException;
import squashtm.automatest.spi.exceptions.UnreadableResponseException;


@Service
public class TestAutomationJenkinsConnector implements TestAutomationConnector{
	
	private static final String CONNECTOR_KIND = "jenkins";

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}

	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) 
				throws  ServerConnectionFailed,
						AccessDenied, 
						UnreadableResponseException, 
						TestAutomationException {
		
		
		
		return null;
	} 
	
	
}
