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

import java.util.NoSuchElementException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.HttpBasedStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild;


class ExecuteTestsStepSequence implements StepSequence {

	private HttpRequestFactory requestFactory = new HttpRequestFactory();
	
	private JsonParser jsonParser = new JsonParser();
	
	private BuildStage currentStage = BuildStage.WAITING;
	
	// ********* to be configured ************
	
	private HttpClient client;
	
	private TestAutomationProjectContent projectContent;
	
	private BuildAbsoluteId absoluteId;
	
	private AbstractBuildProcessor<?> processor;
	
	
	// ************* setters **************
	
	void setClient(HttpClient client) {
		this.client = client;
	}

	
	void setProjectContent(TestAutomationProjectContent content) {
		this.projectContent = content;
	}
	
	
	void setAbsoluteId(BuildAbsoluteId absoluteId) {
		this.absoluteId = absoluteId;
	}
	

	//*************** code ****************
	
	
	ExecuteTestsStepSequence(AbstractBuildProcessor<?> processor){
		super();
		this.processor=processor;
	}
	
	
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
				throw new NoSuchElementException();
				
		default : throw new NoSuchElementException();
				
			
		}
	}
	
	
	
	//************** private stuffs ****************
	
	
	private StartBuild newStartBuild(){
		
		PostMethod method = requestFactory.newStartTestSuiteBuild(projectContent, absoluteId.getExternalId());
		
		StartBuild startBuild = new StartBuild(processor);
		
		wireHttpSteps(startBuild, method);
		
		return startBuild;
		
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
				return true;
			}
		};
		
		public abstract boolean isTerminal();
	}
	
	

}
