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
package org.squashtest.csp.tm.internal.service

import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.service.security.ObjectIdentityService
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.domain.project.ProjectTemplate
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.csp.tm.internal.repository.CampaignLibraryDao
import org.squashtest.csp.tm.internal.repository.GenericProjectDao;
import org.squashtest.csp.tm.internal.repository.ProjectDao
import org.squashtest.csp.tm.internal.repository.ProjectTemplateDao
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao
import org.squashtest.csp.tm.internal.service.project.CustomProjectModificationServiceImpl
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService
import org.squashtest.csp.tm.service.customfield.CustomFieldBindingModificationService
import org.squashtest.csp.tm.service.project.GenericProjectManagerService;
import org.squashtest.csp.tm.service.project.ProjectManagerService
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory

import spock.lang.Specification

class CustomProjectModificationServiceImplTest extends Specification {
	CustomProjectModificationServiceImpl service = new CustomProjectModificationServiceImpl()
	ProjectTemplateDao projectTemplateDao = Mock()
	ProjectDao projectDao = Mock()
	CustomFieldBindingModificationService customFieldBindingModificationService = Mock()
	ProjectsPermissionManagementService projectsPermissionManagementService = Mock()
	GenericProjectManagerService genericProjectManagerService = Mock()
	GenericProjectDao genericProjectDao = Mock()
	
	def setup()
	{
		service.projectDao = projectDao
		service.projectTemplateDao = projectTemplateDao
		service.customFieldBindingModificationService = customFieldBindingModificationService
		service.projectsPermissionManagementService = projectsPermissionManagementService
		service.genericProjectManager = genericProjectManagerService
		service.genericProjectDao = genericProjectDao
		
	}
	
	def "should add projet and copy all settings from template"(){
		given: "a template project"
		ProjectTemplate template = Mock() 
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L		
		project.getClass()>> Project.class

		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, true, true, true, true)
		
		then:
		1* project.setTestAutomationEnabled(true)
		1* project.bindTestAutomationProject(automationProject)
		1* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* projectsPermissionManagementService.copyAssignedUsersFromTemplate(project, template)
	}
	
	
	def "should add projet and copy all settings but bugtracker from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> true
		
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class
		
		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, true, true, false, true)
		
		then:
		1* project.setTestAutomationEnabled(true)
		1* project.bindTestAutomationProject(automationProject)
		0* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* projectsPermissionManagementService.copyAssignedUsersFromTemplate(project, template)
	}
	def "should not try to copy bugtracker settings because template is not bugtracker connected"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> false
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class
		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, true, true, true, true)
		
		then:
		1* project.setTestAutomationEnabled(true)
		1* project.bindTestAutomationProject(automationProject)
		0* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* projectsPermissionManagementService.copyAssignedUsersFromTemplate(project, template)
	}
	
	def "should add projet and copy all settings but test automation from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class
		
		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, true, true, true, false)
		
		then:
		0* project.setTestAutomationEnabled(_)
		0* project.bindTestAutomationProject(automationProject)
		1* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		1* projectsPermissionManagementService.copyAssignedUsersFromTemplate(project, template)
	}
	def "should add projet and copy all settings but user permissions from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class
		
		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, false, true, true, true)
		
		then:
		1* project.setTestAutomationEnabled(true)
		1* project.bindTestAutomationProject(automationProject)
		1* project.setBugtrackerBinding(_)
		1* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(project, template)
		0* projectsPermissionManagementService.copyAssignedUsersFromTemplate(_, _)
	}
	
	def "should add projet and copy all settings but custom fields binging from template"(){
		given: "a template project"
		ProjectTemplate template = Mock()
		template.isTestAutomationEnabled() >> Boolean.TRUE
		projectTemplateDao.findById(1L) >> template
		TestAutomationProject automationProject = Mock()
		template.getTestAutomationProjects() >> [automationProject]
		template.isBugtrackerConnected() >> true
		BugTrackerBinding binding = Mock()
		template.getBugtrackerBinding() >> binding
		BugTracker bugtracker = Mock()
		binding.getBugtracker() >> bugtracker
		
		and: "a project"
		Project project = Mock()
		project.getId()>> 2L
		project.getClass()>> Project.class
		
		
		when:
		service.addProjectAndCopySettingsFromTemplate(project, 1L, true, false, true, true)
		
		then:
		1* project.setTestAutomationEnabled(true)
		1* project.bindTestAutomationProject(automationProject)
		1* project.setBugtrackerBinding(_)
		0* customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(_, _)
		1* projectsPermissionManagementService.copyAssignedUsersFromTemplate(project, template)
	}
	
}
