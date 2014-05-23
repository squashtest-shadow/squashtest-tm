/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Build;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.BuildList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.ExecuteAndWatchBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.ExecuteTestsBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.TestByProjectSorter;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.BadConfiguration;
import org.squashtest.tm.service.testautomation.spi.NotFoundException;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnreadableResponseException;


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
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), generateNewId()));
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

		TestByProjectSorter sorter = new TestByProjectSorter(tests);

		while(sorter.hasNext()){
			startTestExecution(sorter.getNext(), reference);
		}

	}


	@Override
	public void executeTests(Collection<AutomatedTest> tests, String reference,
			TestAutomationCallbackService callbackService)
					throws ServerConnectionFailed, AccessDenied,
					UnreadableResponseException, NotFoundException, BadConfiguration,
					TestAutomationException {

		TestByProjectSorter sorter = new TestByProjectSorter(tests);

		while(sorter.hasNext()){
			startTestExecution(sorter.getNext(), reference, callbackService);
		}

	}


	@Override
	public Map<AutomatedTest, URL> getResultURLs(Collection<AutomatedTest> tests, String reference) throws ServerConnectionFailed,
	AccessDenied,
	UnreadableResponseException,
	NotFoundException,
	BadConfiguration,
	TestAutomationException {

		Map<AutomatedTest, URL> resultMap = new HashMap<AutomatedTest, URL>(tests.size());

		TestByProjectSorter sorter = new TestByProjectSorter(tests);

		while(sorter.hasNext()){

			TestAutomationProjectContent content = sorter.getNext();

			try{

				Integer buildID = optimisticGetBuildID(content.getProject(), reference);

				createAndAddURLs(resultMap, content, buildID);
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

	// ****************** private, second layer method *********************

	private void startTestExecution(TestAutomationProjectContent content, String externalID){

		TestAutomationProject project = content.getProject();

		HttpClient client = clientProvider.getClientFor(project.getServer());

		ExecuteTestsBuildProcessor processor = new ExecuteTestsBuildProcessor(taskScheduler);

		processor.setClient(client);
		processor.setProjectContent(content);
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), externalID));
		processor.setDefaultReschedulingDelay(spamInterval);

		processor.run();

	}

	private void startTestExecution(TestAutomationProjectContent content, String externalID, TestAutomationCallbackService service){

		TestAutomationProject project = content.getProject();

		ResultURLUpdater updater = new ResultURLUpdater(service, content, externalID);

		HttpClient client = clientProvider.getClientFor(project.getServer());

		ExecuteAndWatchBuildProcessor processor = new ExecuteAndWatchBuildProcessor(taskScheduler);

		processor.setClient(client);
		processor.setProjectContent(content);
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), externalID));
		processor.setDefaultReschedulingDelay(spamInterval);
		processor.setGetBuildIDListener(updater);

		processor.run();

	}


	/*
	 * that method is said optimistic because it will attempt to get the buildID of a given build without certainty of its existence
	 * (the lack of which is likely to bring the process to a disappointing conclusion).
	 */
	private Integer optimisticGetBuildID(TestAutomationProject project, String externalID){

		HttpClient client = clientProvider.getClientFor(project.getServer());
		GetMethod method = requestFactory.newGetBuildsForProject(project);

		String json = requestExecutor.execute(client, method);
		BuildList buildList = jsonParser.getBuildListFromJson(json);
		Build buildOfInterest =  buildList.findByExternalId(externalID);

		if (buildOfInterest!=null){
			return buildOfInterest.getId();
		}
		else{
			throw new NotFoundException("TestAutomationConnector : the requested build for project "+project.getJobName()+" externalID "+externalID+" cannot be found");
		}

	}


	private void createAndAddURLs(Map<AutomatedTest, URL> allURLs, TestAutomationProjectContent content, Integer buildID){

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

	// ************************************ other private stuffs **************************

	private String generateNewId(){
		return Long.valueOf(System.currentTimeMillis()).toString();
	}


	private class ResultURLUpdater implements StepEventListener<GetBuildID>{

		private TestAutomationCallbackService service;
		private TestAutomationProjectContent content;
		private String externalID;


		ResultURLUpdater(TestAutomationCallbackService service, TestAutomationProjectContent content, String externalID){
			this.service = service;
			this.content = content;
			this.externalID = externalID;
		}


		@Override
		public void onComplete(GetBuildID step) {

			Map<AutomatedTest, URL> resultUrlPerTest = new HashMap<AutomatedTest, URL>(content.getTests().size());

			Integer buildID = step.getBuildID();

			createAndAddURLs(resultUrlPerTest, content, buildID);

			Iterator<Map.Entry<AutomatedTest, URL>> iterator = resultUrlPerTest.entrySet().iterator();

			while(iterator.hasNext()){

				Map.Entry<AutomatedTest, URL> entry = iterator.next();

				AutomatedExecutionSetIdentifier identifier = toIdentifier(entry.getKey());

				service.updateResultURL(identifier, entry.getValue());

			}

		}

		@Override
		public void onError(GetBuildID step, Exception exception) {
			//nothing special, the regular exception handling is good enough
		}

		private AutomatedExecutionSetIdentifier toIdentifier(AutomatedTest test){
			return new SimpleAutoExecIdentifier(test.getProject().getJobName(), externalID, test.getName());
		}

	}


	private static class SimpleAutoExecIdentifier implements AutomatedExecutionSetIdentifier{


		private String testAutomationProjectName;
		private String automatedSuiteId;
		private String automatedTestName;


		public SimpleAutoExecIdentifier(String testAutomationProjectName,
				String automatedSuiteId, String automatedTestName) {
			super();
			this.testAutomationProjectName = testAutomationProjectName;
			this.automatedSuiteId = automatedSuiteId;
			this.automatedTestName = automatedTestName;
		}

		@Override
		public String getTestAutomationProjectName() {
			return testAutomationProjectName;
		}

		@Override
		public String getAutomatedSuiteId() {
			return automatedSuiteId;
		}

		@Override
		public String getAutomatedTestName() {
			return automatedTestName;
		}

	}


	/**
	 * @see org.squashtest.tm.service.testautomation.spi.TestAutomationConnector#executeParameterizedTests(java.util.Collection, java.lang.String, org.squashtest.tm.service.testautomation.TestAutomationCallbackService)
	 */
	@Override
	public void executeParameterizedTests(Collection<Couple<AutomatedTest, Map<String, Object>>> tests, String id,
			TestAutomationCallbackService securedCallback) {
		// TODO Auto-generated method stub

	}

}
