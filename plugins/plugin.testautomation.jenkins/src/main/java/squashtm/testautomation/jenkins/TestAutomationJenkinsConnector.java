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

import static org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.apache.commons.httpclient.HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED;
import static org.apache.commons.httpclient.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Service;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.HttpClientProvider;
import squashtm.testautomation.spi.TestAutomationConnector;
import squashtm.testautomation.spi.exceptions.AccessDenied;
import squashtm.testautomation.spi.exceptions.ServerConnectionFailed;
import squashtm.testautomation.spi.exceptions.TestAutomationException;
import squashtm.testautomation.spi.exceptions.UnreadableResponseException;


@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector{
	
	private static final String JOBS_LIST_URI = "/api/json";
	
	private static final NameValuePair[] JOBS_LIST_QUERY = new NameValuePair[] { 
																new NameValuePair("tree", "jobs[name,color]") 
															};

	private static final NameValuePair[] TEST_LIST_QUERY = new NameValuePair[]{
																new NameValuePair("tree", "suites[name,cases[name]]")	
															};
	
	
	private static final String CONNECTOR_KIND = "jenkins";
	
	
	
	@Inject
	private HttpClientProvider clientProvider;
	
	@Inject
	private JsonParser jsonParser;
	
	
	//****************************** let's roll ****************************************


	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}
	
	
	
	public boolean checkCredentials(TestAutomationServer server) {
		
		return true;
	}
	
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) 
				throws  ServerConnectionFailed,
						AccessDenied, 
						UnreadableResponseException, 
						TestAutomationException {
			
		HttpClient client = clientProvider.getClientFor(server);
		
		GetMethod getJobsMethod = newGetJobsMethod(server);
		
		try{
			
			int responseCode = client.executeMethod(getJobsMethod);
			
			checkResponseCode(responseCode);
			
			String jsonResponse = getJobsMethod.getResponseBodyAsString();	
			
			return jsonParser.readJobListFromJson(jsonResponse);
			
		}
		catch(IOException ex){
			throw new ServerConnectionFailed("Test automation - jenkins : could not connect to server '"+server+"' "+
					 						 "due to technical error : ", ex);
		}
		catch(AccessDenied ex){
			throw new AccessDenied("Test automation - jenkins : server '"+server+"' rejected the operation because of wrong credentials or insufficient privileges");
		}
		catch(UnreadableResponseException ex){
			throw new UnreadableResponseException("Test automation - jenkins : server '"+server+"' returned malformed response : ", ex.getCause());
		}
		finally{
			getJobsMethod.releaseConnection();
		}
		
	} 
	
	
	
	// ************************************ private tools ************************** 

	
	private void checkResponseCode(int responseCode){
		
		if (responseCode == SC_OK){
			return;
		}
		
		
		switch(responseCode){
			case SC_FORBIDDEN :
			case SC_UNAUTHORIZED :
			case SC_PROXY_AUTHENTICATION_REQUIRED :
				throw new AccessDenied();
		}
	}
	
	
	private GetMethod newGetJobsMethod(TestAutomationServer server){
		
		StringBuilder urlBuilder= new StringBuilder();
		
		urlBuilder.append(server.getBaseURL().toExternalForm());
		urlBuilder.append(JOBS_LIST_URI);


		GetMethod method = new GetMethod();
		
		method.setPath(urlBuilder.toString());
		method.setQueryString(JOBS_LIST_QUERY);

		method.setDoAuthentication(true);
		
		return method;
	}
	
	
}
