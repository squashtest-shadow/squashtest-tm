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

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Arrays;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.LoginDoNotExistException;
import org.squashtest.csp.tm.domain.NoBugTrackerBindingException;
import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.Project;


import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.BugTrackerFinderService;
import org.squashtest.csp.tm.service.ProjectModificationService;
import org.squashtest.csp.tm.web.internal.helper.JsonHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.testautomation.TestAutomationProjectRegistrationForm;


@Controller
@RequestMapping("/projects/{projectId}")
public class ProjectModificationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectModificationController.class);

	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";

	private static final String PROJECT_ID = "projectId";

	@Inject
	private MessageSource messageSource;

	private ProjectModificationService projectModificationService;

	
	@ServiceReference
	public void setProjectModificationService(ProjectModificationService projectModificationService) {
		this.projectModificationService = projectModificationService;
	}

	private BugTrackerFinderService bugtrackerFinderService;

	@ServiceReference
	public void setBugTrackerFinderService(BugTrackerFinderService bugtrackerFinderService) {
		this.bugtrackerFinderService = bugtrackerFinderService;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long projectId, Locale locale) {
		
		AdministrableProject adminProject = projectModificationService.findAdministrableProjectById(projectId);
		TestAutomationServer taServerCoordinates = projectModificationService.getLastBoundServerOrDefault(adminProject.getProject().getId());
		List<TestAutomationProject> boundProjects = projectModificationService.findBoundTestAutomationProjects(projectId);

		Map<Long, String> comboDataMap = createComboDataForBugtracker(locale);

		
		ModelAndView mav = new ModelAndView("page/projects/project-info");
		
		mav.addObject("adminproject", adminProject);
		mav.addObject("taServer", taServerCoordinates);
		mav.addObject("bugtrackersList", JsonHelper.serialize(comboDataMap));
		mav.addObject("bugtrackersListEmpty", comboDataMap.size() == 1);
		mav.addObject("boundTAProjects", boundProjects);
		return mav;
	}

	private Map<Long, String> createComboDataForBugtracker(Locale locale) {
		Map<Long, String> comboDataMap = new HashMap<Long, String>();
		for (BugTracker b : bugtrackerFinderService.findAll()) {
			comboDataMap.put(b.getId(), b.getName());
		}
		comboDataMap.put(-1L, messageSource.getMessage(PROJECT_BUGTRACKER_NAME_UNDEFINED, null, locale));
		return comboDataMap;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=project-label", VALUE })
	@ResponseBody
	public String changeLabel(@RequestParam(VALUE) String projectLabel, @PathVariable long projectId) {
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
	public Object changeActive(HttpServletResponse response, @PathVariable long projectId,
			@RequestParam boolean isActive) {

		projectModificationService.changeActive(projectId, isActive);
		LOGGER.info("Project modification : change project {} is active = {}", projectId, isActive);
		final Boolean newIsActive = isActive;
		return new Object() {
			public Boolean active = newIsActive; // NOSONAR unreadable field actually read by JSON marshaller.
		};
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=project-bugtracker", VALUE })
	@ResponseBody
	public String changeBugtracker(@RequestParam(VALUE) Long bugtrackerId, @PathVariable long projectId, Locale locale) {
		String toReturn ;
		if (bugtrackerId > 0) {
			toReturn = bugtrackerFinderService.findBugtrackerName(bugtrackerId);
			projectModificationService.changeBugTracker(projectId, bugtrackerId);
			LOGGER.debug("Project {} : bugtracker changed, new value : {}", projectId, bugtrackerId);
		} else {
			toReturn = messageSource.getMessage(PROJECT_BUGTRACKER_NAME_UNDEFINED, null, locale);
			projectModificationService.removeBugTracker(projectId);
		}
		return toReturn;
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "id=project-bugtracker-project-name", VALUE })
	@ResponseBody
	public String changeBugtrackerProjectName(@RequestParam(VALUE) String projectBugTrackerName, @PathVariable long projectId, Locale locale) {
		projectModificationService.changeBugTrackerProjectName(projectId, projectBugTrackerName);
		return projectBugTrackerName;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=project-description", VALUE })
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String projectDescription, @PathVariable long projectId) {
		projectModificationService.changeDescription(projectId, projectDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("project " + projectId + ": updated description to " + projectDescription);
		}
		return projectDescription;
	}
	
	@RequestMapping(value="bugtracker/projectName", method = RequestMethod.GET)
	@ResponseBody
	public String getBugtrackerProject(@PathVariable long projectId){
		Project project = projectModificationService.findById(projectId);
		if(project.isBugtrackerConnected()){
			return project.getBugtrackerBinding().getProjectName();
		}else{
			throw new NoBugTrackerBindingException();
		}
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
	public void deleteProject(@PathVariable long projectId) {
		projectModificationService.deleteProject(projectId);
	}

	
	
	// *********************Permission Management*********************
	
	@RequestMapping(value = "/add-permission", method = RequestMethod.POST, params = { "user" })
	public @ResponseBody
	void addNewPermission(@RequestParam long user, @PathVariable long projectId, @RequestParam String permission) {
		projectModificationService.addNewPermissionToProject(user, projectId, permission);
	}

	
	
	@RequestMapping(value = "/add-permission", method = RequestMethod.POST, params = { "userLogin" })
	public @ResponseBody
	void addNewPermissionWithLogin(@RequestParam String userLogin, @PathVariable long projectId,
			@RequestParam String permission) {
		User user = projectModificationService.findUserByLogin(userLogin);
		if (user == null) {
			throw new LoginDoNotExistException();
		}
		projectModificationService.addNewPermissionToProject(user.getId(), projectId, permission);
	}
	
	
	@RequestMapping(value = "/remove-permission", method = RequestMethod.POST)
	public @ResponseBody
	void removePermission(@RequestParam("user") long userId, @PathVariable long projectId) {
		projectModificationService.removeProjectPermission(userId, projectId);
	}

	
	
	@RequestMapping(value = "/permission-popup", method = RequestMethod.GET)
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

	
	@RequestMapping(value = "/permission-table", method = RequestMethod.GET)
	public ModelAndView getPermissionTable(@PathVariable long projectId) {
		
		Project project = projectModificationService.findById(projectId);
		List<UserProjectPermissionsBean> userProjectPermissionsBean = projectModificationService
				.findUserPermissionsBeansByProject(projectId);
		List<PermissionGroup> permissionList = projectModificationService.findAllPossiblePermission();

		ModelAndView mav = new ModelAndView("fragment/project/project-permission-table");
		mav.addObject("project", project);
		mav.addObject("permissionList", permissionList);
		mav.addObject("userPermissionList", userProjectPermissionsBean);
		return mav;
		
	}
	
	
	//********************* test automation *********************
	
	
	//filtering and sorting not supported for now
	@RequestMapping(value = "/test-automation-projects", method=RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getProjectsTableModel(@PathVariable(PROJECT_ID) long projectId, final DataTableDrawParameters params) {
		List<TestAutomationProject> taProjects = projectModificationService.findBoundTestAutomationProjects(projectId);
		
		FilteredCollectionHolder<List<TestAutomationProject>> holder = 
			new FilteredCollectionHolder<List<TestAutomationProject>>(taProjects.size(), taProjects);
		
		return new TestAutomationTableModel().buildDataModel(holder, 0, params.getsEcho());
					
	}
	
	@RequestMapping(value = "/test-automation-projects", method=RequestMethod.POST, headers = "Content-Type=application/json" )
	@ResponseBody
	public void bindTestAutomationProject(@PathVariable(PROJECT_ID) long projectId, @RequestBody TestAutomationProjectRegistrationForm[] projects, Locale locale)
	throws BindException{
		TestAutomationProjectRegistrationForm form=null;
		try{
			Iterator<TestAutomationProjectRegistrationForm> it = Arrays.asList(projects).listIterator();
			while (it.hasNext()){
				form = it.next();				
				projectModificationService.bindTestAutomationProject(projectId, form.toTestAutomationProject());
			}
		}
		catch(MalformedURLException ex){
			//quick and dirty validation
			BindException be = new BindException(new TestAutomationServer(), "ta-project");
			be.rejectValue("baseURL", null, findMessage(locale, "error.url.malformed"));
			throw be;
		}	
	}
	
	
	@RequestMapping(value="/test-automation-enabled", method=RequestMethod.POST, params = "enabled")
	@ResponseBody
	public void enableTestAutomation(@PathVariable(PROJECT_ID) long projectId, @RequestParam("enabled") boolean isEnabled){
		projectModificationService.changeTestAutomationEnabled(projectId, isEnabled);
	}
	
	
	
	@RequestMapping(value = "/test-automation-projects/{taProjectId}", method=RequestMethod.DELETE )
	@ResponseBody
	public void unbindProject(@PathVariable(PROJECT_ID) Long projectId, @PathVariable("taProjectId") Long taProjectId){
		projectModificationService.unbindTestAutomationProject(projectId, taProjectId);
	}
	
	
	
	private final class TestAutomationTableModel extends DataTableModelHelper<TestAutomationProject>{

		
		@Override
		protected Map<String, ?> buildItemData(TestAutomationProject item) {
			Map<String, Object> res = new HashMap<String, Object>();
			
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex() + 1);
			res.put("name", item.getName());
			res.put("server-url", item.getServer().getBaseURL());
			res.put("server-kind", item.getServer().getKind());
			res.put("empty-delete-holder", " ");
			
			return res;
		}
	}
	
	
	
	
	
	//***************** private utils *******************************
	
	private String findMessage(Locale locale, String key){
		return messageSource.getMessage(key, null, locale);
	}
	
	
	
	
	
	
	
	
	

}
