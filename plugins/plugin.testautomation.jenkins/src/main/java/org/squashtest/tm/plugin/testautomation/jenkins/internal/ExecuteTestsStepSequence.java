/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import static org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildStage.START_BUILD;

import java.util.NoSuchElementException;

import org.apache.commons.httpclient.methods.PostMethod;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * 
 * @deprecated no longer used ?
 */
@Deprecated
class ExecuteTestsStepSequence extends HttpBasedStepSequence implements StepSequence {

	// ********* to be configured ************


	private TestAutomationProjectContent projectContent;

	private AbstractBuildProcessor processor;


	// ************* setters **************

	void setProjectContent(TestAutomationProjectContent content) {
		this.projectContent = content;
		setProject(content.getProject());
	}

	// ********** getters ****************

	@Override
	protected AbstractBuildProcessor getProcessor() {
		return processor;
	}



	//*************** code ****************


	ExecuteTestsStepSequence(AbstractBuildProcessor processor){
		super();
		this.processor=processor;
	}


	@Override
	public boolean hasMoreElements() {
		return (currentStage != START_BUILD);
	}


	@Override
	public BuildStep<?> nextElement() {
		switch(currentStage){

		case WAITING :
			currentStage = START_BUILD;
			return newStartBuild();

		case START_BUILD :
			throw new NoSuchElementException();

		default : throw new NoSuchElementException();


		}
	}



	//************** private stuffs ****************


	protected StartBuild newStartBuild(){

		PostMethod method = requestFactory.newStartTestSuiteBuild(projectContent, absoluteId.getExternalId());

		StartBuild startBuild = new StartBuild(processor);

		wireHttpSteps(startBuild, method);

		return startBuild;

	}




}
