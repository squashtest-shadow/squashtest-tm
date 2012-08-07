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
package org.squashtest.csp.tm.internal.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.automatest.TestAutomationProject;
import org.squashtest.csp.tm.domain.automatest.TestAutomationServer;
import org.squashtest.csp.tm.internal.repository.TestAutomationServerDao;
import org.squashtest.csp.tm.service.TestAutomationManagementService;

import squashtm.automatest.api.TestAutomationConnector;



@Service("squashtest.tm.service.TestAutomationManagementService")
public class TestAutomationManagementServiceImpl implements TestAutomationManagementService {

	@Inject
	private TestAutomationServerDao serverDao;
	
	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;
	
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) {
		
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		
		Collection<TestAutomationProject> projects = connector.listProjectsOnServer(server);

		Collection<TestAutomationProject> projectsWithServer = new ArrayList<TestAutomationProject>();
		
		for (TestAutomationProject proj : projects){
			projectsWithServer.add(proj.setServer(server));
		}
		
		return projectsWithServer;
	}
	
	
	// TODO for now the kind is hardcoded to "jenkins". We can easily change that later.
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(URL serverURL, String login, String password) {
		
		TestAutomationServer server = new TestAutomationServer(serverURL, login, password);
		
		return listProjectsOnServer(server);
		
	}

	
	@Override
	public void bindAutomatedProject(long TMprojectId, TestAutomationProject remoteProject) {
		// TODO Auto-generated method stub
		
	}

}
