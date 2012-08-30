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
package squashtm.testautomation.jenkins.internal.tasksteps

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import spock.lang.Specification
import squashtm.testautomation.jenkins.internal.JsonParser;

class CheckBuildRunningTest extends Specification {

	CheckBuildRunning checkRunning;
	HttpClient client;
	HttpMethod method;
	BuildAbsoluteId absoluteId;
	JsonParser parser;
	
	def setup(){
		
		client = Mock()
		method = Mock()
		parser = new JsonParser()
		
		checkRunning = new CheckBuildQueue()
		checkRunning.client = client
		checkRunning.method = method
		checkRunning.parser = parser;
		
		checkRunning.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")
		
	}
	
	/*
	def "should check that a build is still running and the step needs rescheduling"(){
		
		given :
			
		
		when :
		
		
		then :
			
	}
	
	
	def makeBuildRunningWithout
	*/
}
