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
package squashtm.testautomation.jenkins.internal;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Component;

import squashtm.testautomation.domain.TestAutomationServer;


@Component
public class HttpRequestFactory {
	
	
	private static final String JOBS_LIST_URI = "/api/json";
	
	private static final NameValuePair[] JOBS_LIST_QUERY = new NameValuePair[] { 
																new NameValuePair("tree", "jobs[name,color]") 
															};

	private static final NameValuePair[] TEST_LIST_QUERY = new NameValuePair[]{
																new NameValuePair("tree", "suites[name,cases[name]]")	
															};
	
	
	public GetMethod newCheckCredentialsMethod(TestAutomationServer server){
		
		GetMethod method = new GetMethod();
		
		method.setPath(server.getBaseURL().toExternalForm());
		
		String logPass = server.getLogin()+":"+server.getPassword();
		String auth = new String(Base64.encodeBase64(logPass.getBytes()));
		
		method.addRequestHeader(new Header("Authorization", "Basic "+auth));
		
		method.setDoAuthentication(true);
		
		return method;
	}
	
	
	public GetMethod newGetJobsMethod(TestAutomationServer server){
		
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
