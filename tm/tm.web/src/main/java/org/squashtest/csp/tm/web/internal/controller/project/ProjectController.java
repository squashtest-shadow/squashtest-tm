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

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.service.project.ProjectFinder;
import org.squashtest.csp.tm.web.internal.model.jquery.FilterModel;

@Controller
@RequestMapping("/projects")
public class ProjectController {

	@Inject
	private ProjectFinder projectFinder;

	@RequestMapping(method = RequestMethod.GET, params = "format=picker")
	@ResponseBody
	public FilterModel getProjectPickerModel() {
		List<Project> projects = projectFinder.findAllOrderedByName();
		FilterModel model = new FilterModel();

		for (Project project : projects) {
			model.addProject(project.getId(), project.getName());
		}

		return model;
	}
}
