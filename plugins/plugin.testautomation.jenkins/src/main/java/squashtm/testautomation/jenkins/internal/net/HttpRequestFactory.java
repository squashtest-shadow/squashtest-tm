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
package squashtm.testautomation.jenkins.internal.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.jenkins.beans.Parameter;
import squashtm.testautomation.jenkins.beans.ParameterArray;
import squashtm.testautomation.jenkins.internal.JsonParser;


public class HttpRequestFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestFactory.class);
	
	private static final String API_URI = "/api/json";
	
	private static final NameValuePair[] JOBS_LIST_QUERY = new NameValuePair[] { 
																new NameValuePair("tree", "jobs[name,color]") 
															};
	
	private static final NameValuePair[] QUEUED_BUILDS_QUERY = new NameValuePair[]{		
																new NameValuePair("tree","items[id,actions[parameters[name,value]],task[name]]")
															};
	

	private static final NameValuePair[] BUILD_LIST_QUERY = new NameValuePair[]{
																new NameValuePair("depth", "1"),
																new NameValuePair("tree", "builds[number,actions[parameters[name,value]]]")
															};
	
	private static final NameValuePair[] TEST_LIST_QUERY = new NameValuePair[]{
																new NameValuePair("tree", "suites[name,cases[name]]")	
															};

	private JsonParser jsonParser = new JsonParser();
	
	
	
	public String newRandomId(){
		return new Long(new Date().getTime()).toString();
	}
	
	
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
		urlBuilder.append(API_URI);

		GetMethod method = new GetMethod();
		
		method.setPath(urlBuilder.toString());
		method.setQueryString(JOBS_LIST_QUERY);

		method.setDoAuthentication(true);
		
		return method;
	}
	
	
	
	public PostMethod newStartFetchTestListBuild(TestAutomationProject project, String externalID){
				
		ParameterArray params = new ParameterArray( 
			new Parameter[]{
				Parameter.operationTestListParameter(),
				Parameter.newExtIdParameter(externalID)
			}
		);
		
		
		PostMethod method = newStartBuild(project, params);
		
		return method;
		
	}
	
	
	
	public GetMethod newCheckQueue(TestAutomationProject project){
		
		String strURL = project.getServer().getBaseURL().toExternalForm()+"/queue"+API_URI;
		URL url = makeURL(strURL);
		
		GetMethod method = new GetMethod();
		method.setPath(url.toString());
		method.setQueryString(QUEUED_BUILDS_QUERY);
		method.setDoAuthentication(true);
		
		return method;
	}
	
	
	public GetMethod newGetBuildsForProject(TestAutomationProject project){
		
		StringBuilder urlBuilder = new StringBuilder();
		URL baseURL = project.getServer().getBaseURL();
		
		urlBuilder.append(baseURL.toExternalForm());
		urlBuilder.append("/job/"+project.getName()+"/api/json");
		
		String finalURL = makeURL(urlBuilder.toString()).toExternalForm();
		
		GetMethod method = new GetMethod();
		method.setPath(finalURL);
		method.setQueryString(BUILD_LIST_QUERY);
		
		return method;	
		
	}
	
	public GetMethod newGetTestListFromBuild(TestAutomationProject project, int buildId){
		
		StringBuilder urlBuilder = new StringBuilder();
		URL baseURL = project.getServer().getBaseURL();
		
		urlBuilder.append(baseURL.toExternalForm());
		urlBuilder.append("/job/"+project.getName()+"/"+buildId+"/testReport/"+API_URI);
		
		String finalURL = makeURL(urlBuilder.toString()).toExternalForm();
		
		GetMethod method = new GetMethod();
		method.setPath(finalURL);
		method.setQueryString(TEST_LIST_QUERY);
		
		return method;	
		
		
	}
	
	//******************************* private stuffs ***********************
	
	protected PostMethod newStartBuild(TestAutomationProject project, ParameterArray params){
		
		StringBuilder urlBuilder = new StringBuilder();
		URL baseURL = project.getServer().getBaseURL();
		
		urlBuilder.append(baseURL.toExternalForm());
		urlBuilder.append("/job/"+project.getName()+"/build");
		
		URL finalURL = makeURL(urlBuilder.toString());
		
		String jsonParam = jsonParser.toJson(params);
		
		PostMethod method = new PostMethod();
		method.setPath(finalURL.toString());
		method.setParameter("json", jsonParam);
		
		method.setDoAuthentication(true);
		
		return method; 
		
	}


	
	private URL makeURL(String unescaped){
		try{
			String uri = URIUtil.encodePath(unescaped);
			return new URL(uri);
		}
		catch(URIException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error("HttpRequestFactory : the URI is invalid, and that was not supposed to happen.");
			}
			throw new RuntimeException(ex);
		}
		catch(MalformedURLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error("HttpRequestFactory : the URI gave birth to invalid URL, and that was not supposed to happen.");
			}
			throw new RuntimeException(ex);
		}
	}
	
}
