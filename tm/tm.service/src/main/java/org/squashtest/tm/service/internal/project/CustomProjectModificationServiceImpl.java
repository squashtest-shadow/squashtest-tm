/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.project;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import static org.squashtest.tm.service.security.Authorizations.*;

/**
 * 
 * @author mpagnon
 * 
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	@Inject
	private CustomFieldBindingModificationService customFieldBindingModificationService;
	@Inject
	private ProjectsPermissionManagementService permissionService;
	@Inject
	private ProjectTemplateDao projectTemplateDao;
	@Inject
	private GenericProjectManagerService genericProjectManager;
	@Inject
	private ProjectDao projectDao;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private GenericProjectDao genericProjectDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Project> findAllReadable() {
		return projectDao.findAll();
	}

	/**
	 * @see ProjectManagerService#addProjectAndCopySettingsFromTemplate(Project, long, boolean, boolean, boolean,
	 *      boolean)
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	@Override
	public Project addProjectAndCopySettingsFromTemplate(Project newProject, long templateId,
			boolean copyAssignedUsers, boolean copyCustomFieldsSettings, boolean copyBugtrackerSettings,
			boolean copyTestAutomationSettings, boolean copyInfolists, boolean copyMilestone) {
		genericProjectManager.persist(newProject);

		ProjectTemplate projectTemplate = projectTemplateDao.findById(templateId);
		if (copyAssignedUsers) {
			copyAssignedUsers(newProject, projectTemplate);
		}
		if (copyCustomFieldsSettings) {
			copyCustomFieldsSettings(newProject, projectTemplate);
		}
		if (copyBugtrackerSettings) {
			copyBugtrackerSettings(newProject, projectTemplate);
		}
		if (copyTestAutomationSettings) {
			copyTestAutomationSettings(newProject, projectTemplate);
		}
		if (copyInfolists){
			copyInfolists(newProject, projectTemplate);
		}

		if (copyMilestone){
			copyMilestone(newProject, projectTemplate);
		}

		return newProject;
	}

	private void copyMilestone(Project newProject, ProjectTemplate projectTemplate) {

		List<Milestone> milestones = getOnlyBindableMilestones(projectTemplate.getMilestones());

		newProject.bindMilestones(milestones);

		for (Milestone milestone: milestones){
			milestone.addProjectToPerimeter(newProject);
		}
	}




	private List<Milestone> getOnlyBindableMilestones(List<Milestone> milestones) {
		List<Milestone> bindableMilestones = new ArrayList<Milestone>();
		for (Milestone m : milestones){
			if (m.getStatus().isBindableToProject()){
				bindableMilestones.add(m);
			}
		}
		return bindableMilestones;
	}

	private void copyTestAutomationSettings(Project newProject, ProjectTemplate projectTemplate) {

		newProject.setTestAutomationServer(projectTemplate.getTestAutomationServer());

		for (TestAutomationProject automationProject : projectTemplate.getTestAutomationProjects()) {
			newProject.bindTestAutomationProject(automationProject);
		}
	}

	private void copyBugtrackerSettings(Project newProject, ProjectTemplate projectTemplate) {
		if (projectTemplate.isBugtrackerConnected()) {
			genericProjectManager.changeBugTracker(newProject, projectTemplate.getBugtrackerBinding().getBugtracker());
		}
	}

	private void copyCustomFieldsSettings(Project newProject, ProjectTemplate projectTemplate) {
		customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(newProject, projectTemplate);

	}

	private void copyAssignedUsers(Project newProject, ProjectTemplate projectTemplate) {
		permissionService.copyAssignedUsersFromTemplate(newProject, projectTemplate);

	}

	private void copyInfolists(Project newProject, ProjectTemplate projectTemplate){
		newProject.setRequirementCategories(projectTemplate.getRequirementCategories());
		newProject.setTestCaseNatures(projectTemplate.getTestCaseNatures());
		newProject.setTestCaseTypes(projectTemplate.getTestCaseTypes());
	}

	@Override
	public List<GenericProject> findAllICanManage() {
		List<GenericProject> projects = genericProjectDao.findAll();
		List<GenericProject> manageableProjects = new ArrayList<GenericProject>();

		for (GenericProject project : projects) {
			if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

}
