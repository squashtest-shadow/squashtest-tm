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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

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
import org.squashtest.tm.plugin.testautomation.jenkins.beans.TestListElement;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildDef;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.OptimisticTestList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.StartTestExecution;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList;
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

		// first we try an optimistic approach
		try{
			OptimisticTestList otl = new OptimisticTestList(clientProvider, project);
			return otl.run();
		}

		// if the file isn't available we regenerate the file
		catch(Exception ex){

			HttpClient client = clientProvider.getClientFor(project.getServer());

			FetchTestListBuildProcessor processor = new FetchTestListBuildProcessor();

			processor.setClient(client);
			processor.setProject(project);
			processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), generateNewId()));
			processor.setDefaultReschedulingDelay(spamInterval);

			processor.run();

			return processor.getResult();

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

			new StartTestExecution(buildDef, clientProvider, externalId).run();

		}

	}


	// ************************************ other private stuffs **************************




	private String generateNewId() {
		return Long.toString(System.currentTimeMillis());
	}


	private List<BuildDef> mapToJobDefs(
			MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject) {
		ArrayList<BuildDef> jobDefs = new ArrayList<BuildDef>(execsByProject.size());

		for (Entry<TestAutomationProject, List<Couple<AutomatedExecutionExtender, Map<String, Object>>>> entry : execsByProject
				.entrySet()) {
			if (entry.getValue().size()> 0){
				// fetch the name of the slave node if any
				Couple<AutomatedExecutionExtender, Map<String, Object>> firstEntry = entry.getValue().get(0);

				jobDefs.add(new BuildDef(entry.getKey(), entry.getValue(), firstEntry.getA1().getNodeName()));
			}
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


	/**
	 * @see TestAutomationJenkinsConnector#findTestAutomationProjectURL(TestAutomationProject)
	 */
	@Override
	public URL findTestAutomationProjectURL(TestAutomationProject testAutomationProject) {
		TestAutomationServer server = testAutomationProject.getServer();
		String projectUrl = server.getBaseURL().toString() + "/job/" + testAutomationProject.getJobName();
		try {
			return new URL(projectUrl);
		} catch (MalformedURLException e) {
			throw new TestAutomationProjectMalformedURLException(projectUrl, e);
		}
	}


	public class TestAutomationProjectMalformedURLException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4904491027261699261L;

		public TestAutomationProjectMalformedURLException(String projectUrl, Exception e) {
			super("The test automation project url : " + projectUrl + ", is malformed", e);
		}

	}


}
