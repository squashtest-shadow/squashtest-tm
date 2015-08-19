/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.springframework.http.client.CommonsClientHttpRequestFactory
import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.CallbackURL
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class StartTestExecutionIT extends Specification {

	BuildDef buildDef = Mock()
	TestAutomationProject project = Mock()

	def setup() {
		buildDef.project >> project
	}



	def mockHttpClientProvider(httpClient) {
		HttpClientProvider res = Mock()
		res.getClientFor(_) >> httpClient
		res.getRequestFactoryFor(_) >> new CommonsClientHttpRequestFactory(httpClient);

		return res
	}

	def "should start a new build"() {
		given:
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		manager.getParams().setMaxTotalConnections(25);

		HttpClient client = new HttpClient(manager);
		client.getParams().setAuthenticationPreemptive(true);


		and:
		TestAutomationServer server = Mock()
		server.baseURL >> new URL("http://localhost:9292/stub-ta-server")
		project.server >> server

		and:
		project.jobName >> "fancy job"

		and:
		AutomatedExecutionExtender exec = Mock()
		exec.getId() >> 12
		AutomatedTest test = Mock()
		exec.getAutomatedTest() >> test
		test.fullName >> "fancy test"

		buildDef.parameterizedExecutions >> [
			new Couple(exec, [batman:"leatherpants"])
		]

		and:
		// this initializes CallbackURL.instance. I wouldn't go so far as to call CallbackURL filthy, but it's definitely dirty
		new CallbackURL().setURL("http://127.0.0.1/squashtm")

		and :
		HttpClientProvider provider = mockHttpClientProvider(client)

		when:
		new StartTestExecution(buildDef, provider, "EXTERNAL-ID").run();

		then:
		notThrown(Exception)
	}
}