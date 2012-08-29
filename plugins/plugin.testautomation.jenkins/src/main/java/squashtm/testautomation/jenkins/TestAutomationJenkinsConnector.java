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
package squashtm.testautomation.jenkins;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.domain.TestAutomationTest;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.HttpClientProvider;
import squashtm.testautomation.jenkins.internal.net.HttpRequestFactory;
import squashtm.testautomation.jenkins.internal.net.RequestExecutor;
import squashtm.testautomation.spi.TestAutomationConnector;
import squashtm.testautomation.spi.exceptions.AccessDenied;
import squashtm.testautomation.spi.exceptions.NotFoundException;
import squashtm.testautomation.spi.exceptions.ServerConnectionFailed;
import squashtm.testautomation.spi.exceptions.TestAutomationException;
import squashtm.testautomation.spi.exceptions.UnreadableResponseException;


@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector{

	
	private static final String CONNECTOR_KIND = "jenkins";
	private static final int DEFAULT_SPAM_INTERVAL_MILLIS = 5000;
	
	
	private HttpClientProvider clientProvider = new HttpClientProvider();
	
	
	private JsonParser jsonParser = new JsonParser();
	
	
	private HttpRequestFactory requestFactory = new HttpRequestFactory();

	
	@Value("${tm.test.automation.pollinterval.millis}")
	private int spamInterval = DEFAULT_SPAM_INTERVAL_MILLIS;
	
	
	private RequestExecutor requestExecutor = RequestExecutor.getInstance();
	
	//****************************** let's roll ****************************************


	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}
	
	
	@PostConstruct
	public void initialize(){
		
	}
	
	public boolean checkCredentials(TestAutomationServer server) {
		
		HttpClient client = clientProvider.getClientFor(server);

		GetMethod credCheck = requestFactory.newCheckCredentialsMethod(server);
		
		requestExecutor.execute(client, credCheck);
		
		//if everything went fine, we may return true. Or else let the exception go.
		return true;
	
	}
	
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) 
				throws  ServerConnectionFailed,
						AccessDenied, 
						UnreadableResponseException, 
						TestAutomationException {
			
		HttpClient client = clientProvider.getClientFor(server);
		
		GetMethod getJobsMethod = requestFactory.newGetJobsMethod(server);
		
		String response = requestExecutor.execute(client, getJobsMethod);
		
		try{
			return jsonParser.readJobListFromJson(response);
		}
		catch(UnreadableResponseException ex){
			throw new UnreadableResponseException("Test automation - jenkins : server '"+server+"' returned malformed response : ", ex.getCause());
		}
		
	} 
	
	
	@Override
	public Collection<TestAutomationTest> listTestsInProject(TestAutomationProject project) 
				throws ServerConnectionFailed,
					   AccessDenied, 
					   UnreadableResponseException, 
					   NotFoundException,
					   TestAutomationException {

		return null;	
	}

	
	// ************************************ private tools ************************** 

	
}
