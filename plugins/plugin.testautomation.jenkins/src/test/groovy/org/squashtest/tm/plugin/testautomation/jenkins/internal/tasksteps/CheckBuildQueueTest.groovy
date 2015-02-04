/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;

import spock.lang.Specification

class CheckBuildQueueTest extends Specification {

	CheckBuildQueue checkQueue;
	HttpClient client;
	HttpMethod method;
	BuildAbsoluteId absoluteId;
	JsonParser parser;
	
	
	def setup(){
		
		client = Mock()
		method = Mock()
		parser = new JsonParser()
		
		checkQueue = new CheckBuildQueue()
		checkQueue.client = client
		checkQueue.method = method
		checkQueue.parser = parser;
		
		checkQueue.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")
	}
	
	
	def "should check that a given build is not queued"(){
		
		given :
			def json = makeQueueWithoutThatBuild()
			method.getResponseBodyAsString() >> json
		
		when :
			checkQueue.perform()
		
		then :
			checkQueue.buildIsQueued == false
			checkQueue.needsRescheduling() == false
		
	}
	
	def "should check that the given build is queued and the step needs rescheduling while it is"(){

		given :
			def json = makeQueueWithThatBuild()
			method.getResponseBodyAsString() >> json
		
		when :
			checkQueue.perform()
		
		then :
			checkQueue.buildIsQueued == true
			checkQueue.needsRescheduling() == true
	}
	
	def "should check that the given build is not queued because the queue is empty"(){
		
		given :
			method.getResponseBodyAsString() >> '{"items":[]}'
			
		when :
			checkQueue.perform()
			
		then :
			checkQueue.buildIsQueued == false
			checkQueue.needsRescheduling() == false
		
	}
	
	
	def makeQueueWithoutThatBuild(){
		
		return '{"items":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
		       '{"name":"externalJobId","value":"WrongExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],'+
			   '"id":4,"task":{"name":"CorrectJob"}},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],"id":13,'+
			   '"task":{"name":"WrongJob"}}]}'
		
		
	}

	
	def makeQueueWithThatBuild(){
		
		return '{"items":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"WrongExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],'+
			   '"id":4,"task":{"name":"CorrectJob"}},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
			   '{"name":"externalJobId","value":"CorrectExternalID"},{"name":"callerId","value":"anonymous@example.com"},'+
			   '{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{}],"id":13,'+
			   '"task":{"name":"CorrectJob"}}]}'
		
		
	}
	
}


