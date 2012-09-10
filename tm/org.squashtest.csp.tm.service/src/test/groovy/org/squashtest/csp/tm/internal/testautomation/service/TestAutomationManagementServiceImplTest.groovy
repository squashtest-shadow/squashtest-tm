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
package org.squashtest.csp.tm.internal.testautomation.service

import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.internal.repository.TestAutomationServerDao;
import org.squashtest.csp.tm.internal.testautomation.service.TestAutomationConnectorRegistry;
import org.squashtest.csp.tm.internal.testautomation.service.TestAutomationManagementServiceImpl;
import org.squashtest.csp.tm.internal.testautomation.service.TestAutomationManagementServiceImpl.ExtenderSorter;
import org.squashtest.csp.tm.internal.testautomation.tasks.FetchTestListTask;
import org.squashtest.csp.tm.internal.testautomation.thread.FetchTestListFuture;
import org.squashtest.csp.tm.internal.testautomation.thread.TestAutomationTaskExecutor;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;
import org.squashtest.csp.tm.testautomation.spi.TestAutomationConnector;
import org.squashtest.csp.tm.testautomation.spi.UnknownConnectorKind;

import spock.lang.Specification;


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
	
	def "extender sorter should sort extenders "(){
		
		given :
			AutomatedSuite suite = makeSomeSuite()
		
		when :
			def sorter = new ExtenderSorter(suite)
		
		then :
			def col1 = sorter.getNextEntry();
			def col2 = sorter.getNextEntry();
			
			col1.key == "jenkins"
			col2.key == "qc"
			
			col1.value.size() == 10
			col1.value.collect{ it.automatedTest.project }.unique().collect{ it.name} as Set == ["project-jenkins-1", "project-jenkins-2"] as Set
			
			col2.value.size() == 5
			col2.value.collect{ it.automatedTest.project}.unique().collect{ it.name} as Set == ["project-qc-1"] as Set 
		
	}
	
	
	def "should collect tests from extender list"(){
		
		given :
			
			def tests = [new AutomatedTest("bob", null), new AutomatedTest("mike", null), new AutomatedTest("robert", null) ]
		
			def exts = []
			
			tests.each{
				def ex = new AutomatedExecutionExtender()
				ex.automatedTest = it
				exts << ex
			}
			
		when :
		
			def res = service.collectAutomatedTests(exts)
		
		then :
			res == tests
	}
	
	
	def "should start some tests"(){
		
		given :
			AutomatedSuite suite = makeSomeSuite()
		
		and :
			def jenConnector = Mock(TestAutomationConnector)
			def qcConnector = Mock(TestAutomationConnector)

		when :
			service.startAutomatedSuite(suite)
		
		then :
			
			1 * connectorRegistry.getConnectorForKind("jenkins") >> jenConnector
			1 * connectorRegistry.getConnectorForKind("qc") >> qcConnector
			
			1 * jenConnector.executeTests(_, "12345")
			1 * qcConnector.executeTests(_, "12345")
	
	}
	
	
	def "should notify some executions that an error occured before they could start"(){
		
		given :
			AutomatedSuite suite = makeSomeSuite()
			
			suite.executionExtenders.each{
		
				def exec = new Execution()
				exec.automatedExecutionExtender = it
				it.execution = exec
				
			} 
		
		and :		
			def jenConnector = Mock(TestAutomationConnector)
			def qcConnector = Mock(TestAutomationConnector)
			
			connectorRegistry.getConnectorForKind("jenkins") >> jenConnector
			connectorRegistry.getConnectorForKind("qc") >> { throw new UnknownConnectorKind("connector unknown") }
			
		when :
			service.startAutomatedSuite(suite)	
		
		
		then :
			1 * jenConnector.executeTests(_, "12345")
			
			def executions = suite.executionExtenders.collect{it.execution}
			executions.findAll{it.executionStatus == ExecutionStatus.ERROR }.size() == 5
			executions.findAll{it.executionStatus == ExecutionStatus.READY }.size() == 10
		
		
	}
	
	
	def makeSomeSuite(){
		
		AutomatedSuite suite = new AutomatedSuite();
		suite.id = "12345"
		
		TestAutomationServer serverJenkins = new TestAutomationServer(new URL("http://jenkins-ta"), "jenkins");
		TestAutomationServer serverQC = new TestAutomationServer(new URL("http://qc-ta"), "qc");
		
		TestAutomationProject projectJ1 = new TestAutomationProject("project-jenkins-1", serverJenkins)
		TestAutomationProject projectQC1 = new TestAutomationProject("project-qc-1", serverQC)
		TestAutomationProject projectJ2 = new TestAutomationProject("project-jenkins-2", serverJenkins)		
		
		def allTests = []
		
		def projects = [projectJ1, projectQC1, projectJ2]
		
		projects.each{ proj ->
			
			5.times{ num ->
				
				AutomatedTest test = new AutomatedTest("${proj.name} - test $num", proj)
				allTests << test			
			}			
		}
		
		def allExts = [];
		
		allTests.each{
			
			def ex = new AutomatedExecutionExtender()
			ex.automatedTest = it
			
			allExts << ex
			
		}
		
		suite.addExtenders(allExts)
		
		return suite
		
		
	}
	
}
