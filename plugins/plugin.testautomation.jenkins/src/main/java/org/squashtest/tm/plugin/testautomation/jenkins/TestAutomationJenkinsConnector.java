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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildDef;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.ExecuteAndWatchBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.BadConfiguration;
import org.squashtest.tm.service.testautomation.spi.NotFoundException;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnreadableResponseException;

@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private static final String CONNECTOR_KIND = "jenkins";
	private static final int DEFAULT_SPAM_INTERVAL_MILLIS = 5000;

	@Inject
	private TaskScheduler taskScheduler;
	@Inject
	private HttpClientProvider clientProvider;

	private JsonParser jsonParser = new JsonParser();
	private HttpRequestFactory requestFactory = new HttpRequestFactory();

	@Value("${tm.test.automation.pollinterval.millis}")
	private int spamInterval = DEFAULT_SPAM_INTERVAL_MILLIS;

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();

	@Inject
	private Provider<ExecuteAndWatchBuildProcessor> executeAndWatchBuildProcessor;

	// ****************************** let's roll ****************************************

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}

	public boolean checkCredentials(TestAutomationServer server) {

		HttpClient client = clientProvider.getClientFor(server);

		GetMethod credCheck = requestFactory.newCheckCredentialsMethod(server);

		requestExecutor.execute(client, credCheck);

		// if everything went fine, we may return true. Or else let the exception go.
		return true;

	}

	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server)
			throws ServerConnectionFailed, AccessDenied, UnreadableResponseException, BadConfiguration,
			TestAutomationException {

		HttpClient client = clientProvider.getClientFor(server);

		GetMethod getJobsMethod = requestFactory.newGetJobsMethod(server);

		String response = requestExecutor.execute(client, getJobsMethod);

		try {
			return jsonParser.readJobListFromJson(response);
		} catch (UnreadableResponseException ex) {
			throw new UnreadableResponseException("Test automation - jenkins : server '" + server
					+ "' returned malformed response : ", ex.getCause());
		}

	}

	@Override
	public Collection<AutomatedTest> listTestsInProject(TestAutomationProject project) throws ServerConnectionFailed,
	AccessDenied, UnreadableResponseException, NotFoundException, BadConfiguration, TestAutomationException {

		HttpClient client = clientProvider.getClientFor(project.getServer());

		FetchTestListBuildProcessor processor = new FetchTestListBuildProcessor();

		processor.setClient(client);
		processor.setProject(project);
		processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), generateNewId()));
		processor.setDefaultReschedulingDelay(spamInterval);

		processor.run();

		return processor.getResult();

	}


	private void startTestExecution(BuildDef buildDef, String externalId, TestAutomationCallbackService service) {

		ResultURLUpdater updater = new ResultURLUpdater(service, buildDef.getTests(), externalId);

		executeAndWatchBuildProcessor.get()
		.configuration()
		.buildDef(buildDef)
		.externalId(externalId)
		.buildIdListener(updater)
		.configure()
		.run();

	}

	// ************************************ other private stuffs **************************

	private String generateNewId() {
		return Long.toString(System.currentTimeMillis());
	}

	private class ResultURLUpdater implements StepEventListener<GetBuildID> {

		private TestAutomationCallbackService service;
		private Collection<AutomatedTest> tests;
		private String externalID;

		ResultURLUpdater(TestAutomationCallbackService service, Collection<AutomatedTest> tests, String externalID) {
			this.service = service;
			this.tests = tests;
			this.externalID = externalID;
		}

		@Override
		public void onComplete(GetBuildID step) {

			Map<AutomatedTest, URL> resultUrlPerTest = new HashMap<AutomatedTest, URL>(tests.size());

			Integer buildID = step.getBuildID();

			createAndAddURLs(resultUrlPerTest, tests, buildID);

			for (Map.Entry<AutomatedTest, URL> entry : resultUrlPerTest.entrySet()) {
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

		private void createAndAddURLs(Map<AutomatedTest, URL> allURLs, Collection<AutomatedTest> tests, Integer buildID) {

			for (AutomatedTest test : tests) {
				String resultPath = requestFactory.buildResultURL(test, buildID);
				URL resultURL;

				try {
					resultURL = new URL(resultPath);
				} catch (MalformedURLException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Test Automation : malformed URL, could not create result url from string '"
								+ resultPath + "'", e);
					}
					resultURL = null;
				}

				allURLs.put(test, resultURL);

			}

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
	 * @see org.squashtest.tm.service.testautomation.spi.TestAutomationConnector#executeParameterizedTests(java.util.Collection,
	 *      java.lang.String, org.squashtest.tm.service.testautomation.TestAutomationCallbackService)
	 */
	@Override
	public void executeParameterizedTests(
			Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions,
			String externalId, TestAutomationCallbackService callbackService) {

		MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject = reduceToParamdExecsByProject(parameterizedExecutions);

		List<BuildDef> buildDefs = mapToJobDefs(execsByProject);

		for (BuildDef buildDef : buildDefs) {
			startTestExecution(buildDef, externalId, callbackService);
		}

	}

	private List<BuildDef> mapToJobDefs(
			MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject) {
		ArrayList<BuildDef> jobDefs = new ArrayList<BuildDef>(execsByProject.size());

		for (Entry<TestAutomationProject, List<Couple<AutomatedExecutionExtender, Map<String, Object>>>> entry : execsByProject
				.entrySet()) {
			jobDefs.add(new BuildDef(entry.getKey(), entry.getValue()));
		}
		return jobDefs;
	}

	private MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> reduceToParamdExecsByProject(
			Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions) {
		MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject = new LinkedMultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>>();

		for (Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec : parameterizedExecutions) {
			execsByProject.add(paramdExec.getA1().getAutomatedProject(), paramdExec);
		}

		return execsByProject;
	}
}
