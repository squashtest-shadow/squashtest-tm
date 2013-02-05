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

package org.squashtest.tm.web.internal.controller.project;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.tm.exception.LoginDoNotExistException;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.web.internal.helper.ProjectHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.testautomation.TestAutomationProjectRegistrationForm;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.IndexBasedMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/generic-projects")
public class GenericProjectController {
	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private GenericProjectManagerService projectManager;

	@Inject
	private BugTrackerFinderService bugtrackerFinderService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericProjectController.class);

	private static final String PROJECT_ID = "projectId";
	private static final String PROJECT_ID_ULR = "/{projectId}";
	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";


	private DatatableMapper allProjectsMapper = new NameBasedMapper(9)
												.mapAttribute(GenericProject.class, "name", String.class, "name")
												.mapAttribute(GenericProject.class, "label", String.class, "label")
												.mapAttribute(GenericProject.class, "active", boolean.class, "active")
												.mapAttribute(GenericProject.class, "audit.createdOn", Date.class, "created-on")
												.mapAttribute(GenericProject.class, "audit.createdBy", String.class, "created-by")
												.mapAttribute(GenericProject.class, "audit.lastModifiedOn", Date.class, "last-mod-on")
												.mapAttribute(GenericProject.class, "audit.lastModifiedBy", String.class, "last-mod-by");

	
	@RequestMapping(value = "", params = "sEcho", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getProjectsTableModel(final DataTableDrawParameters params, final Locale locale) {
		
		PagingAndSorting sorter = new DataTableMapperPagingAndSortingAdapter(params, allProjectsMapper,
									  SortedAttributeSource.SINGLE_ENTITY);
		
		Filtering filter = new DataTableFiltering(params);

		PagedCollectionHolder<List<GenericProject>> holder = projectManager.findSortedProjects(sorter, filter);

		return new ProjectDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "isTemplate=false")
	public @ResponseBody void createNewProject(@Valid @ModelAttribute("add-project") Project project) {
		projectManager.persist(project);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "isTemplate=true")
	public @ResponseBody void createNewProject(@Valid @ModelAttribute("add-project") ProjectTemplate template) {
		projectManager.persist(template);
	}
	
	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "id=project-label", VALUE })
	@ResponseBody
	public String changeLabel(@RequestParam(VALUE) String projectLabel, @PathVariable long projectId) {
		projectManager.changeLabel(projectId, projectLabel);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("project " + projectId + ": updated label to " + projectLabel);
		}
		return projectLabel;
	}

	
	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long projectId, @RequestParam String newName) {

		projectManager.changeName(projectId, newName);
		LOGGER.info("Project modification : renaming {} as {}", projectId, newName);
		return new RenameModel(newName);
	}

	
	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "isActive" })
	@ResponseBody
	public Active changeActive(HttpServletResponse response, @PathVariable long projectId,
			@RequestParam boolean isActive) {

		projectManager.changeActive(projectId, isActive);
		LOGGER.info("Project modification : change project {} is active = {}", projectId, isActive);
		return new Active(isActive);
	}
	
	private final class Active {
		private Boolean active;
		private Active(Boolean active){
			this.active = active;
		}
		@SuppressWarnings("unused")
		public Boolean isActive(){
			return active;
		}
	}

	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "id=project-bugtracker", VALUE })
	@ResponseBody
	public String changeBugtracker(@RequestParam(VALUE) Long bugtrackerId, @PathVariable long projectId, Locale locale) {
		String toReturn ;
		if (bugtrackerId > 0) {
			toReturn = bugtrackerFinderService.findBugtrackerName(bugtrackerId);
			projectManager.changeBugTracker(projectId, bugtrackerId);
			LOGGER.debug("Project {} : bugtracker changed, new value : {}", projectId, bugtrackerId);
		} else {
			toReturn = messageSource.getMessage(PROJECT_BUGTRACKER_NAME_UNDEFINED, null, locale);
			projectManager.removeBugTracker(projectId);
		}
		return toReturn;
	}
	
	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "id=project-bugtracker-project-name", VALUE })
	@ResponseBody
	public String changeBugtrackerProjectName(@RequestParam(VALUE) String projectBugTrackerName, @PathVariable long projectId, Locale locale) {
		projectManager.changeBugTrackerProjectName(projectId, projectBugTrackerName);
		return projectBugTrackerName;
	}

	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.POST, params = { "id=project-description", VALUE })
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String projectDescription, @PathVariable long projectId) {
		projectManager.changeDescription(projectId, projectDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("project " + projectId + ": updated description to " + projectDescription);
		}
		return projectDescription;
	}
	
	@RequestMapping(value= PROJECT_ID_ULR+"/bugtracker/projectName", method = RequestMethod.GET)
	@ResponseBody
	public String getBugtrackerProject(@PathVariable long projectId){
		GenericProject project = projectManager.findById(projectId);
		if(project.isBugtrackerConnected()){
			return project.getBugtrackerBinding().getProjectName();
		}else{
			throw new NoBugTrackerBindingException();
		}
	}
	
	@RequestMapping(value = PROJECT_ID_ULR+"/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long projectId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		GenericProject project = projectManager.findById(projectId);
		if (project == null) {
			throw new UnknownEntityException(projectId, Project.class);
		}
		mav.addObject("auditableEntity", project);
		// context-absolute url of this entity
		mav.addObject("entityContextUrl", "/projects/" + projectId);

		return mav;
	}

	
	
	@RequestMapping(value = PROJECT_ID_ULR, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteProject(@PathVariable long projectId) {
		projectManager.deleteProject(projectId);
	}

	
	// ********************************** Permission Popup *******************************
	
	
	@RequestMapping(value = PROJECT_ID_ULR+"/permission-popup", method = RequestMethod.GET)
	public ModelAndView getPermissionPopup(@PathVariable long projectId) {
		
		GenericProject project = projectManager.findById(projectId);
		List<PermissionGroup> permissionList = projectManager.findAllPossiblePermission();
		List<User> userList = projectManager.findUserWithoutPermissionByProject(projectId);

		ModelAndView mav = new ModelAndView("fragment/project/project-permission-popup");
		mav.addObject("project", project);
		mav.addObject("userList", userList);
		mav.addObject("permissionList", permissionList);
		
		return mav;
	}
	
	
	@RequestMapping(value = PROJECT_ID_ULR+"/users/{userLogin}/permissions/{permission}", method = RequestMethod.PUT )
	public @ResponseBody
	void addNewPermissionWithLogin(@PathVariable("userLogin") String userLogin, @PathVariable("projectId") long projectId, @PathVariable("permission") String permission) {
		
		User user = projectManager.findUserByLogin(userLogin);
		
		if (user == null) {
			throw new LoginDoNotExistException();
		}
		
		projectManager.addNewPermissionToProject(user.getId(), projectId, permission);
		
	}
	

	// ***************************** permission table *************************************

	
	@RequestMapping(value = PROJECT_ID_ULR+"/user-permissions", method = RequestMethod.GET)
	public ModelAndView getPermissionTable(@PathVariable long projectId) {
		
		throw new NotImplementedException();
		/*
		 * 
		 * todo : refactor that using the UserPermissionDatatableModelHelper
		 * 
		 * 
		List<UserProjectPermissionsBean> userProjectPermissionsBean = projectManager.findUserPermissionsBeansByProject(projectId);		
		List<PermissionGroup> permissionList = projectManager.findAllPossiblePermission();

		ModelAndView mav = new ModelAndView("fragment/project/project-permission-table");
		mav.addObject("permissionList", permissionList);
		mav.addObject("userPermissionList", userProjectPermissionsBean);
		return mav;*/
		
	}
	
	
	
	@RequestMapping(value = PROJECT_ID_ULR+"/users/{userId}/permissions/{permission}", method = RequestMethod.POST)
	public @ResponseBody
	void addNewPermission(@PathVariable("userId") long user, @PathVariable("projectId") long projectId, @PathVariable("permission") String permission) {
		projectManager.addNewPermissionToProject(user, projectId, permission);
	}

	
	@RequestMapping(value = PROJECT_ID_ULR+"/users/{userId}/permissions", method = RequestMethod.DELETE)
	public @ResponseBody
	void removePermission(@PathVariable("userId") long userId, @PathVariable long projectId) {
		projectManager.removeProjectPermission(userId, projectId);
	}

	
	// ********************************** test automation ***********************************
	
	
	//filtering and sorting not supported for now
	@RequestMapping(value = PROJECT_ID_ULR+"/test-automation-projects", method=RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getProjectsTableModel(@PathVariable(PROJECT_ID) long projectId, final DataTableDrawParameters params) {
		List<TestAutomationProject> taProjects = projectManager.findBoundTestAutomationProjects(projectId);
		
		FilteredCollectionHolder<List<TestAutomationProject>> holder = 
			new FilteredCollectionHolder<List<TestAutomationProject>>(taProjects.size(), taProjects);
		
		return new TestAutomationTableModel().buildDataModel(holder, 0, params.getsEcho());
					
	}
	
	@RequestMapping(value = PROJECT_ID_ULR+"/test-automation-projects", method=RequestMethod.POST, headers = "Content-Type=application/json" )
	@ResponseBody
	public void bindTestAutomationProject(@PathVariable(PROJECT_ID) long projectId, @RequestBody TestAutomationProjectRegistrationForm[] projects, Locale locale)
	throws BindException{
		TestAutomationProjectRegistrationForm form=null;
		try{
			Iterator<TestAutomationProjectRegistrationForm> it = Arrays.asList(projects).listIterator();
			while (it.hasNext()){
				form = it.next();				
				projectManager.bindTestAutomationProject(projectId, form.toTestAutomationProject());
			}
		}
		catch(MalformedURLException ex){
			//quick and dirty validation
			BindException be = new BindException(new TestAutomationServer(), "ta-project");
			be.rejectValue("baseURL", null, messageSource.internationalize("error.url.malformed", locale));
			throw be;
		}	
	}
	
	
	@RequestMapping(value = PROJECT_ID_ULR + "/test-automation-enabled", method = RequestMethod.POST, params = "enabled")
	@ResponseBody
	public void enableTestAutomation(@PathVariable(PROJECT_ID) long projectId,
			@RequestParam("enabled") boolean isEnabled) {
		projectManager.changeTestAutomationEnabled(projectId, isEnabled);
	}
	
	@RequestMapping(value = PROJECT_ID_ULR+"/test-automation-projects/{taProjectId}", method=RequestMethod.DELETE )
	@ResponseBody
	public void unbindProject(@PathVariable(PROJECT_ID) Long projectId, @PathVariable("taProjectId") Long taProjectId){
		projectManager.unbindTestAutomationProject(projectId, taProjectId);
	}
	
	
	
	
	// ********************** other stuffs *****************************
	
	
	private final class TestAutomationTableModel extends DataTableModelHelper<TestAutomationProject>{

		
		@Override
		protected Map<String, ?> buildItemData(TestAutomationProject item) {
			Map<String, Object> res = new HashMap<String, Object>();
			
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex() + 1);
			res.put("name", item.getName());
			res.put("server-url", item.getServer().getBaseURL());
			res.put("server-kind", item.getServer().getKind());
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			
			return res;
		}
	}
	

	private static final class ProjectDataTableModelHelper extends DataTableModelHelper<GenericProject> {
		private InternationalizationHelper messageSource;
		private Locale locale;

		private ProjectDataTableModelHelper(Locale locale, InternationalizationHelper messageSource) {
			this.locale = locale;
			this.messageSource = messageSource;
		}

		@Override
		public Object buildItemData(GenericProject project) {
			Map<String, Object> data = new HashMap<String, Object>(11);

			final AuditableMixin auditable = (AuditableMixin) project;

			data.put("project-id", project.getId());
			data.put("index", getCurrentIndex());
			data.put("name", project.getName());
			data.put("active", messageSource.internationalizeYesNo(project.isActive(), locale));
			data.put("label", project.getLabel());
			data.put("created-on", messageSource.localizeDate(auditable.getCreatedOn(), locale));
			data.put("created-by", auditable.getCreatedBy());
			data.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
			data.put("last-mod-by", auditable.getLastModifiedBy());
			data.put("raw-type", ProjectHelper.isTemplate(project) ? "template" : "project");
			data.put("type", "&nbsp;");
			
			return data;
		}
	}

}
