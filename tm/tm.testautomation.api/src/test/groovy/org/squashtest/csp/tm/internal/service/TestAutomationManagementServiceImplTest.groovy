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

import spock.lang.Specification;
import squashtm.testautomation.spi.TestAutomationConnector;

import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.internal.repository.TestAutomationServerDao;

class TestAutomationManagementServiceImplTest extends Specification {

	TestAutomationServerDao serverDao;
	
	TestAutomationConnectorRegistry connectorRegistry;
	
	TestAutomationManagementServiceImpl service;
	
	def setup(){
		serverDao = Mock()
		connectorRegistry = Mock()
		service = new TestAutomationManagementServiceImpl()
		service.serverDao = serverDao
		service.connectorRegistry = connectorRegistry
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
		
}
