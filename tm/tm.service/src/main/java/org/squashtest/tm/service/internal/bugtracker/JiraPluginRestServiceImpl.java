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
package org.squashtest.tm.service.internal.bugtracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCaseRestInfo;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.service.bugtracker.JiraPluginRestService;
import org.squashtest.tm.service.internal.repository.IssueDao;

@Service("squashtest.tm.service.JiraPluginRestService")
public class JiraPluginRestServiceImpl implements JiraPluginRestService{

	@Inject
	private IssueDao issueDao;

	@Inject
	BugTrackerManagerService bugTrackersManagerService;

	@Override
	public List<TestCaseRestInfo> getTestcases(String issueKey, String bugTrackerName) {

		List<TestCaseRestInfo> result = new ArrayList<TestCaseRestInfo>();

		//Get bugtracker from key
		BugTracker bugtracker = bugTrackersManagerService.getBugtrackerFromKeyIfExists(bugTrackerName);

		if(bugtracker != null){

			//Get issue
			//FIXME
			List<Issue> issues = issueDao.findIssueIfExists(issueKey, bugTrackerName);
			for(Issue issue : issues){

				//Get issue holder (execution or execution step)
				Execution execution = issueDao.findIssueHoldersAtExecutionLevel(issue);

				//Get test case
				//for(Execution execution : executions){

				TestCaseRestInfo testCaseRestInfo = new TestCaseRestInfo();


				testCaseRestInfo.setName(execution.getName());
				testCaseRestInfo.setDescription(execution.getTcdescription());
				testCaseRestInfo.setPrerequisite(execution.getPrerequisite());
				testCaseRestInfo.setProjectName(execution.getProject().getName());
				testCaseRestInfo.setPath(execution.getReferencedTestCase().getFullName());

				List<ExecutionStep> steps = execution.getSteps();

				for(ExecutionStep step : steps){
					testCaseRestInfo.addStep(step.getAction(), step.getExpectedResult());
				}

				result.add(testCaseRestInfo);
			}
		}

		return result;
	}

	@Override
	public String login(String login, String password) {
		return null;
	}
}
