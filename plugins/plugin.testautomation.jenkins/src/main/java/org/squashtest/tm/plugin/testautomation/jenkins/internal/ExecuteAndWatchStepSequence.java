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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import static org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildStage.GET_BUILD_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.CallbackURL;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * Note by G.Fouquet : this object's init is a bit complicated and was handled by its processor (context). As I dont
 * think it should be the responsibility of the processor, I introduced "service locator" aspect to processor / context
 * so that this object fetches the services it needs but which could not be injected and self inits. as much as
 * possible.
 * 
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
// spring support of httpclient 3.1 deprecated yet we rely on it
class ExecuteAndWatchStepSequence extends HttpBasedStepSequence implements StepSequence {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteAndWatchStepSequence.class);

	private final BuildDef buildDef;

	private final ExecuteAndWatchContext context;

	private final StepEventListener<GetBuildID> buildIdListener;

	@Override
	protected BuildProcessor getProcessor() {
		return context;
	}

	// ************** constructor ****************

	ExecuteAndWatchStepSequence(ExecuteAndWatchContext context, BuildDef buildDef, String externalId,
			StepEventListener<GetBuildID> buildIdListener) {
		super();
		this.context = context;
		this.buildDef = buildDef;
		this.buildIdListener = buildIdListener;

		TestAutomationProject project = buildDef.getProject();
		this.setProject(project);
		this.setClient(context.getHttpClientProvider().getClientFor(project.getServer()));
		this.setAbsoluteId(new BuildAbsoluteId(project.getName(), externalId));

	}

	// *************** code ****************

	@Override
	public boolean hasMoreElements() {
		return (currentStage != GET_BUILD_ID);
	}

	@Override
	public BuildStep<?> nextElement() {
		switch (currentStage) {

		case WAITING:
			currentStage = BuildStage.START_BUILD;
			return newStartBuild();

		case START_BUILD:
			currentStage = BuildStage.CHECK_QUEUE;
			return newCheckQueue();

		case CHECK_QUEUE:
			currentStage = BuildStage.GET_BUILD_ID;
			return newGetBuildID();

		case GET_BUILD_ID:
			throw new NoSuchElementException();

		default:
			throw new NoSuchElementException();

		}
	}

	// ********** some override ****************

	@Override
	protected GetBuildID newGetBuildID() {
		GetBuildID step = super.newGetBuildID();
		step.addListener(buildIdListener);
		return step;
	}

	protected StartBuild newStartBuild() {
		TestAutomationProject project = buildDef.getProject();
		TestAutomationServer server = project.getServer();

		RestTemplate template = new RestTemplate(context.getHttpClientProvider().getRequestFactoryFor(
				project.getServer()));

		Object bime = template.postForLocation(createUrl(server), createPostData(buildDef, absoluteId.getExternalId()),
				createUrlParams(project));

		LOGGER.info("started build {}", bime);

		return new StartBuild(context);

	}

	private String createUrl(TestAutomationServer server) {
		return server.getBaseURL().toString() + "/job/{jobName}/build";
	}

	private Map<String, ?> createUrlParams(TestAutomationProject project) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("jobName", project.getName());
		return params;
	}

	private MultiValueMap<String, ?> createPostData(BuildDef buildDef, String externalId) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

		parts.add("operation", "run");
		parts.add("externalJobId", externalId);
		parts.add("notificationURL", CallbackURL.getInstance().getValue());
		parts.add("testList", "{\"file\": \"testsuite.json\"}");

		File tmp;
		try {
			tmp = createJsonSuite(buildDef);
			parts.add("testsuite.json", new FileSystemResource(tmp));
		} catch (JsonProcessingException e) {
			LOGGER.error("Error while mashalling json model. Maybe a bug ?", e);
		} catch (IOException e) {
			LOGGER.error("Error while writing json model into temp file. Maybe temp folder is not writable ?", e);
		}

		return parts;
	}

	private File createJsonSuite(BuildDef buildDef) throws IOException, JsonGenerationException, JsonMappingException {
		ObjectMapper objectMapper = new ObjectMapper();

		File tmp = File.createTempFile("ta-suite", ".json");
		tmp.deleteOnExit();

		objectMapper.writeValue(tmp, new JsonSuiteAdapter(buildDef));

		return tmp;
	}

	/**
	 * Adapts a {@link TestAutomationProjectContent} into something which can be marshalled into a json test suite
	 * (payload of "execute tests" request).
	 * 
	 * @author Gregory
	 * 
	 */
	private static class JsonSuiteAdapter {
		private final BuildDef buildDef;
		private List<JsonTestAdapter> tests;

		private JsonSuiteAdapter(BuildDef buildDef) {
			super();
			this.buildDef = buildDef;
		}

		@SuppressWarnings("unused")
		public List<JsonTestAdapter> getTest() {
			if (tests == null) {
				tests = new ArrayList<JsonTestAdapter>();

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
	private static class JsonTestAdapter {
		private final Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec;

		private JsonTestAdapter(@NotNull Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec) {
			super();
			this.paramdExec = paramdExec;
		}

		@SuppressWarnings("unused")
		public String getScript() {
			return paramdExec.getA1().getAutomatedTest().getFullName();
		}

		@SuppressWarnings("unused")
		public String getId() {
			return paramdExec.getA1().getId().toString();
		}

		@SuppressWarnings("unused")
		public Map<String, Object> getParam() {
			return paramdExec.getA2();
		}

	}
}
