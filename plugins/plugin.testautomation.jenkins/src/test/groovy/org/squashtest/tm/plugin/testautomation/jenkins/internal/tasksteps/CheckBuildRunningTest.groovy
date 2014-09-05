/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;

import spock.lang.Specification

class CheckBuildRunningTest extends Specification {

	CheckBuildRunning checkRun;
	HttpClient client;
	HttpMethod method;
	JsonParser parser;
	
	BuildAbsoluteId  absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")
	
	def setup(){
		
		client = Mock()
		method = Mock()
		parser = new JsonParser()
		
		checkRun = new CheckBuildRunning()
		checkRun.client = client
		checkRun.method = method
		checkRun.parser = parser;
		
		
	}
	
	
	def "should say that the given build is still running, and need to be checked again"(){
		
		given :
			def json = makeBuildingJson()
			method.getResponseBodyAsString() >> json
		
		when :
			checkRun.perform()
		
		then :
			checkRun.stillBuilding == true
			checkRun.needsRescheduling() == true
		
	}
	
	def "should say that the given build is over"(){
		
		given :
			def json = makeFinishedJson()
			method.getResponseBodyAsString() >> json
		
		when :
			checkRun.perform()
		
		then :
			checkRun.stillBuilding == false
			checkRun.needsRescheduling() == false
		
	}
	
	def makeBuildingJson(){
		return '{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		'{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId",'+
		'"value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
		'"building":true,"number":10}'
	}
	
	def makeFinishedJson(){
		return '{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		'{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId",'+
		'"value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
		'"building":false,"number":10}'
	}
	
}
