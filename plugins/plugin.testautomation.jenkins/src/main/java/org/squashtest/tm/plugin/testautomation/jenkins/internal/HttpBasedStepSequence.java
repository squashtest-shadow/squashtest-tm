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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildQueue;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildRunning;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.HttpBasedStep;

public abstract class HttpBasedStepSequence {

	protected HttpRequestFactory requestFactory = new HttpRequestFactory();
	
	protected JsonParser jsonParser = new JsonParser();
	
	protected BuildStage currentStage = BuildStage.WAITING;
	
		
	// ********* to be configured ************
	
	protected HttpClient client;
	
	protected TestAutomationProject project;
	
	protected BuildAbsoluteId absoluteId;
	
	
	
	// ************* setters **************
	
	void setClient(HttpClient client) {
		this.client = client;
	}

	
	void setProject(TestAutomationProject project) {
		this.project = project;
	}
	
	
	void setAbsoluteId(BuildAbsoluteId absoluteId) {
		this.absoluteId = absoluteId;
	}
	
	// *********** getters *******************
	
	abstract protected AbstractBuildProcessor getProcessor();
	
	
	// *********** useful methods ************

	
	protected CheckBuildQueue newCheckQueue(){
		
		GetMethod method = requestFactory.newCheckQueue(project);
		
		CheckBuildQueue checkQueue = new CheckBuildQueue(getProcessor()); 
		
		wireHttpSteps(checkQueue, method);
		
		return checkQueue;
	}
	
	
	protected GetBuildID newGetBuildID(){
		
		GetMethod method = requestFactory.newGetBuildsForProject(project);
		
		GetBuildID getBuildID = new GetBuildID(getProcessor());
		
		wireHttpSteps(getBuildID, method);
		
		return getBuildID;
		
	}
	
	protected CheckBuildRunning newCheckBuildRunning(){
		
		GetMethod method = requestFactory.newGetBuild(project, absoluteId.getBuildId());
		
		CheckBuildRunning running = new CheckBuildRunning(getProcessor());
		
		wireHttpSteps(running, method);
		
		return running;
	}
	
	protected GatherTestList newGatherResults(){
		
		GetMethod method = requestFactory.newGetBuildResults(project, absoluteId.getBuildId());
		
		GatherTestList gatherList = new GatherTestList(getProcessor());
		
		wireHttpSteps(gatherList, method);
		
		return gatherList; 
		
	}
	
	
	protected void wireHttpSteps(HttpBasedStep step, HttpMethod method){
		step.setClient(client);
		step.setMethod(method);
		step.setParser(jsonParser);
		step.setBuildAbsoluteId(absoluteId);		
	}
	
	
}
