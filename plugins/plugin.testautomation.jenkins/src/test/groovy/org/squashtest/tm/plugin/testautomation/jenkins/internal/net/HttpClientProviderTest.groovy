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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net


import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider.ServerKey

import spock.lang.Specification

class HttpClientProviderTest extends Specification {

	// ********************* tests for ServerKey : is it fit for being a key in a map ?*************** 
	
	
	def "keys generated from different instances of the 'same' TestAutomationServer should be equal"(){
		
		given :
			TestAutomationServer server1 = new TestAutomationServer("server1", new URL("http://www.toto.com"), "toto", "toto", "jenkins")
			TestAutomationServer server2 = new TestAutomationServer("server1", new URL("http://www.toto.com"), "toto", "toto", "jenkins")
		
			
		when :
			def key1 = new ServerKey(server1)
			def key2 = new ServerKey(server2)
			
		then :
			key1.equals(key2)
		
	}	
	
	def "keys generated from two different TestAutomationServer should not be equal"(){
		given :
			TestAutomationServer server1 = new TestAutomationServer("server1", new URL("http://www.toto.com"), "toto", "toto", "jenkins")
			TestAutomationServer server2 = new TestAutomationServer("server2", new URL("http://www.titi.com"), "titi", "titi", "jenkins")
		
			
		when :
			def key1 = new ServerKey(server1)
			def key2 = new ServerKey(server2)
			
		then :
			! key1.equals(key2)
	}
}
