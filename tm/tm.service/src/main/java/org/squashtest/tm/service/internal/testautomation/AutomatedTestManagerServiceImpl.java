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
package org.squashtest.tm.service.internal.testautomation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.internal.repository.AutomatedExecutionExtenderDao;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind;

/**
 * 
 * 
 * @author bsiri
 * 
 */
@Transactional
@Service("squashtest.tm.service.AutomatedTestService")
public class AutomatedTestManagerServiceImpl implements UnsecuredAutomatedTestManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);
	private static final int DEFAULT_THREAD_TIMEOUT = 30000; // timeout as milliseconds

	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;

	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private AutomatedSuiteDao automatedSuiteDao;

	@Inject
	private AutomatedTestDao testDao;

	@Inject
	private AutomatedExecutionExtenderDao extenderDao;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;

	@Inject
	private TestAutomationCallbackService callbackService;

	@Inject
	private CustomFieldValueFinderService customFieldValueFinder;
	@Inject
	private Provider<TaParametersBuilder> paramBuilder;

	private TestAutomationTaskExecutor executor;

	@Inject
	public void setAsyncTaskExecutor(AsyncTaskExecutor executor) {
		TestAutomationTaskExecutor taExecutor = new TestAutomationTaskExecutor(executor);
		this.executor = taExecutor;
	}

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	// ******************** Entity Management ************************

	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId) {

		List<Execution> executions = new ArrayList<Execution>();
		AutomatedSuite suite = automatedSuiteDao.findById(automatedTestSuiteId);
		for (AutomatedExecutionExtender e : suite.getExecutionExtenders()) {
			executions.add(e.getExecution());
		}
		return executions;
	}

	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {
		return testDao.persistOrAttach(newTest);
	}

	@Override
	public void removeIfUnused(AutomatedTest test) {
		testDao.removeIfUnused(test);
	}

	// **************************** Remote Calls ***********************

	@Override
	public Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects) {

		// 1 : prepare the tasks
		Collection<FetchTestListTask> tasks = prepareAllFetchTestListTasks(projects);

		// 2 : start the tasks
		Collection<FetchTestListFuture> futures = submitAllFetchTestListTasks(tasks);

		// 3 : harvest the results
		return collectAllTestLists(futures);

	}

	@Override
	public void startAutomatedSuite(AutomatedSuite suite) {

		ExtenderSorter sorter = new ExtenderSorter(suite);

		TestAutomationCallbackService securedCallback = new CallbackServiceSecurityWrapper(callbackService);

		while (sorter.hasNext()) {

			Entry<String, Collection<AutomatedExecutionExtender>> extendersByKind = sorter.getNextEntry();

			TestAutomationConnector connector = null;

			try {
				connector = connectorRegistry.getConnectorForKind(extendersByKind.getKey());
				Collection<Couple<AutomatedTest, Map<String, Object>>> tests = collectAutomatedTests(extendersByKind
						.getValue());
				connector.executeParameterizedTests(tests, suite.getId(), securedCallback);
			} catch (UnknownConnectorKind ex) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Test Automation : unknown connector :", ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			} catch (TestAutomationException ex) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Test Automation : an error occured :", ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			}

		}
	}

	@Override
	public AutomatedSuite findAutomatedTestSuiteById(String suiteId) {
		LOGGER.trace("Find AutomatedSuite by Id = " + suiteId);
		return automatedSuiteDao.findById(suiteId);
	}

	@Override
	public void fetchAllResultURL(TestAutomationProject project, AutomatedSuite suite) {

		Collection<AutomatedExecutionExtender> extenders = extenderDao.findAllBySuiteIdAndProjectId(suite.getId(),
				project.getId());

		Collection<AutomatedTest> tests = testDao.findAllByExtender(extenders);

		try {
			TestAutomationConnector connector = connectorRegistry.getConnectorForKind(project.getServer().getKind());

			Map<AutomatedTest, URL> urlMap = connector.getResultURLs(tests, suite.getId());

			_mergeResultURL(urlMap, extenders);

		} catch (UnknownConnectorKind ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(
						"Test Automation : cannot update the result URL for some executions due to unknown connector :",
						ex);
			}
			throw ex;
		}

	}

	// ****************************** fetch test list methods ****************************************

	private Collection<FetchTestListTask> prepareAllFetchTestListTasks(Collection<TestAutomationProject> projects) {
		Collection<FetchTestListTask> tasks = new ArrayList<FetchTestListTask>();

		for (TestAutomationProject project : projects) {
			tasks.add(new FetchTestListTask(connectorRegistry, project));
		}

		return tasks;
	}

	private Collection<FetchTestListFuture> submitAllFetchTestListTasks(Collection<FetchTestListTask> tasks) {

		Collection<FetchTestListFuture> futures = new ArrayList<FetchTestListFuture>();

		for (FetchTestListTask task : tasks) {
			futures.add(executor.sumbitFetchTestListTask(task));
		}

		return futures;
	}

	private Collection<TestAutomationProjectContent> collectAllTestLists(Collection<FetchTestListFuture> futures) {

		Collection<TestAutomationProjectContent> results = new ArrayList<TestAutomationProjectContent>();

		for (FetchTestListFuture future : futures) {

			try {
				TestAutomationProjectContent projectContent = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
				results.add(projectContent);
			} catch (Exception ex) {
				results.add(future.getTask().buildFailedResult(ex));
			}
		}

		return results;

	}

	// ******************* dispatching methods **************************

	private Collection<Couple<AutomatedTest, Map<String, Object>>> collectAutomatedTests(
			Collection<AutomatedExecutionExtender> extenders) {

		Collection<Couple<AutomatedTest, Map<String, Object>>> tests = new ArrayList<Couple<AutomatedTest, Map<String, Object>>>(
				extenders.size());

		for (AutomatedExecutionExtender extender : extenders) {
			tests.add(createAutomatedTestAndParams(extender));
		}

		return tests;

	}

	private Couple<AutomatedTest, Map<String, Object>> createAutomatedTestAndParams(AutomatedExecutionExtender extender) {
		Execution execution = extender.getExecution();

		Collection<CustomFieldValue> tcFields = customFieldValueFinder.findAllCustomFieldValues(execution
				.getReferencedTestCase());
		Collection<CustomFieldValue> iterFields = customFieldValueFinder.findAllCustomFieldValues(execution
				.getIteration());
		Collection<CustomFieldValue> campFields = customFieldValueFinder.findAllCustomFieldValues(execution
				.getCampaign());

		Map<String, Object> params = paramBuilder.get().testCase().addEntity(execution.getReferencedTestCase())
				.addCustomFields(tcFields).iteration().addCustomFields(iterFields).campaign()
				.addCustomFields(campFields).build();

		return new Couple<AutomatedTest, Map<String, Object>>(extender.getAutomatedTest(), params);
	}

	private void notifyExecutionError(Collection<AutomatedExecutionExtender> failedExecExtenders, String message) {
		for (AutomatedExecutionExtender extender : failedExecExtenders) {
			extender.setExecutionStatus(ExecutionStatus.ERROR);
			extender.setResultSummary(message);
		}
	}

	private static class ExtenderSorter {

		private Map<String, Collection<AutomatedExecutionExtender>> extendersByKind;

		private Iterator<Entry<String, Collection<AutomatedExecutionExtender>>> iterator = null;

		public ExtenderSorter(AutomatedSuite suite) {

			extendersByKind = new HashMap<String, Collection<AutomatedExecutionExtender>>(suite.getExecutionExtenders()
					.size());

			for (AutomatedExecutionExtender extender : suite.getExecutionExtenders()) {

				String serverKind = extender.getAutomatedTest().getProject().getServer().getKind();

				register(extender, serverKind);

			}

			iterator = extendersByKind.entrySet().iterator();

		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Map.Entry<String, Collection<AutomatedExecutionExtender>> getNextEntry() {

			return iterator.next();

		}

		private void register(AutomatedExecutionExtender extender, String serverKind) {

			if (!extendersByKind.containsKey(serverKind)) {
				extendersByKind.put(serverKind, new LinkedList<AutomatedExecutionExtender>());
			}

			extendersByKind.get(serverKind).add(extender);

		}

	}

	// ************************* other private stuffs ***********************

	private void _mergeResultURL(Map<AutomatedTest, URL> urlMap, Collection<AutomatedExecutionExtender> extenders) {

		for (AutomatedExecutionExtender ext : extenders) {

			AutomatedTest test = ext.getAutomatedTest();
			URL resultURL = urlMap.get(test);
			ext.setResultURL(resultURL);

		}

	}

	/**
	 * That wrapper is a TestAutomationCallbackService, that ensures that the security context is properly set for any
	 * thread that requires its services.
	 * 
	 * @author bsiri
	 * 
	 */
	private static class CallbackServiceSecurityWrapper implements TestAutomationCallbackService {

		private SecurityContext secContext;

		private TestAutomationCallbackService wrapped;

		/*
		 * the SecurityContext here is the one from the original thread. The others methods will use that instance of
		 * SecurityContext for all their operations from now on (see the code, it's straightforward).
		 */
		CallbackServiceSecurityWrapper(TestAutomationCallbackService service) {
			secContext = SecurityContextHolder.getContext();
			wrapped = service;
		}

		@Override
		public void updateResultURL(AutomatedExecutionSetIdentifier execIdentifier, URL resultURL) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateResultURL(execIdentifier, resultURL);
		}

		@Override
		public void updateExecutionStatus(AutomatedExecutionSetIdentifier execIdentifier, ExecutionStatus newStatus) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateExecutionStatus(execIdentifier, newStatus);

		}

		@Override
		public void updateResultSummary(AutomatedExecutionSetIdentifier execIdentifier, String newSummary) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateResultSummary(execIdentifier, newSummary);
		}

	}

}
