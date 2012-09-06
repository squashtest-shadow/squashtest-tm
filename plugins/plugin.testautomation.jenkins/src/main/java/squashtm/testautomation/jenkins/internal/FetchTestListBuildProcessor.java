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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationTest;

import squashtm.testautomation.jenkins.internal.tasks.StepSequence;
import squashtm.testautomation.jenkins.internal.tasks.SynchronousBuildProcessor;
import squashtm.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import squashtm.testautomation.jenkins.internal.tasksteps.GatherTestList;

public class FetchTestListBuildProcessor extends SynchronousBuildProcessor<Collection<TestAutomationTest>>{
	
	private FetchTestListStepSequence stepSequence = new FetchTestListStepSequence(this);
	
	private TestAutomationProject project;

	//******* collaborators *********
	
	public void setClient(HttpClient client){
		stepSequence.setClient(client);
	}
	
	public void setProject(TestAutomationProject project){
		stepSequence.setProject(project);
		this.project = project;
	}
	
	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId){
		stepSequence.setAbsoluteId(absoluteId);
	}

	
	//******* the result we obtain once the computation is over *********
	
	private Collection<TestAutomationTest> tests = new ArrayList<TestAutomationTest>();
	
	
	@Override
	public Collection<TestAutomationTest> getResult() {
		return tests;
	}

	@Override
	protected void buildResult() {
		
		if (! stepSequence.hasMoreElements()){
			
			Collection<String> names = ((GatherTestList) currentStep).getTestNames();
			
			for (String name : names){
				TestAutomationTest test = new TestAutomationTest(name, project);
				tests.add(test);
			}
		}
		else{
			throw new RuntimeException("tried to build the result before the computation is over, probably due to a buggy thread");
		}
	}

	@Override
	protected StepSequence getStepSequence() {
		return stepSequence;
	}
}
