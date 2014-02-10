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
package org.squashtest.tm.service.internal.library;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.IssueDao;

/**
 * Will update a node regarding it's project settings. The updated attributes will be :
 * <ul>
 * <li>custom fields (see {@linkplain #updateCustomFields(BoundEntity)})</li>
 * <li>issues (see {@linkplain #updateIssues(List)})</li>
 * <li>automated scripts (see {@linkplain #updateAutomationParams(TestCase)})</li>
 * </ul>
 * 
 * @author mpagnon
 * 
 */
@Component
public class TreeNodeUpdater implements NodeVisitor {

	@Inject
	private PrivateCustomFieldValueService privateCustomFieldValueService;

	@Override
	public void visit(CampaignFolder campaignFolder) {
		// nothing to update
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		// nothing to update
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		// nothing to update
	}

	@Override
	public void visit(Campaign campaign) {
		updateCustomFields(campaign);
	}

	@Override
	/**
	 * Iterations cannot be moved , if we go through this method it is because we moved a campaign first.
	 */
	public void visit(Iteration iteration) {
		updateCustomFields(iteration);
		List<Issue> issues = issueDao.findAllForIteration(iteration.getId());
		updateIssues(issues, iteration.getProject());

	}

	@Override
	/**
	 * TestSuite cannot be moved, if we go through this method it is because we moved  an iteration.
	 * Hence there is no need to update executions because there were all updated when iteration was updated.
	 * 
	 */
	public void visit(TestSuite testSuite) {
		updateCustomFields(testSuite);
		updateIssues(issueDao.findAllForTestSuite(testSuite.getId()), testSuite.getProject());
	}

	@Override
	public void visit(Requirement requirement) {
		updateCustomFields(requirement.getCurrentVersion());
		for (RequirementVersion version : requirement.getRequirementVersions()) {
			updateCustomFields(version);
		}
	}

	@Override
	public void visit(TestCase testCase) {
		updateCustomFields(testCase);
		for (TestStep step : testCase.getSteps()) {
			step.accept(new TestStepVisitor() {

				@Override
				public void visit(CallTestStep visited) {// nope

				}

				@Override
				public void visit(ActionTestStep visited) {
					updateCustomFields(visited);
				}
			});
		}
		updateAutomationParams(testCase);
	}

	@Inject
	private IssueDao issueDao;

	/**
	 * @see PrivateCustomFieldValueService#migrateCustomFieldValues(BoundEntity)
	 * @param entity
	 */
	public void updateCustomFields(BoundEntity entity) {
		privateCustomFieldValueService.migrateCustomFieldValues(entity);

	}

	/**
	 * Will remove issue if they are bound to a bugtracker that is not the bugtracker of the current project.
	 * 
	 * @param executions
	 */
	public void updateIssues(List<Issue> issues, Project project) {
		for (Issue issue : issues) {
			if (project != null
					&& (project.getBugtrackerBinding() == null || (!issue.getBugtracker().getId()
							.equals(project.getBugtrackerBinding().getBugtracker().getId())))) {
				issueDao.remove(issue);
			}
		}
	}

	/**
	 * Will remove script of test-case if the script's automated-project is not bound to the current test-case's
	 * project.
	 * 
	 * @param testCase
	 */
	public void updateAutomationParams(TestCase testCase) {
		if (testCase.isAutomated()) {
			Project tcProject = testCase.getProject();
			if (tcProject.isTestAutomationEnabled()) {
				TestAutomationProject autoProject = testCase.getAutomatedTest().getProject();
				if (!tcProject.getTestAutomationProjects().contains(autoProject)) {
					testCase.removeAutomatedScript();
				}
			} else {
				testCase.removeAutomatedScript();
			}
		}

	}

}
