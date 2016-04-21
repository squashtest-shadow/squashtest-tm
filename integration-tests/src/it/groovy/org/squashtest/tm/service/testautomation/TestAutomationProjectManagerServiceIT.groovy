/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService

import spock.lang.Specification
import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
public class TestAutomationProjectManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	TestAutomationProjectManagerService service

	@DataSet("TestAutomationService.sandbox.xml")
	def "should persist a new TestAutomationProject"(){

		given :
		def server = getServer(-1L)
		def project = new TestAutomationProject("roberto5","Project Roberto 5" , server)
		def tmproject = getProject(-1L)
		project.setTmProject(tmproject)

		when :
		service.persist(project)
		then :
		project.id != null
		project.label == "Project Roberto 5"
		project.jobName == "roberto5"
		project.server.id == -1L
	}


	@DataSet("TestAutomationService.sandbox.xml")
	def "should say that a project label is ok"(){
		given :
		def server  = getServer(-2L)
		def project = new TestAutomationProject("whatever","Project Mike 2",  server)
		def tmproject = getProject(-1L)
		project.setTmProject(tmproject)

		when :
		service.persist(project)
		session.flush()

		then :
		notThrown ConstraintViolationException
	}

	@DataSet("TestAutomationService.sandbox.xml")
	def "should say that a project label is not unique"(){
		given :
		def server  = getServer(-2L)
		def project = new TestAutomationProject("whatever","Project Mike 2",  server)
		def tmproject = getProject(-2L)
		project.setTmProject(tmproject)

		when :
		service.persist(project)
		session.flush()


		then :
		thrown ConstraintViolationException
	}



	def getServer(id){
		return getSession().load(TestAutomationServer.class, id)
	}

	def getProject(id){
		return getSession().load(GenericProject.class, id)
	}
}
