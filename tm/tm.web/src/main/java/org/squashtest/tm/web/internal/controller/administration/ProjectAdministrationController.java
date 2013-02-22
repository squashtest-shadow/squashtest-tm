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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Controller
@RequestMapping("/administration/projects")
public class ProjectAdministrationController {
	/**
	 * Finder service for generic project. We manage here both projects and templates !
	 */
	@Inject
	private GenericProjectFinder projectFinder;
	@Inject
	private BugTrackerFinderService bugtrackerFinderService;
	@Inject
	private InternationalizationHelper messageSource;
	
	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";

	@ModelAttribute("projectsPageSize")
	public long populateProjectsPageSize() {
		return DefaultPaging.FIRST_PAGE.getPageSize();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {
		ModelAndView mav = new ModelAndView("page/projects/show-projects");
		mav.addObject("projects", projectFinder.findAllOrderedByName(DefaultPaging.FIRST_PAGE));
		return mav;
	}
	
	@RequestMapping(value = "{projectId}", method = RequestMethod.GET)
	public ModelAndView showProjectEditor(@PathVariable long projectId, Locale locale) {
		return getProjectInfos(projectId, locale);
	}
	
	@RequestMapping(value = "{projectId}/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long projectId, Locale locale) {
		
		AdministrableProject adminProject = projectFinder.findAdministrableProjectById(projectId);
		
		
		//user permissions data
		List<PartyProjectPermissionsBean> partyProjectPermissionsBean = projectFinder.findPartyPermissionsBeanByProject(new DefaultPagingAndSorting("login", 25), DefaultFiltering.NO_FILTERING, projectId).getPagedItems();		
		List<Map<?,?>> partyPermissions = new PartyPermissionDatatableModelHelper(locale,messageSource).buildAllData(partyProjectPermissionsBean);
		
		List<PermissionGroup> availablePermissions = projectFinder.findAllPossiblePermission();
		
		//test automation data
		TestAutomationServer taServerCoordinates = projectFinder.getLastBoundServerOrDefault((long) adminProject.getProject().getId());
		List<TestAutomationProject> boundProjects = projectFinder.findBoundTestAutomationProjects(projectId);

		
		//bugtracker data
		Map<Long, String> comboDataMap = createComboDataForBugtracker(locale);
		
		
		//populating model
		ModelAndView mav = new ModelAndView("page/projects/project-info");
		
		mav.addObject("adminproject", adminProject);
		mav.addObject("taServer", taServerCoordinates);
		mav.addObject("boundTAProjects", boundProjects);
		mav.addObject("bugtrackersList", JsonHelper.serialize(comboDataMap));
		mav.addObject("bugtrackersListEmpty", comboDataMap.size() == 1);
		mav.addObject("userPermissions", partyPermissions);
		mav.addObject("availablePermissions", availablePermissions);
		
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

}
