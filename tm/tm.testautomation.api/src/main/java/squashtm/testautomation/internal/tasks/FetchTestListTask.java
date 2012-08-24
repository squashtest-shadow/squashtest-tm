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
package squashtm.testautomation.internal.tasks;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.domain.TestAutomationTest;
import squashtm.testautomation.internal.service.TestAutomationConnectorRegistry;
import squashtm.testautomation.model.TestAutomationProjectContent;
import squashtm.testautomation.spi.TestAutomationConnector;

public class FetchTestListTask implements TestAutomationConnectorTask<TestAutomationProjectContent> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnectorTask.class);

	private TestAutomationConnectorRegistry connectorRegistry;
	private TestAutomationProject project;
	
	public FetchTestListTask(TestAutomationConnectorRegistry connectorRegistry, TestAutomationProject project){
		super();
		this.connectorRegistry=connectorRegistry;
		this.project=project;
	}
	
	@Override
	public TestAutomationProjectContent call() throws Exception {
		TestAutomationServer server = project.getServer();
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		
		Collection<TestAutomationTest> allTests =  connector.listTestsInProject(project);
		return new TestAutomationProjectContent(project, allTests);
	}
	
	
	@Override
	public TestAutomationProjectContent buildFailedResult(Exception thrownException) {
		if (LOGGER.isErrorEnabled()){
			LOGGER.error("TestAutomationConnector : the task 'fetch test list' failed for project '"+project.getName()+
						 "' on server '"+project.getServer().getBaseURL()+"', caused by :",thrownException);
		}
		return new TestAutomationProjectContent(project, thrownException);
	}

}
