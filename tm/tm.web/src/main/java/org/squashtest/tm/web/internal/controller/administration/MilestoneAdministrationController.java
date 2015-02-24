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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneStatusComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;


@Controller
@RequestMapping("administration/milestones")
public class MilestoneAdministrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneAdministrationController.class);

	@Inject
	private InternationalizationHelper messageSource;
	@Inject
	private MilestoneManagerService milestoneManager;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;
	@Inject
	private ProjectFinder projectFinder;
	


	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody long addMilestone(@Valid @ModelAttribute("add-milestone") Milestone milestone) {

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			milestone.setRange(MilestoneRange.GLOBAL);
		} else {
			milestone.setRange(MilestoneRange.RESTRICTED);
			List<GenericProject> projects = projectFinder.findAllICanManage();
			milestone.addProjectsToPerimeter(projects);
		}
		LOGGER.info("description " + milestone.getDescription());
		LOGGER.info("label " + milestone.getLabel());
		LOGGER.info("range " + milestone.getRange());
		LOGGER.info("status " + milestone.getStatus());
		LOGGER.info("end date " + milestone.getEndDate());
		milestoneManager.addMilestone(milestone);
		return milestone.getId();
	}

	@RequestMapping(value = "/{milestoneIds}", method = RequestMethod.DELETE)
	public @ResponseBody void removeMilestones(@PathVariable("milestoneIds") List<Long> milestoneIds) {
		milestoneManager.removeMilestones(milestoneIds);
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showMilestones(Locale locale) {
		ModelAndView mav = new ModelAndView("page/milestones/show-milestones");
		mav.addObject("milestoneStatus", statusComboDataBuilderProvider.get().useLocale(locale).buildMap());
		mav.addObject("editableMilestoneIds", milestoneManager.findAllIdsOfEditableMilestone());
		return mav;
	}
	
	
	@RequestMapping(value = "/list")
	public @ResponseBody DataTableModel getMilestonesTableModel(final DataTableDrawParameters params, final Locale locale) {

		MilestoneDataTableModelHelper helper = new MilestoneDataTableModelHelper(messageSource);
		helper.setLocale(locale);
		Collection<Object> aaData = helper.buildRawModel(milestoneManager.findAllICanSee());
	    DataTableModel model = new DataTableModel("");
	    model.setAaData((List<Object>) aaData);
		return model;
	}
	


}
