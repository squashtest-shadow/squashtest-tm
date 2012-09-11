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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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

import squashtm.testautomation.jenkins.internal.ExecuteTestsBuildProcessor;
import squashtm.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.HttpClientProvider;
import squashtm.testautomation.jenkins.internal.net.HttpRequestFactory;
import squashtm.testautomation.jenkins.internal.net.RequestExecutor;
import squashtm.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;


@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector{

	
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
