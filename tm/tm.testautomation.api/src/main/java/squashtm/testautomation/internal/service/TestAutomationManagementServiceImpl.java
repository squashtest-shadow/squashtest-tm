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
package squashtm.testautomation.internal.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.transaction.annotation.Transactional;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.internal.tasks.FetchTestListTask;
import squashtm.testautomation.internal.tasks.TestAutomationConnectorTask;
import squashtm.testautomation.internal.thread.FetchTestListFuture;
import squashtm.testautomation.internal.thread.TestAutomationTaskExecutor;
import squashtm.testautomation.model.TestAutomationProjectContent;
import squashtm.testautomation.repository.TestAutomationProjectDao;
import squashtm.testautomation.repository.TestAutomationServerDao;
import squashtm.testautomation.spi.TestAutomationConnector;

@Transactional
public class TestAutomationManagementServiceImpl implements  InsecureTestAutomationManagementService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationManagementServiceImpl.class);
	private static final int DEFAULT_THREAD_TIMEOUT = 30000;	//timeout as milliseconds
	
	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;

	@Inject
	private TestAutomationServerDao serverDao;
	
	@Inject
	private TestAutomationProjectDao projectDao;
	
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
		
		return connector.listProjectsOnServer(server);
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

	
	//from the insecure interface
	@Override
	public TestAutomationProject persistOrAttach(TestAutomationProject newProject) {
		
		TestAutomationServer inBaseServer = serverDao.uniquePersist(newProject.getServer());
				
		return projectDao.uniquePersist(newProject.newWithServer(inBaseServer));
		
	}


	//from the insecure interface
	@Override
	public TestAutomationServer getDefaultServer() {
		return defaultServer;
	}
	
	
	
	//****************************** private methods ****************************************
	
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


	


}
