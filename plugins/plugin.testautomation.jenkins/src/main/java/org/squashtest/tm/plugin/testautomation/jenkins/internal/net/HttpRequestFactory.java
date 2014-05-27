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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Parameter;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ParameterArray;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.BadConfiguration;

public class HttpRequestFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestFactory.class);

	private static final String API_URI = "/api/json";

	private static final NameValuePair[] JOB_LIST_QUERY = new NameValuePair[] {
		new NameValuePair("tree", "jobs[name,color]")
	};

	private static final NameValuePair[] QUEUED_BUILDS_QUERY = new NameValuePair[] {
		new NameValuePair("tree", "items[id,actions[parameters[name,value]],task[name]]")
	};

	private static final NameValuePair[] EXISTING_BUILDS_QUERY = new NameValuePair[] {
		new NameValuePair("tree", "builds[building,number,actions[parameters[name,value]]]")
	};

	private static final NameValuePair[] SINGLE_BUILD_QUERY = new NameValuePair[] {
		new NameValuePair("tree", "building,number,actions[parameters[name,value]]")
	};

	private static final NameValuePair[] BUILD_RESULT_QUERY = new NameValuePair[] {
		new NameValuePair("tree", "suites[name,cases[name,status]]")
	};

	private JsonParser jsonParser = new JsonParser();

	private CallbackURLProvider callbackProvider = new CallbackURLProvider();

	public String newRandomId() {
		return Long.valueOf(System.currentTimeMillis()).toString();
	}

	public GetMethod newCheckCredentialsMethod(TestAutomationServer server) {

		String path = toUrlPath(server, API_URI);

		GetMethod method = new GetMethod();

		method.setPath(path);

		String logPass = server.getLogin() + ":" + server.getPassword();
		String auth = new String(Base64.encodeBase64(logPass.getBytes()));

		method.addRequestHeader(new Header("Authorization", "Basic " + auth));

		method.setDoAuthentication(true);

		return method;
	}

	public GetMethod newGetJobsMethod(TestAutomationServer server) {

		String path = toUrlPath(server, API_URI);

		GetMethod method = new GetMethod();

		method.setPath(path);
		method.setQueryString(JOB_LIST_QUERY);

		method.setDoAuthentication(true);

		return method;
	}

	public PostMethod newStartFetchTestListBuild(TestAutomationProject project, String externalID) {

		ParameterArray params = new ParameterArray(
				new Parameter[] {
						Parameter.operationTestListParameter(),
						Parameter.newExtIdParameter(externalID)
				}
				);

		PostMethod method = newStartBuild(project, params);

		return method;

	}

	/**
	 * @deprecated
	 * @param content
	 * @param externalID
	 * @return
	 */
	@Deprecated
	public PostMethod newStartTestSuiteBuild(TestAutomationProjectContent content, String externalID) {

		String strURL = callbackProvider.get().toExternalForm();

		ParameterArray params = new ParameterArray(
				new Parameter[] {
						Parameter.operationRunSuiteParameter(),
						Parameter.newExtIdParameter(externalID),
						Parameter.newCallbackURlParameter(strURL)
				}
				);

		PostMethod method = newStartBuild(content.getProject(), params);

		return method;
	}

	public GetMethod newCheckQueue(TestAutomationProject project) {

		String path = toUrlPath(project.getServer(), "/queue" + API_URI);

		GetMethod method = new GetMethod();
		method.setPath(path);
		method.setQueryString(QUEUED_BUILDS_QUERY);
		method.setDoAuthentication(true);

		return method;
	}

	public GetMethod newGetBuildsForProject(TestAutomationProject project) {

		String path = toUrlPath(project.getServer(), "/job/" + project.getJobName() + API_URI);

		GetMethod method = new GetMethod();
		method.setPath(path);
		method.setQueryString(EXISTING_BUILDS_QUERY);

		method.setDoAuthentication(true);

		return method;

	}

	public GetMethod newGetBuild(TestAutomationProject project, int buildId) {

		String path = toUrlPath(project.getServer(), "/job/" + project.getJobName() + "/" + buildId + "/" + API_URI);

		GetMethod method = new GetMethod();
		method.setPath(path);
		method.setQueryString(SINGLE_BUILD_QUERY);

		method.setDoAuthentication(true);

		return method;
	}

	public GetMethod newGetBuildResults(TestAutomationProject project, int buildId) {

		String path = toUrlPath(project.getServer(), "/job/" + project.getJobName() + "/" + buildId + "/testReport/"
				+ API_URI);

		GetMethod method = new GetMethod();
		method.setPath(path);
		method.setQueryString(BUILD_RESULT_QUERY);

		method.setDoAuthentication(true);

		return method;

	}

	public String buildResultURL(AutomatedTest test, Integer buildID) {

		TestAutomationProject project = test.getProject();

		String relativePath = toRelativePath(test);
		String urlPath = toUrlPath(project.getServer(), "/job/" + project.getJobName() + "/" + buildID + "/testReport/"
				+ relativePath);

		return urlPath;

	}

	// ******************************* private stuffs ***********************

	protected PostMethod newStartBuild(TestAutomationProject project, ParameterArray params) {

		String path = toUrlPath(project.getServer(), "/job/" + project.getJobName() + "/build");

		String jsonParam = jsonParser.toJson(params);

		PostMethod method = new PostMethod();
		method.setPath(path);
		method.setParameter("json", jsonParam);

		method.setDoAuthentication(true);

		return method;

	}

	private String toUrlPath(TestAutomationServer server, String path) {

		StringBuilder urlBuilder = new StringBuilder();
		URL baseURL = server.getBaseURL();

		urlBuilder.append(baseURL.toExternalForm());
		urlBuilder.append(path);

		return makeURL(urlBuilder.toString()).toExternalForm();
	}

	private URL makeURL(String unescaped) {
		try {
			String uri = URIUtil.encodePath(unescaped);
			return new URL(uri);
		} catch (URIException ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("HttpRequestFactory : the URI is invalid, and that was not supposed to happen.");
			}
			throw new RuntimeException(ex);
		} catch (MalformedURLException ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("HttpRequestFactory : the URI corresponds to an invalid URL, and that was not supposed to happen.");
			}
			throw new RuntimeException(ex);
		}
	}

	private String toRelativePath(AutomatedTest test) {

		String name = "";

		if (test.isAtTheRoot()) {
			name = "(root)/";
		}

		name += test.getPath() + test.getShortName().replaceAll("[-\\.]", "_");

		return name;

	}

	private static class CallbackURLProvider {

		public URL get() {

			CallbackURL callback = CallbackURL.getInstance();

			try {

				String strURL = callback.getValue();
				URL url = new URL(strURL);
				return url;

			} catch (MalformedURLException ex) {

				BadConfiguration bc = new BadConfiguration(
						"Test Automation configuration : The test could not be started because the service is not configured properly. "
								+
								"The url specified at property '" + callback.getConfPropertyName()
								+ "' in configuration file 'tm.testautomation.conf.properties' is malformed. Please " +
						"contact the administration team.");

				bc.setPropertyName(callback.getConfPropertyName());

				throw bc;
			}

		}

	}

}
