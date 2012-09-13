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
package org.squashtest.tm.plugin.testautomation.jenkins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;
import org.squashtest.csp.tm.testautomation.spi.AccessDenied;
import org.squashtest.csp.tm.testautomation.spi.BadConfiguration;
import org.squashtest.csp.tm.testautomation.spi.NotFoundException;
import org.squashtest.csp.tm.testautomation.spi.ServerConnectionFailed;
import org.squashtest.csp.tm.testautomation.spi.TestAutomationConnector;
import org.squashtest.csp.tm.testautomation.spi.TestAutomationException;
import org.squashtest.csp.tm.testautomation.spi.UnreadableResponseException;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Build;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.BuildList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.ExecuteTestsBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;



@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector{

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);
	
	private static final String CONNECTOR_KIND = "jenkins";
	private static final int DEFAULT_SPAM_INTERVAL_MILLIS = 5000;
	
	
	//@Inject
	private TaskScheduler taskScheduler;
	
	
	private HttpClientProvider clientProvider = new HttpClientProvider();
	
	
	private JsonParser jsonParser = new JsonParser();
	
	
	private HttpRequestFactory requestFactory = new HttpRequestFactory();

	
	@Value("${tm.test.automation.pollinterval.millis}")
	private int spamInterval = DEFAULT_SPAM_INTERVAL_MILLIS;
	
	
	
	private RequestExecutor requestExecutor = RequestExecutor.getInstance();
	
	//****************************** let's roll ****************************************

	
	@ServiceReference
	public void setTaskScheduler(TaskScheduler taskScheduler){
		this.taskScheduler=taskScheduler;
	}

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
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
						BadConfiguration, 
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
	public Collection<AutomatedTest> listTestsInProject(TestAutomationProject project) 
				throws ServerConnectionFailed,
					   AccessDenied, 
					   UnreadableResponseException, 
					   NotFoundException,
					   BadConfiguration, 
					   TestAutomationException {

		HttpClient client = clientProvider.getClientFor(project.getServer());
		
		FetchTestListBuildProcessor processor = new FetchTestListBuildProcessor();
		
		processor.setClient(client);
		processor.setProject(project);
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getName(), generateNewId()));
		processor.setDefaultReschedulingDelay(spamInterval);
		
		processor.run();
		
		return processor.getResult();
		
	}

	
	@Override
	public void executeTests(Collection<AutomatedTest> tests, String reference)
			throws ServerConnectionFailed, 
			AccessDenied,
			UnreadableResponseException, 
			NotFoundException,
			BadConfiguration, 
			TestAutomationException {

		TestSorter sorter = new TestSorter(tests);
		
		while(sorter.hasNext()){
			_startTestExecution(sorter.getNext(), reference);
		}
	
	}
	
	@Override
	public Map<AutomatedTest, URL> getResultURLs(Collection<AutomatedTest> tests, String reference) throws ServerConnectionFailed,
					  AccessDenied,
					  UnreadableResponseException,
					  NotFoundException,
					  BadConfiguration,
					  TestAutomationException {
		
		Map<AutomatedTest, URL> resultMap = new HashMap<AutomatedTest, URL>();
		
		TestSorter sorter = new TestSorter(tests);
		
		while(sorter.hasNext()){

			TestAutomationProjectContent content = sorter.getNext();
			
			try{
			
				Integer buildID = _optimisticGetBuildID(content.getProject(), reference);
			
				_createAndAddURLs(resultMap, content, buildID);
			}
			catch(TestAutomationException ex){
				if (LOGGER.isErrorEnabled()){
					LOGGER.error("Test Automation : could not create result url due to an inner error : ",ex );
				}
				for (AutomatedTest test : content.getTests()){
					resultMap.put(test, null);
				}
			}
			
		}
		
		return resultMap;
		
	}
	
	
	private void _startTestExecution(TestAutomationProjectContent content, String externalID){
		
		TestAutomationProject project = content.getProject();
		
		HttpClient client = clientProvider.getClientFor(project.getServer());
		
		ExecuteTestsBuildProcessor processor = new ExecuteTestsBuildProcessor(taskScheduler);
		
		processor.setClient(client);
		processor.setProjectContent(content);
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getName(), externalID));
		processor.setDefaultReschedulingDelay(spamInterval);
		
		processor.run();
		
	}
	
	
	/*
	 * that method is said optimistic because it will attempt to get the buildID of a given build without certainty of its existence 
	 * (the lack of which is likely to bring the process to a disappointing conclusion).
	 */
	private Integer _optimisticGetBuildID(TestAutomationProject project, String externalID){
		
		HttpClient client = clientProvider.getClientFor(project.getServer());		
		GetMethod method = requestFactory.newGetBuildsForProject(project);
		
		String json = requestExecutor.execute(client, method);		
		BuildList buildList = jsonParser.getBuildListFromJson(json);		
		Build buildOfInterest =  buildList.findByExternalId(externalID);
		
		if (buildOfInterest!=null){
			return buildOfInterest.getId();			
		}
		else{
			throw new NotFoundException("TestAutomationConnector : the requested build for project "+project.getName()+" externalID "+externalID+" cannot be found");
		}		
		
	}
	
	
	private void _createAndAddURLs(Map<AutomatedTest, URL> allURLs, TestAutomationProjectContent content, Integer buildID){
		
		for (AutomatedTest test : content.getTests()){
			
			String resultPath = requestFactory.buildResultURL(test, buildID);  
			
			URL resultURL;
			
			try {
				resultURL = new URL(resultPath);
			} 
			catch (MalformedURLException e) {
				if (LOGGER.isErrorEnabled()){
					LOGGER.error("Test Automation : malformed URL, could not create result url from string '"+resultPath+"'",e);
				}
				resultURL = null;
			}
			
			allURLs.put(test, resultURL);
			
		}
		
	}
	
	// ************************************ private tools ************************** 

	private String generateNewId(){
		return new Long(System.currentTimeMillis()).toString();
	}


	private static class TestSorter{
	
		private Map<TestAutomationProject, TestAutomationProjectContent> testsByProject = new HashMap<TestAutomationProject, TestAutomationProjectContent>();
		
		private Iterator<TestAutomationProjectContent> iterator;
		
		public TestSorter(Collection<AutomatedTest> tests){
			
			for (AutomatedTest test : tests){
				
				TestAutomationProject project = test.getProject();
				
				register(project, test);
				
			}
			
			iterator = testsByProject.values().iterator();
			
		}
		
		public boolean hasNext(){
			return iterator.hasNext();
		}
		
		public TestAutomationProjectContent getNext(){
			return iterator.next();
		}
		
		private void register(TestAutomationProject project, AutomatedTest test){
			
			if (! testsByProject.containsKey(project)){
				TestAutomationProjectContent newContent = new TestAutomationProjectContent(project, new LinkedList<AutomatedTest>());
				testsByProject.put(project, newContent);
			}
			
			TestAutomationProjectContent content = testsByProject.get(project);
			
			content.getTests().add(test);
			
		}
		
	}
	

}
