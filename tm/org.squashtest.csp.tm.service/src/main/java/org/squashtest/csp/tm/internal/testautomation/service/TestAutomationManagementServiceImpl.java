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
package org.squashtest.csp.tm.internal.testautomation.service;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.internal.repository.AutomatedSuiteDao;
import org.squashtest.csp.tm.internal.repository.TestAutomationProjectDao;
import org.squashtest.csp.tm.internal.repository.TestAutomationServerDao;
import org.squashtest.csp.tm.internal.repository.AutomatedTestDao;
import org.squashtest.csp.tm.internal.testautomation.tasks.FetchTestListTask;
import org.squashtest.csp.tm.internal.testautomation.thread.FetchTestListFuture;
import org.squashtest.csp.tm.internal.testautomation.thread.TestAutomationTaskExecutor;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;
import org.squashtest.csp.tm.testautomation.spi.TestAutomationConnector;
import org.squashtest.csp.tm.testautomation.spi.TestAutomationException;
import org.squashtest.csp.tm.testautomation.spi.UnknownConnectorKind;


@Transactional
@Service("squashtest.tm.service.TestAutomationService")
public class TestAutomationManagementServiceImpl implements  InsecureTestAutomationManagementService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);
	private static final int DEFAULT_THREAD_TIMEOUT = 30000;	//timeout as milliseconds
	
	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;

	@Inject
	private TestAutomationServerDao serverDao;
	
	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private AutomatedSuiteDao automatedSuiteDao;
	
	@Inject
	private AutomatedTestDao testDao;
	
	
	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;
	
	@Inject
	private TestAutomationServer defaultServer;

	private TestAutomationTaskExecutor executor ;

	
	@ServiceReference
	public void setAsyncTaskExecutor(AsyncTaskExecutor executor){
		TestAutomationTaskExecutor taExecutor = new TestAutomationTaskExecutor(executor);
		this.executor=taExecutor;
	}
	
	
	
	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}



	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) {
		
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		
		connector.checkCredentials(server);	
		try{
			return connector.listProjectsOnServer(server);
		}
		catch(TestAutomationException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error("Test Automation : failed to list projects on server : ",ex);
			}
			throw ex;
		}
	}
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(URL serverURL, String login, String password) {
			
		TestAutomationServer server = new TestAutomationServer(serverURL, login, password);
		
		return listProjectsOnServer(server);
		
	}
	
	
	@Override
	public Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects) {
		
		//1 : prepare the tasks
		Collection<FetchTestListTask> tasks = prepareAllFetchTestListTasks(projects);
		
		//2 : start the tasks
		Collection<FetchTestListFuture> futures = submitAllFetchTestListTasks(tasks); 
		
		//3 : harvest the results
		return collectAllTestLists(futures); 
		
	}

	
	@Override
	public List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId) {

		List<Execution> executions = new ArrayList<Execution>();
		AutomatedSuite suite = automatedSuiteDao.findById(automatedTestSuiteId);
		for(AutomatedExecutionExtender e : suite.getExecutionExtenders()) {
			executions.add(e.getExecution());
		}
		return executions;
	}
	
	//from the insecure interface
	@Override
	public TestAutomationProject persistOrAttach(TestAutomationProject newProject) {
		
		TestAutomationServer inBaseServer = serverDao.uniquePersist(newProject.getServer());
				
		return projectDao.uniquePersist(newProject.newWithServer(inBaseServer));
		
	}
	
	
	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {
		return testDao.uniquePersist(newTest);
	}
	
	
	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}
	
	
	@Override
	public AutomatedTest findTestById(long testId) {
		return testDao.findById(testId);
	}
	


	//from the insecure interface
	@Override
	public TestAutomationServer getDefaultServer() {
		return defaultServer;
	}
	

	@Override
	public void startAutomatedSuite(AutomatedSuite suite) {
		
		ExtenderSorter sorter = new ExtenderSorter(suite);
		
		while (sorter.hasNext()){
			
			Entry<String, Collection<AutomatedExecutionExtender>> extendersByKind = sorter.getNextEntry();
			
			TestAutomationConnector connector = null;
			
			try{
				connector = connectorRegistry.getConnectorForKind(extendersByKind.getKey());
				Collection<AutomatedTest> tests = collectAutomatedTests(extendersByKind.getValue());
				connector.executeTests(tests, suite.getId());
			}
			catch(UnknownConnectorKind ex){
				if (LOGGER.isErrorEnabled()){
					LOGGER.error("Test Automation : unknown connector :",ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			}
			catch(TestAutomationException ex){
				if (LOGGER.isErrorEnabled()){
					LOGGER.error("Test Automation : an error occured :",ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			}
			
		}
	}


	
	//****************************** fetch test list methods ****************************************
	
	private Collection<FetchTestListTask> prepareAllFetchTestListTasks(Collection<TestAutomationProject> projects){
		Collection<FetchTestListTask> tasks = new ArrayList<FetchTestListTask>();
		
		for (TestAutomationProject project : projects){
			tasks.add(new FetchTestListTask(connectorRegistry, project));
		}
		
		return tasks;
	}
	
	private Collection<FetchTestListFuture> submitAllFetchTestListTasks(Collection<FetchTestListTask> tasks){
		
		Collection<FetchTestListFuture> futures = new ArrayList<FetchTestListFuture>();
		
		for (FetchTestListTask task : tasks){
			futures.add(executor.sumbitFetchTestListTask(task));
		}
		
		return futures;
	}
	
	
	private Collection<TestAutomationProjectContent> collectAllTestLists(Collection<FetchTestListFuture> futures){
		
		Collection<TestAutomationProjectContent> results = new ArrayList<TestAutomationProjectContent>();
		
		for (FetchTestListFuture  future : futures){
			
			try {
				TestAutomationProjectContent projectContent = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
				results.add(projectContent);
			} 
			catch(Exception ex){
				results.add(future.getTask().buildFailedResult(ex));
			}
		}
		
		return results;
		
	}


	// ******************* dispatching methods **************************

	
	
	private Collection<AutomatedTest> collectAutomatedTests(Collection<AutomatedExecutionExtender> extenders){
		
		Collection<AutomatedTest> tests = new LinkedList<AutomatedTest>();
		
		for (AutomatedExecutionExtender extender : extenders){
			
			tests.add(extender.getAutomatedTest());
			
		}
		
		return tests;
		
	}

	
	private void notifyExecutionError(Collection<AutomatedExecutionExtender> failedExecExtenders, String message){
		for (AutomatedExecutionExtender extender : failedExecExtenders){
			extender.setExecutionStatus(ExecutionStatus.ERROR);
			extender.setResultSummary(message);
		}
	}
	
	private static class ExtenderSorter{
		
		private Map<String, Collection<AutomatedExecutionExtender>> extendersByKind
			= new HashMap<String, Collection<AutomatedExecutionExtender>>();
		
		private Iterator<Entry<String, Collection<AutomatedExecutionExtender>>> iterator =null;
		
		
		public ExtenderSorter(AutomatedSuite suite){
			
			for (AutomatedExecutionExtender extender : suite.getExecutionExtenders()){
				
				String serverKind = extender.getAutomatedTest().getProject().getServer().getKind();
				
				register(extender, serverKind);
			
			}	
			
			iterator = extendersByKind.entrySet().iterator();
			
		}
		
		public boolean hasNext(){
			return iterator.hasNext();
		}
		
		public Map.Entry<String, Collection<AutomatedExecutionExtender>> getNextEntry(){
			
			return iterator.next();
					
		}
		
		private void register(AutomatedExecutionExtender extender, String serverKind){
			
			if (! extendersByKind.containsKey(serverKind)){
				extendersByKind.put(serverKind, new LinkedList<AutomatedExecutionExtender>());
			}
			
			extendersByKind.get(serverKind).add(extender);
			
		}
		
	}

	@Override
	public AutomatedSuite findAutomatedTestSuiteById(String suiteId) {
		LOGGER.trace("Find AutomatedSuite by Id = "+suiteId);
		return automatedSuiteDao.findById(suiteId);
	}

}
