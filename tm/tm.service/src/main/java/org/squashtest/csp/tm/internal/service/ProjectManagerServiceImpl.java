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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.ObjectIdentityService;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.CampaignLibraryDao;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.ProjectTemplateDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.service.BugTrackerManagerService;
import org.squashtest.csp.tm.service.ProjectManagerService;
import org.squashtest.csp.tm.service.ProjectModificationService;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;
import org.squashtest.csp.tm.service.customfield.CustomFieldBindingModificationService;

@Service("squashtest.tm.service.ProjectManagerService")
@Transactional
public class ProjectManagerServiceImpl implements ProjectManagerService {

	@Inject
	private ProjectDao projectDao;
	
	@Inject
	private ProjectTemplateDao projectTemplateDao;

	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private RequirementLibraryDao requirementLibraryDao;

	@Inject
	private ObjectIdentityService objectIdentityService;
	
	@Inject
	private CustomFieldBindingModificationService customFieldBindingModificationService;
	
	@Inject
	private ProjectsPermissionManagementService projectsPermissionManagementService;
	
	@Inject
	private ProjectModificationService projectModificationService;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void addProject(Project project) {
		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		campaignLibraryDao.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		requirementLibraryDao.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		testCaseLibraryDao.persist(tcl);

		projectDao.persist(project);

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());

	}
	/**
	 * @see ProjectManagerService#addProjectAndCopySettingsFromTemplate(Project, long, boolean, boolean, boolean, boolean)
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Override
	public Project addProjectAndCopySettingsFromTemplate(Project newProject, long templateId,
			boolean copyAssignedUsers, boolean copyCustomFieldsSettings, boolean copyBugtrackerSettings,
			boolean copyTestAutomationSettings) {
		addProject(newProject);
		ProjectTemplate projectTemplate = projectTemplateDao.findById(templateId);
		if(copyAssignedUsers){
			copyAssignedUsers(newProject, projectTemplate);
		}
		if(copyCustomFieldsSettings){
			copyCustomFieldsSettings(newProject, projectTemplate);
		}
		if(copyBugtrackerSettings){
			copyBugtrackerSettings(newProject, projectTemplate);
		}
		if(copyTestAutomationSettings){
			copyTestAutomationSettings(newProject, projectTemplate);
		}
		return newProject;
	}
	
	
	private void copyTestAutomationSettings(Project newProject, ProjectTemplate projectTemplate) {
		newProject.setTestAutomationEnabled(projectTemplate.isTestAutomationEnabled());
		for(TestAutomationProject automationProject : projectTemplate.getTestAutomationProjects()){
			newProject.bindTestAutomationProject(automationProject);
		}
	}
	private void copyBugtrackerSettings(Project newProject, ProjectTemplate projectTemplate) {
		if(projectTemplate.isBugtrackerConnected()) {
			projectModificationService.changeBugTracker(newProject, projectTemplate.getBugtrackerBinding().getBugtracker());
		}
	}
	private void copyCustomFieldsSettings(Project newProject, ProjectTemplate projectTemplate) {
		customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate( newProject, projectTemplate);
		
	}
	private void copyAssignedUsers(Project newProject, ProjectTemplate projectTemplate) {
		projectsPermissionManagementService.copyAssignedUsersFromTemplate(newProject, projectTemplate);
		
	}
	
	

}
