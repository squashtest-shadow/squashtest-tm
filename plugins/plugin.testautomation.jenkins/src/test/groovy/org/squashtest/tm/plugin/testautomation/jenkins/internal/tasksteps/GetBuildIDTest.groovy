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

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;

import spock.lang.Specification

class GetBuildIDTest extends Specification {

	GetBuildID getID;
	CloseableHttpClient client;
	HttpUriRequest method;
	BuildAbsoluteId absoluteId;
	JsonParser parser;
	
	def setup(){
		
		client = Mock()
		method = Mock()
		parser = new JsonParser()
		
		getID = new GetBuildID()
		getID.client = client
		getID.method = method
		getID.parser = parser;
		
		getID.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")
		
	}
	

	def "should get the id of a build"(){
		
		given :
			def json = makeBuildListForProject()
			method.getResponseBodyAsString() >> json
			
		when :
			getID.perform()
		
		then :
			getID.absoluteId.buildId == 8
			
	}
	
	
	def makeBuildListForProject(){
		return '{"builds":[{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121028"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{},{}],'+
				'"building":false,"number":11},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121035"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],'+
				'"building":false,"number":10},{"actions":[{"parameters":[{"name":"operation","value":"test-list"},'+
				'{"name":"externalJobId","value":"300820121025"},{"name":"callerId","value":"anonymous@example.com"},'+
				'{"name":"notificationURL","value":"file://dev/null"},{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":9},'+
				'{"actions":[{"parameters":[{"name":"operation","value":"test-list"},{"name":"externalJobId","value":"CorrectExternalID"},'+
				'{"name":"callerId","value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},'+
				'{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":8},'+
				'{"actions":[{"parameters":[{"name":"operation","value":"test-list"},{"name":"externalJobId","value":"240820121832"},'+
				'{"name":"callerId","value":"anonymous@example.com"},{"name":"notificationURL","value":"file://dev/null"},'+
				'{"name":"testList","value":"**/*"}]},{},{}],"building":false,"number":7}]}'
	}

}
