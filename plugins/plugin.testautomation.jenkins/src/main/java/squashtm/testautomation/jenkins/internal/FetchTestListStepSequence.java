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
package squashtm.testautomation.jenkins.internal;

import java.util.NoSuchElementException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.jenkins.internal.net.HttpRequestFactory;
import squashtm.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import squashtm.testautomation.jenkins.internal.tasks.BuildStep;
import squashtm.testautomation.jenkins.internal.tasks.StepSequence;
import squashtm.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import squashtm.testautomation.jenkins.internal.tasksteps.CheckBuildQueue;
import squashtm.testautomation.jenkins.internal.tasksteps.CheckBuildRunning;
import squashtm.testautomation.jenkins.internal.tasksteps.GatherTestList;
import squashtm.testautomation.jenkins.internal.tasksteps.GetBuildID;
import squashtm.testautomation.jenkins.internal.tasksteps.HttpBasedStep;
import squashtm.testautomation.jenkins.internal.tasksteps.StartBuild;

class FetchTestListStepSequence implements StepSequence {

	private HttpRequestFactory requestFactory = new HttpRequestFactory();
	
	private JsonParser jsonParser = new JsonParser();
	
	private BuildStage currentStage = BuildStage.WAITING;
	
	// ********* to be configured ************
	
	private HttpClient client;
	
	private TestAutomationProject project;
	
	private BuildAbsoluteId absoluteId;
	
	private AbstractBuildProcessor<?> processor;
	
	
	// ************** constructor ****************
		
	FetchTestListStepSequence(AbstractBuildProcessor<?> processor) {
		super();
		this.processor=processor;
	}
	
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
	
	
	//*************** code ****************


	@Override
	public boolean hasMoreElements() {
		return (! currentStage.isTerminal());
	}

	@Override
	public BuildStep nextElement() {
		switch(currentStage){
		
		case WAITING :
				currentStage = BuildStage.START_BUILD;
				return newStartBuild();
				
		case START_BUILD :
				currentStage = BuildStage.CHECK_QUEUE;
				return newCheckQueue();
				
		case CHECK_QUEUE :
				currentStage = BuildStage.GET_BUILD_ID;
				return newGetBuildID();
				
		case GET_BUILD_ID :
				currentStage = BuildStage.CHECK_BUILD_RUNNING;
				return newCheckBuildRunning();
				
		case CHECK_BUILD_RUNNING :
				currentStage = BuildStage.GATHER_RESULT;
				return newGatherResults();
				
		case GATHER_RESULT :
				 throw new NoSuchElementException();
				 
		default : throw new NoSuchElementException();
				
			
		}
	}
	

	//************** private stuffs ****************
	
	private StartBuild newStartBuild(){
		
		PostMethod method = requestFactory.newStartFetchTestListBuild(project, absoluteId.getExternalId());
		
		StartBuild startBuild = new StartBuild(processor);
		
		wireHttpSteps(startBuild, method);
		
		return startBuild;
		
	}
	
	private CheckBuildQueue newCheckQueue(){
		
		GetMethod method = requestFactory.newCheckQueue(project);
		
		CheckBuildQueue checkQueue = new CheckBuildQueue(processor); 
		
		wireHttpSteps(checkQueue, method);
		
		return checkQueue;
	}
	
	
	private GetBuildID newGetBuildID(){
		
		GetMethod method = requestFactory.newGetBuildsForProject(project);
		
		GetBuildID buildRunning = new GetBuildID(processor);
		
		wireHttpSteps(buildRunning, method);
		
		return buildRunning;
		
	}
	
	private BuildStep newCheckBuildRunning(){
		
		GetMethod method = requestFactory.newGetBuild(project, absoluteId.getBuildId());
		
		CheckBuildRunning running = new CheckBuildRunning(processor);
		
		wireHttpSteps(running, method);
		
		return running;
	}
	
	private BuildStep newGatherResults(){
		
		GetMethod method = requestFactory.newGetBuildResults(project, absoluteId.getBuildId());
		
		GatherTestList gatherList = new GatherTestList(processor);
		
		wireHttpSteps(gatherList, method);
		
		return gatherList; 
		
	}
	
	
	private void wireHttpSteps(HttpBasedStep step, HttpMethod method){
		step.setClient(client);
		step.setMethod(method);
		step.setParser(jsonParser);
		step.setBuildAbsoluteId(absoluteId);		
	}


	private static enum BuildStage{
		
		WAITING{
			@Override
			public boolean isTerminal() {
				return false;
			}
		},
		START_BUILD{
			@Override
			public boolean isTerminal() {
				return false;
			}
		},
		CHECK_QUEUE{
			@Override
			public boolean isTerminal() {
				return false;
			}
		},
		GET_BUILD_ID{
			@Override
			public boolean isTerminal() {
				return false;
			}
		},
		CHECK_BUILD_RUNNING{
			@Override
			public boolean isTerminal() {
				return false;
			}
		},
		GATHER_RESULT{
			@Override
			public boolean isTerminal() {
				return true;
			}
		};
		
		public abstract boolean isTerminal();
	}
	
	
}
