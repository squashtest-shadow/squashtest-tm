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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.project.WorkspaceWizardModel;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager;

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
	private InternationalizationHelper internationalizationHelper;
	@Inject
	private MessageSource messageSource;

	@Inject
	private TestAutomationServerManagerService taServerService;

	@Inject
	private WorkspaceWizardManager wizardManager;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;


	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";

	@ModelAttribute("projectsPageSize")
	public long populateProjectsPageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {
		ModelAndView mav = new ModelAndView("page/projects/show-projects");
		mav.addObject("projects", projectFinder.findAllOrderedByName(Pagings.DEFAULT_PAGING));
		return mav;
	}

	@RequestMapping(value = "{projectId}", method = RequestMethod.GET)
	public ModelAndView showProjectEditor(@PathVariable long projectId, Locale locale) {
		return getProjectInfos(projectId, locale);
	}

	@RequestMapping(value = "{projectId}/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long projectId, Locale locale) {

		AdministrableProject adminProject = projectFinder.findAdministrableProjectById(projectId);

		// user permissions data
		List<PartyProjectPermissionsBean> partyProjectPermissionsBean = projectFinder
				.findPartyPermissionsBeanByProject(new DefaultPagingAndSorting("login", 25),
						DefaultFiltering.NO_FILTERING, projectId).getPagedItems();
		Collection<Object> partyPermissions = new PartyPermissionDatatableModelHelper(locale, internationalizationHelper)
		.buildRawModel(partyProjectPermissionsBean);

		List<PermissionGroup> availablePermissions = projectFinder.findAllPossiblePermission();

		// test automation data
		Collection<TestAutomationServer> availableTAServers = taServerService.findAllOrderedByName();

		// bugtracker data
		Map<Long, String> comboDataMap = createComboDataForBugtracker(locale);

		// execution status data
		CampaignLibrary cl = adminProject.getCampaignLibrary();
		Map<String, Boolean> allowedStatuses = new HashMap<String, Boolean>();
		allowedStatuses.put(ExecutionStatus.SETTLED.toString(), cl.allowsStatus(ExecutionStatus.SETTLED));
		allowedStatuses.put(ExecutionStatus.UNTESTABLE.toString(), cl.allowsStatus(ExecutionStatus.UNTESTABLE));

		// populating model
		ModelAndView mav = new ModelAndView("page/projects/project-info");

		mav.addObject("adminproject", adminProject);
		mav.addObject("availableTAServers", availableTAServers);
		mav.addObject("bugtrackersList", JsonHelper.serialize(comboDataMap));
		mav.addObject("bugtrackersListEmpty", comboDataMap.size() == 1);
		mav.addObject("userPermissions", partyPermissions);
		mav.addObject("availablePermissions", availablePermissions);
		mav.addObject("attachments", attachmentsHelper.findAttachments(adminProject.getProject()));
		mav.addObject("allowedStatuses", allowedStatuses);


		return mav;
	}

	// ********************** Wizard administration section ************

	@RequestMapping(value = "{projectId}/wizards")
	public String getWizardsManager(@PathVariable("projectId") Long projectId, Model model) {

		GenericProject project = projectFinder.findById(projectId);

		Collection<WorkspaceWizardModel> availableWizards = toWizardModel(wizardManager.findAll());

		Collection<String> enabledWizards = new ArrayList<String>();
		enabledWizards.addAll(project.getTestCaseLibrary().getEnabledPlugins());
		enabledWizards.addAll(project.getRequirementLibrary().getEnabledPlugins());
		enabledWizards.addAll(project.getCampaignLibrary().getEnabledPlugins());

		model.addAttribute("availableWizards", availableWizards);
		model.addAttribute("enabledWizards", enabledWizards);
		model.addAttribute("projectId", projectId);

		return "project-tabs/workspace-wizards-tab.html";

	}


	private Collection<WorkspaceWizardModel> toWizardModel(Collection<WorkspaceWizard> wizards) {
		Locale locale = LocaleContextHolder.getLocale();
		List<WorkspaceWizardModel> output = new ArrayList<WorkspaceWizardModel>(wizards.size());

		for (WorkspaceWizard wizard : wizards) {
			WorkspaceWizardModel model = new WorkspaceWizardModel(wizard);
			model.setType(internationalizationHelper.getMessage("label.Wizard", null, locale));
			output.add(model);
		}

		return output;
	}

	private Map<Long, String> createComboDataForBugtracker(Locale locale) {
		Map<Long, String> comboDataMap = new HashMap<Long, String>();
		for (BugTracker b : bugtrackerFinderService.findAll()) {
			comboDataMap.put(b.getId(), b.getName());
		}
		comboDataMap.put(-1L, internationalizationHelper.getMessage(PROJECT_BUGTRACKER_NAME_UNDEFINED, null, locale));
		return comboDataMap;

	}

}
