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
package org.squashtest.csp.tm.web.internal.controller.administration;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.service.ProjectManagerService;
import org.squashtest.csp.tm.service.project.GenericProjectFinder;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;

@Controller
@RequestMapping("/administration/projects")
public class ProjectAdministrationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAdministrationController.class);

	@Inject
	private ProjectManagerService projectManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	/**
	 * Finder service for generic project. We manage here both projects and templates !
	 */
	@Inject
	private GenericProjectFinder projectFinder;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addProject(@Valid @ModelAttribute("add-project") Project project) {

		LOGGER.info("description " + project.getDescription());
		LOGGER.info("name " + project.getName());
		LOGGER.info("label " + project.getLabel());

		projectManagerService.addProject(project);

	}

	@ModelAttribute("projectsPageSize")
	public long populateCustomFieldsPageSize() {
		return DefaultPaging.FIRST_PAGE.getPageSize();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {

		ModelAndView mav = new ModelAndView("page/projects/show-projects");
		mav.addObject("projects", projectFinder.findAllOrderedByName(DefaultPaging.FIRST_PAGE));
		return mav;
	}
}
