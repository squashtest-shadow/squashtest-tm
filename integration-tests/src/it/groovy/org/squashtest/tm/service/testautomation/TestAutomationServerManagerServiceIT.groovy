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
package org.squashtest.tm.service.testautomation

import javax.inject.Inject

import org.hibernate.context.CurrentSessionContext
import org.hibernate.exception.ConstraintViolationException
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender
import org.squashtest.tm.domain.testautomation.AutomatedSuite
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.internal.customfield.DefaultEditionStatusStrategy
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService

import spock.lang.Specification
import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
public class TestAutomationServerManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestAutomationServerManagerService service

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should delete a test automation server" (){
		given :
		def serverId = 1L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
	}

	@DataSet("TestAutomationServerManagerServiceIT.bound.xml")
	def "should delete a tas bound to a project" (){
		given :
		def serverId = 1L
		def taProjectId = 1L
		def tmProjectId = 1L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
		!found(TestAutomationProject.class, taProjectId)
		GenericProject tmProject = findEntity(GenericProject, tmProjectId)
		tmProject.getTestAutomationServer() == null
		tmProject.getTestAutomationProjects().size() == 0
	}

	@DataSet("TestAutomationServerManagerServiceIT.executed.xml")
	def "should delete a tas with executions" (){
		given :
		def serverId = 11L
		def taProjectId = 10L
		def tmProjectId = 1L
		def tmTestId = 13L
		def taTestId = 12L
		def executionId = 15L
		def automatedSuiteId = "16"
		def automatedExecutionExtenderId = 17L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
		!found(TestAutomationProject.class, taProjectId)
		GenericProject tmProject = findEntity(GenericProject, tmProjectId)
		tmProject.getTestAutomationServer() == null
		tmProject.getTestAutomationProjects().size() == 0
		found(Execution.class, executionId)
		TestCase test = findEntity(TestCase.class, tmTestId)
		test !=null
		test.getAutomatedTest() == null
		found(AutomatedSuite.class, automatedSuiteId)
		AutomatedExecutionExtender aee = findEntity(AutomatedExecutionExtender.class, automatedExecutionExtenderId)
		aee != null
		aee.getResultURL() == null
	}
}
