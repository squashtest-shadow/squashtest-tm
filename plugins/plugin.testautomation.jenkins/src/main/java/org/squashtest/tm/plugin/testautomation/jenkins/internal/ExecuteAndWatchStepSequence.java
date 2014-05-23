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

import static org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildStage.GET_BUILD_ID;

import java.util.NoSuchElementException;

import org.apache.commons.httpclient.methods.PostMethod;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepEventListener;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

class ExecuteAndWatchStepSequence extends HttpBasedStepSequence implements StepSequence {

	private TestAutomationProjectContent projectContent;

	private ExecuteAndWatchBuildProcessor processor;

	private StepEventListener<GetBuildID> buildIDListener;

	// ************** setters *************

	public void setBuildIDEventListener(StepEventListener<GetBuildID> listener) {
		this.buildIDListener = listener;
	}

	void setProjectContent(TestAutomationProjectContent content) {
		this.projectContent = content;
		setProject(content.getProject());
	}

	// ************** getters *************

	@Override
	protected AbstractBuildProcessor getProcessor() {
		return processor;
	}

	// ************** constructor ****************

	ExecuteAndWatchStepSequence(ExecuteAndWatchBuildProcessor processor) {
		super();
		this.processor = processor;
	}

	// *************** code ****************

	@Override
	public boolean hasMoreElements() {
		return (currentStage != GET_BUILD_ID);
	}

	@Override
	public BuildStep<?> nextElement() {
		switch (currentStage) {

		case WAITING:
			currentStage = BuildStage.START_BUILD;
			return newStartBuild();

		case START_BUILD:
			currentStage = BuildStage.CHECK_QUEUE;
			return newCheckQueue();

		case CHECK_QUEUE:
			currentStage = BuildStage.GET_BUILD_ID;
			return newGetBuildID();

		case GET_BUILD_ID:
			throw new NoSuchElementException();

		default:
			throw new NoSuchElementException();

		}
	}

	// ********** some override ****************

	@Override
	protected GetBuildID newGetBuildID() {
		GetBuildID step = super.newGetBuildID();
		step.addListener(buildIDListener);
		return step;
	}

	protected StartBuild newStartBuild() {

		PostMethod method = requestFactory.newStartTestSuiteBuild(projectContent, absoluteId.getExternalId());

		StartBuild startBuild = new StartBuild(processor);

		wireHttpSteps(startBuild, method);

		return startBuild;

	}

}
