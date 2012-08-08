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
package org.squashtest.csp.tm.internal.service

import javax.inject.Inject;

import org.apache.poi.hssf.record.formula.functions.T
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer
import org.squashtest.csp.tm.service.TestAutomationManagementService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestAutomationManagementServiceImplIT extends DbunitServiceSpecification {

	@Inject
	TestAutomationManagementService service
	
	
	
	@DataSet("TestAutomationService.sandbox.xml")
	def "should persist a new TestAutomationProject along with a new TestAutomationServer"(){
		
		given :
			def server = new TestAutomationServer(new URL("http://www.bobinio.com"), "bobinio", "passbobinio")
			def project = new TestAutomationProject("bobinio1", server)
		
		when :
			def res = service.registerNewProject(project)
		
		then :
			res.id!=null
			res.name=="bobinio1"
			
			def reserver = res.server
			reserver.id != null
			reserver.baseURL == new URL("http://www.bobinio.com")
			reserver.login == "bobinio"
			reserver.password == "passbobinio"
			reserver.kind == "jenkins"
		
	}
	
	@DataSet("TestAutomationService.sandbox.xml")
	def "should persist a new TestAutomationProject hosted on a known TestAutomationServer"(){
		given :
			def server = new TestAutomationServer(new URL("http://www.roberto.com"), "roberto", "passroberto")
			def project = new TestAutomationProject("roberto5", server)
			
		when :
			def res = service.registerNewProject(project)

		then :
			res.id != null
			res.name == "roberto5"
			
			res.server.id == 1l	//that instance existed already
	}
	
	@DataSet("TestAutomationService.sandbox.xml")
	def "should persist a new TestAutomationProject very similar to a known one, but hosted on a different server"(){
		
		given :
			def server  = new TestAutomationServer(new URL("http://www.roberto.com"), "roberto_user", "passroberto_user")
			def project = new TestAutomationProject("roberto1", server)
			
		when :
			def res = service.registerNewProject(project)
		
		then :
			res.id!=null
			res.id!=11l
			res.name=="roberto1"
			
			res.server.id == 3l 
		
	}
	
	
	@DataSet("TestAutomationService.sandbox.xml")
	def "should not persist the arguments because they happen to exist already"(){
		
		given :
			def server  = new TestAutomationServer(new URL("http://www.mike.com"), "mike", "passmike", "something-else")
			def project = new TestAutomationProject("mike2", server)
			
		when :
			def res = service.registerNewProject(project)
		
		then :
			res.id == 22l
			res.server.id == 2l
		
	}
}
