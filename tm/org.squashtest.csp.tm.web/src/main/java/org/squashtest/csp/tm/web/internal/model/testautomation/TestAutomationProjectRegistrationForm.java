package org.squashtest.csp.tm.web.internal.model.testautomation;

import java.net.MalformedURLException;
import java.net.URL;

import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

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
