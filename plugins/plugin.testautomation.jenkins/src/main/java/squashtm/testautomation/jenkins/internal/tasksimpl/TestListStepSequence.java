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
package squashtm.testautomation.jenkins.internal.tasksimpl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.HttpRequestFactory;
import squashtm.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import squashtm.testautomation.jenkins.internal.tasks.StepSequence;
import squashtm.testautomation.jenkins.internal.tasks.BuildStep;

public class TestListStepSequence implements StepSequence {

	private HttpRequestFactory requestFactory;
	
	private HttpClient client;
	
	private JsonParser jsonParser;
	
	private TestAutomationProject project;
	
	private AbstractBuildProcessor<?> processor;
	
	
	private BuildStage currentStage;
		
	
	// ************* setters **************
	
	public void setRequestFactory(HttpRequestFactory requestFactory) {
		this.requestFactory = requestFactory;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setJsonParser(JsonParser jsonParser) {
		this.jsonParser = jsonParser;
	}


	public void setProject(TestAutomationProject project) {
		this.project = project;
	}

	public TestListStepSequence(AbstractBuildProcessor<?> processor) {
		super();
		this.processor=processor;
	}
	
	
	

	@Override
	public boolean hasMoreElements() {
		return (currentStage != BuildStage.GATHER_RESULT);
	}

	@Override
	public BuildStep nextElement() {
		// TODO Auto-generated method stub
		return null;
	}
	

	//************** private stuffs ****************
	
	private StartBuild newStartBuild(){
		
		PostMethod method = requestFactory.newStartFetchTestListBuild(project, processor.getExternalId());
		
		StartBuild startBuild = new StartBuild();
		
		startBuild.setClient(client);
		startBuild.setMethod(method);
		startBuild.setBuildProcessor(processor);
		
		return startBuild;
		
	}
	
	private CheckBuildQueue newCheckQueue(){
		
		GetMethod method = requestFactory.newCheckQueue(project);
		
		CheckBuildQueue checkQueue = new CheckBuildQueue(); 
		
		checkQueue.setClient(client);
		checkQueue.setMethod(method);
		checkQueue.setBuildProcessor(processor);
		checkQueue.setParser(jsonParser);
		checkQueue.setExternalId(processor.getExternalId());
		checkQueue.setProjectName(project.getName());
		checkQueue.setDefaultReschedulingDelay(processor.getDefaultReschedulingDelay());
		
		return checkQueue;
	}
	
	private static enum BuildStage{
		START_BUILD,
		CHECK_QUEUE,
		CHECK_RUNNING,
		GATHER_RESULT
	}
	
	
}
