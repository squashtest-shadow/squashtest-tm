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
package org.squashtest.csp.tm.web.internal.controller.project;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.LoginDoNotExistException;
import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.csp.tm.service.ProjectModificationService;

@Controller
@RequestMapping("/projects/{projectId}")
public class ProjectModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectModificationController.class);
	private ProjectModificationService projectModificationService;
	@ServiceReference
	public void setProjectModificationService(ProjectModificationService projectModificationService) {
		this.projectModificationService = projectModificationService;
	}
	@RequestMapping(value="/info", method=RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long projectId){
		AdministrableProject project = projectModificationService.findAdministrableProjectById(projectId);
		ModelAndView mav = new ModelAndView("page/projects/project-info");	
		mav.addObject("adminproject", project);
		return mav;
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "id=project-label", "value" })
	@ResponseBody
	public String changeLabel(@RequestParam("value") String projectLabel, @PathVariable long projectId) {
		projectModificationService.changeLabel(projectId, projectLabel);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("project " + projectId + ": updated label to " + projectLabel);
		}
		return projectLabel;
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long projectId, @RequestParam String newName) {

		projectModificationService.changeName(projectId, newName);
		LOGGER.info("Project modification : renaming {} as {}", projectId, newName);
		final String reNewName = newName;
		return new Object() {
			public String newName = reNewName; // NOSONAR unreadable field actually read by JSON marshaller.
		};
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "isActive" })
	@ResponseBody
	public Object changeActive(HttpServletResponse response, @PathVariable long projectId, @RequestParam boolean isActive) {

		projectModificationService.changeActive(projectId, isActive);
		LOGGER.info("Project modification : change project {} is active = {}", projectId, isActive);
		final Boolean newIsActive = isActive;
		return new Object() {
			public Boolean active = newIsActive; // NOSONAR unreadable field actually read by JSON marshaller.
		};
	}
	@RequestMapping(method = RequestMethod.POST, params = { "id=project-description", "value" })
	@ResponseBody
	public String changeDescription(@RequestParam("value") String projectDescription, @PathVariable long projectId) {
		projectModificationService.changeDescription(projectId, projectDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("project " + projectId + ": updated description to " + projectDescription);
		}
		return projectDescription;
	}
	
	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long projectId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		Project project = projectModificationService.findById(projectId);
		if (project == null) {
			throw new UnknownEntityException(projectId, Project.class);
		}
		mav.addObject("auditableEntity", project);
		// context-absolute url of this entity
		mav.addObject("entityContextUrl", "/projects/" + projectId);

		return mav;
	}
	
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteProject(@PathVariable long projectId){
		projectModificationService.deleteProject(projectId);
	}
	
	//*********************Permission Management*********************
	@RequestMapping(value="/add-permission", method=RequestMethod.POST, params = { "user" })
	public @ResponseBody void addNewPermission(@RequestParam long user, @PathVariable long projectId, @RequestParam String permission){
		projectModificationService.addNewPermissionToProject(user, projectId, permission);
	}
	@RequestMapping(value="/add-permission", method=RequestMethod.POST, params = { "userLogin" })
	public @ResponseBody void addNewPermissionWithLogin(@RequestParam String userLogin, @PathVariable long projectId, @RequestParam String permission){
		User user = projectModificationService.findUserByLogin(userLogin);
		if(user == null){
			throw new LoginDoNotExistException();
		}
		projectModificationService.addNewPermissionToProject(user.getId(), projectId, permission);
	}
	@RequestMapping(value="/remove-permission", method=RequestMethod.POST)
	public @ResponseBody void removePermission(@RequestParam("user") long userId, @PathVariable long projectId){
		projectModificationService.removeProjectPermission(userId, projectId);
	}
	
	@RequestMapping(value = "/permission-popup" ,method = RequestMethod.GET)
	public ModelAndView getPermissionPopup(@PathVariable long projectId) {
		Project project = projectModificationService.findById(projectId);
		List<PermissionGroup> permissionList = projectModificationService.findAllPossiblePermission();
		List<User> userList = projectModificationService.findUserWithoutPermissionByProject(projectId);
		
		ModelAndView mav = new ModelAndView("fragment/project/project-permission-popup");
		mav.addObject("project", project);
		mav.addObject("userList", userList);
		mav.addObject("permissionList", permissionList);
		return mav;
	}
	
	@RequestMapping(value = "/permission-table" ,method = RequestMethod.GET)
	public ModelAndView getPermissionTable(@PathVariable long projectId) {
		Project project = projectModificationService.findById(projectId);
		List<UserProjectPermissionsBean> userProjectPermissionsBean = projectModificationService.findUserPermissionsBeansByProject(projectId);
		List<PermissionGroup> permissionList = projectModificationService.findAllPossiblePermission();
		
		ModelAndView mav = new ModelAndView("fragment/project/project-permission-table");
		mav.addObject("project", project);
		mav.addObject("permissionList", permissionList);
		mav.addObject("userPermissionList", userProjectPermissionsBean);
		return mav;
	}
	
}
