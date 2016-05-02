/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ParameterArray;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.NotFoundException;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class configure and execute a unique HTTP request.
 * This case is simple enough, we don't need to watch a full build.
 *
 *
 */

public class StartTestExecution {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestExecution.class);
	private static final String UNUSED = "unused";

	private final BuildDef buildDef;

	private final HttpClientProvider clientProvider;

	private final String externalId;

	public StartTestExecution(BuildDef buildDef, HttpClientProvider clientProvider, String externalId) {
		super();
		this.buildDef = buildDef;
		this.clientProvider = clientProvider;
		this.externalId = externalId;
	}

	public void run() {

		TestAutomationProject project = buildDef.getProject();
		TestAutomationServer server = project.getServer();

		RestTemplate template = new RestTemplate(clientProvider.getRequestFactoryFor(
			project.getServer()));

		String url = createUrl(server);
		Map<String, ?> urlParams = createUrlParams(project);
		MultiValueMap<String, ?> postData = createPostData(buildDef, externalId);


		Object bime = execute(template, url, postData, urlParams);

		// TODO : inspect the result to check errors and such a la RequestExecutor
		LOGGER.info("started build {}", bime);

	}

	private Object execute(RestTemplate template, String url, MultiValueMap<String, ?> postData, Map<String, ?> urlParams) {
		try {
			return template.postForLocation(url, postData, urlParams);
		} catch (ResourceAccessException ex) {
			throw new ServerConnectionFailed(ex);
		} catch (HttpClientErrorException ex) {
			switch (ex.getStatusCode()) {
				case FORBIDDEN:
				case UNAUTHORIZED:
				case PROXY_AUTHENTICATION_REQUIRED:
					throw new AccessDenied(); // NOSONAR no need for actual call stack
				case NOT_FOUND:
					throw new NotFoundException(ex);
				default:
					throw new TestAutomationException(ex.getMessage(), ex);
			}
		}
	}

	private String createUrl(TestAutomationServer server) {
		return server.getBaseURL().toString() + "/job/{jobName}/build";
	}

	private Map<String, ?> createUrlParams(TestAutomationProject project) {
		Map<String, Object> params = new HashMap<>();
		params.put("jobName", project.getJobName());
		return params;
	}

	private MultiValueMap<String, ?> createPostData(BuildDef buildDef, String externalId) {

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

		ParameterArray stdParams = new HttpRequestFactory().getStartTestSuiteBuildParameters(externalId,
			buildDef.getNode());

		File tmp;
		try {

			tmp = createJsonSuite(buildDef);
			parts.add(HttpRequestFactory.MULTIPART_BUILDFILENAME, new FileSystemResource(tmp));
			parts.add(HttpRequestFactory.MULTIPART_JENKINSARGS, new ObjectMapper().writeValueAsString(stdParams));

		} catch (JsonProcessingException e) {
			LOGGER.error("Error while mashalling json model. Maybe a bug ?", e);

		} catch (IOException e) {
			LOGGER.error("Error while writing json model into temp file. Maybe temp folder is not writable ?", e);

		}

		return parts;
	}

	private File createJsonSuite(BuildDef buildDef) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		File tmp = File.createTempFile("ta-suite", ".json");
		tmp.deleteOnExit();

		objectMapper.writeValue(tmp, new JsonSuiteAdapter(buildDef));

		return tmp;
	}

	/**
	 * Adapts a TestAutomationProjectContent into something which can be marshalled into a json test suite
	 * (payload of "execute tests" request).
	 *
	 * @author Gregory Fouquet
	 *
	 */
	private static final class JsonSuiteAdapter {
		private final BuildDef buildDef;
		private List<JsonTestAdapter> tests;

		private JsonSuiteAdapter(BuildDef buildDef) {
			super();
			this.buildDef = buildDef;
		}

		@SuppressWarnings(UNUSED)
		public List<JsonTestAdapter> getTest() {
			if (tests == null) {
				tests = new ArrayList<>();

				for (Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec : buildDef
					.getParameterizedExecutions()) {
					JsonTestAdapter json = new JsonTestAdapter(paramdExec);
					tests.add(json);
				}
			}

			return tests;
		}
	}

	/**
	 * Adapts a parameterized test (<code>Couple<AutomatedTest, Map></code>) into something suitable for the
	 * "execute tests" request.
	 *
	 * @author Gregory Fouquet
	 *
	 */
	private static final class JsonTestAdapter {
		private final Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec;

		private JsonTestAdapter(@NotNull Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec) {
			super();
			this.paramdExec = paramdExec;
		}

		@SuppressWarnings(UNUSED)
		public String getScript() {
			return paramdExec.getA1().getAutomatedTest().getName();
		}

		@SuppressWarnings(UNUSED)
		public String getId() {
			return paramdExec.getA1().getId().toString();
		}

		@SuppressWarnings(UNUSED)
		public Map<String, Object> getParam() {
			return paramdExec.getA2();
		}

	}

}
