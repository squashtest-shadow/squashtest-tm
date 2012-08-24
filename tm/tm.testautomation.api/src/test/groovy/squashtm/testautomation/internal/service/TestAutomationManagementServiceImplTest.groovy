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
package squashtm.testautomation.internal.service

import spock.lang.Specification;
import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.internal.tasks.FetchTestListTask;
import squashtm.testautomation.internal.thread.FetchTestListFuture;
import squashtm.testautomation.internal.thread.TestAutomationTaskExecutor;
import squashtm.testautomation.model.TestAutomationProjectContent;
import squashtm.testautomation.repository.TestAutomationServerDao;
import squashtm.testautomation.spi.TestAutomationConnector;


class TestAutomationManagementServiceImplTest extends Specification {

	TestAutomationServerDao serverDao;
	
	TestAutomationConnectorRegistry connectorRegistry;
	
	TestAutomationManagementServiceImpl service;
	
	TestAutomationTaskExecutor executor;
	
	def setup(){
		serverDao = Mock()
		connectorRegistry = Mock()
		executor = Mock()
		service = new TestAutomationManagementServiceImpl()
		service.serverDao = serverDao
		service.connectorRegistry = connectorRegistry
		service.executor = executor;
	}

	
	def "should return a list of projects refering to a server object"(){
		
		given :
			def proj1 = new TestAutomationProject("proj1", null)
			def proj2 = new TestAutomationProject("proj2", null)
			def proj3 = new TestAutomationProject("proj3", null)
		
		and :
			TestAutomationConnector connector = Mock()
			connector.listProjectsOnServer(_) >> [ proj1, proj2, proj3 ] 
			
		and :
			connectorRegistry.getConnectorForKind(_) >> connector
			
		and :
			def server = new TestAutomationServer(new URL("http://www.toto.com"), "toto", "toto")
			
		when :
			def res = service.listProjectsOnServer(server)
			
		then :
			
			//the collection contains three elements
			res.size()==3									

			//all of the elements refer to the same server instance :
			res.collect{it.server}.unique().size() == 1	
			
			//the elements have the specified names :
			res.collect{it.name} == ["proj1", "proj2", "proj3"]
	}
		
	
	def "should build a bunch of tasks to fetch the test lists"(){
		
		given :
			List<TestAutomationProject> projects = [ Mock(TestAutomationProject),
													 Mock(TestAutomationProject),
													 Mock(TestAutomationProject)
												   ]
			
		when :
			def res = service.prepareAllFetchTestListTasks(projects)
			
		then :
			res.collect { 
				[it.project, it.connectorRegistry] 
			} == [
				[projects[0], connectorRegistry],
				[projects[1], connectorRegistry],
				[projects[2], connectorRegistry]				
			]
	}
	
	def "should submit a bunch of tasks"(){
		
		given :
			List<FetchTestListTask> tasks = [ Mock(FetchTestListTask),
											  Mock(FetchTestListTask),
											  Mock(FetchTestListTask)
											]
			
		and :
			List<FetchTestListFuture> futures = [ Mock(FetchTestListFuture),
												  Mock(FetchTestListFuture),
												  Mock(FetchTestListFuture)				
												]				
			
		and :
			executor.sumbitFetchTestListTask(tasks[0]) >> futures[0]
			executor.sumbitFetchTestListTask(tasks[1]) >> futures[1]
			executor.sumbitFetchTestListTask(tasks[2]) >> futures[2]
			
		when :
			def res = service.submitAllFetchTestListTasks(tasks)
			
		then :
			res == futures
			
	}
	
	def "should collect test list results"(){
		
		given :
			TestAutomationProjectContent content1 = Mock()
			TestAutomationProjectContent content2 = Mock()
		
		and :
			FetchTestListFuture fut1 =  Mock()
			fut1.get(_,_) >> content1
			
			FetchTestListTask task2 = Mock()
			task2.buildFailedResult(_) >> content2 
			FetchTestListFuture fut2 = Mock()
			
			fut2.getTask() >> task2
			fut2.get(_,_) >> { throw new Exception() }
			
		when :
			def res = service.collectAllTestLists([fut1, fut2])
			
		then :
			res == [content1, content2]
		
	}
	
}
