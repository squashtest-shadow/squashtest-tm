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

import org.apache.commons.httpclient.HttpClient;
import org.springframework.scheduling.TaskScheduler;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.DelayedBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * 
 * @deprecated no longer used ?
 */
@Deprecated
public class ExecuteTestsBuildProcessor extends DelayedBuildProcessor {

	private ExecuteTestsStepSequence stepSequence = new ExecuteTestsStepSequence(this);


	//******* collaborators *********

	public void setClient(HttpClient client){
		stepSequence.setClient(client);
	}

	public void setProjectContent(TestAutomationProjectContent content){
		stepSequence.setProjectContent(content);
	}

	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId){
		stepSequence.setAbsoluteId(absoluteId);
	}


	//************** ctor **************

	public ExecuteTestsBuildProcessor(TaskScheduler scheduler) {
		super(scheduler);
	}


	@Override
	protected StepSequence getStepSequence() {
		return stepSequence;
	}

}
