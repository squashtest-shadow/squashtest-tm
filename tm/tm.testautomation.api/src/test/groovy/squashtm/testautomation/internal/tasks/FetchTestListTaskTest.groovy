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
package squashtm.testautomation.internal.tasks

import spock.lang.Specification
import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.domain.TestAutomationTest;
import squashtm.testautomation.internal.service.TestAutomationConnectorRegistry;
import squashtm.testautomation.model.TestAutomationProjectContent;
import squashtm.testautomation.spi.TestAutomationConnector;

class FetchTestListTaskTest extends Specification {

	def "should do the job"(){
		
		given :
			TestAutomationProject project = Mock()
			TestAutomationServer server = Mock()
			
			project.getServer() >> server
			
		and :
			Collection<TestAutomationTest> allTests = []
			
		and :
			TestAutomationConnector connector = Mock()
			TestAutomationConnectorRegistry registry = Mock()
			
			
			registry.getConnectorForKind(_) >> connector
			connector.listTestsInProject(project) >> allTests
			
		when :
			def task = new FetchTestListTask(registry, project)
			def res = task.call();
		
		then :
			res.project == project
			res.tests == allTests
			res.hadKnownProblems() == false
	}
	
	def "should do a failed job"(){
		
		given :
			TestAutomationProject project = Mock()
			TestAutomationServer server = Mock()
			
		and :
			project.getServer() >> server
			project.getName() >> "project"
			server.getBaseURL() >> new URL("http://www.mike.com") 
			
			Exception ex = new Exception()
			
		when :
			def task = new FetchTestListTask(null, project)
			def res = task.buildFailedResult(ex)
			
		then :
			res.project == project
			res.hadKnownProblems()
			res.knownProblem == ex
		
	}
	
}
