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
package org.squashtest.tm.service.testautomation.spi;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;




public interface TestAutomationConnector {

	/**
	 * A String indicating which kind of connector it is
	 * 
	 * @return
	 */
	String getConnectorKind();


	/**
	 * Checks that the given server configuration (including credentials) actually works.
	 * 
	 * @param server
	 * @return true if the credentials work, false otherwise
	 */
	boolean checkCredentials(TestAutomationServer server);

	/**
	 * <p>Given a server (that contains everything you need to connect it), returns the collection of {@link TestAutomationProject}
	 * that it hosts.</p>
	 * 
	 * 
	 * @param server
	 * @return a Collection that may never be null if success
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or otherwise replied not nicely
	 * @throws NotFoundException if the server could not find its projects
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above.
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server)
			throws ServerConnectionFailed,
			AccessDenied,
			UnreadableResponseException,
			NotFoundException,
			BadConfiguration,
			TestAutomationException;


	/**
	 * <p>Given a project (that contains everything you need to connect it), returns the collection of {@link AutomatedTest}
	 * that it contains</p>
	 *
	 * @param project
	 * 
	 * @return a Collection possibly empty but never null of TestAutomationTest if success
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or otherwise was rude to you
	 * @throws NotFoundException if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above.
	 */
	Collection<AutomatedTest> listTestsInProject(TestAutomationProject project)
			throws ServerConnectionFailed,
			AccessDenied,
			UnreadableResponseException,
			NotFoundException,
			BadConfiguration,
			TestAutomationException;


	/**
	 * <p>Given a bunch of tests, must tell the remote server to execute them. These particular executions of those tests are grouped and must be
	 * identifiable by a reference.</p>
	 * 
	 * <p>That method must return immediately after initiating the test start sequence, it must not wait for their completion. However it may
	 * possibly start a background task to oversee the remote executions from here.</p>
	 * 
	 * @param tests the tests that must be executed
	 * @param reference a reference that index the resulting executions of those tests
	 * 
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or otherwise thrown garbages at you
	 * @throws NotFoundException if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above.
	 * @deprecated use instead
	 */
	@Deprecated
	void executeTests(Collection<AutomatedTest> tests, String reference)
			throws ServerConnectionFailed,
			AccessDenied,
			UnreadableResponseException,
			NotFoundException,
			BadConfiguration,
			TestAutomationException;


	/**
	 * <p>
	 * Same than {@link #executeTests(Collection, String)} (in particular, it must return as soon as the tests have
	 * started). Then, later on in an asynchronous process it may use the callback service to provide TM with updates
	 * on the given executions when they are available.
	 *  </p>
	 * 
	 *  <p>
	 *  	The {@link TestAutomationCallbackService} instance will handle the security context for you.
	 *  </p>
	 * 
	 * @param tests the tests that must be executed. These entities are detached copies of the real entities,
	 * 		so they won't keep a session or transaction stuck forever.
	 * 
	 * @param reference a reference that indexes the resulting executions of those tests
	 * 
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or otherwise returned gibberish
	 * @throws NotFoundException if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above.
	 * @deprecated use instead
	 */
	@Deprecated
	void executeTests(Collection<AutomatedTest> tests, String reference, TestAutomationCallbackService callbackService)
			throws ServerConnectionFailed,
			AccessDenied,
			UnreadableResponseException,
			NotFoundException,
			BadConfiguration,
			TestAutomationException;




	/**
	 * <p>Given a bunch of tests, must return the URLs of their executions, that can be identified by the reference. The is a map associating
	 * each test to the corresponding URL.</p>
	 * 
	 * @param tests the group of tests we want to know the URL where to look for the results.
	 * @param reference the reference of their executions.
	 * 
	 * @throws ServerConnectionFailed if could not connect to the server
	 * @throws AccessDenied if the server was reached but the used user could log in
	 * @throws UnreadableResponseException if the server replied something that is not suitable for a response or otherwise used a too mature language for this audience
	 * @throws NotFoundException if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException for anything that doesn't fit the exceptions above.
	 */
	Map<AutomatedTest, URL> getResultURLs(Collection<AutomatedTest> tests, String reference)
			throws ServerConnectionFailed,
			AccessDenied,
			UnreadableResponseException,
			NotFoundException,
			BadConfiguration,
			TestAutomationException;


	/**
	 * @param tests
	 * @param id
	 * @param securedCallback
	 */
	void executeParameterizedTests(Collection<Couple<AutomatedTest, Map<String, Object>>> tests, String id,
			TestAutomationCallbackService securedCallback);
}
