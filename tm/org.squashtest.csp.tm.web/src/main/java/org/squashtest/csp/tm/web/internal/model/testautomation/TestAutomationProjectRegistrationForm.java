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
package org.squashtest.csp.tm.web.internal.model.testautomation;

import java.net.MalformedURLException;
import java.net.URL;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;

public class TestAutomationProjectRegistrationForm {
	
	private String serverBaseURL;
	private String serverLogin;
	private String serverPassword;
	
	private String projectName;

	public String getServerBaseURL() {
		return serverBaseURL;
	}

	public void setServerBaseURL(String serverBaseURL) {
		this.serverBaseURL = serverBaseURL;
	}

	public String getServerLogin() {
		return serverLogin;
	}

	public void setServerLogin(String serverLogin) {
		this.serverLogin = serverLogin;
	}

	public String getServerPassword() {
		return serverPassword;
	}

	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public TestAutomationProject toTestAutomationProject() throws MalformedURLException{
		TestAutomationServer server = new TestAutomationServer(new URL(serverBaseURL), serverLogin, serverPassword);
		TestAutomationProject project = new TestAutomationProject(projectName, server);
		return project;
		
	}
	
	
}
