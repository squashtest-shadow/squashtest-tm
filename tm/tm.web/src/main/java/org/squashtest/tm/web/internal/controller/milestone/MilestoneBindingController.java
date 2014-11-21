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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/milestones-binding")
public class MilestoneBindingController {


	private DatatableMapper<String> allProjectsMapper = new NameBasedMapper(2)
	.map("name", "name")
	.map("label", "label");

	private static final String IDS = "Ids[]";

	@Inject
	private MilestoneBindingManagerService service;


	@RequestMapping(value="/project/{projectId}/milestone", method = RequestMethod.POST, params = {IDS})
	@ResponseBody
	public void bindMilestonesToProject(@PathVariable Long projectId, @RequestParam(IDS) List<Long> milestoneIds) {
		service.bindMilestonesToProject(milestoneIds, projectId);
	}

	@RequestMapping(value="/milestone/{milestoneId}/project", method = RequestMethod.POST, params = {IDS})
	@ResponseBody
	public void bindProjectsToMilestone(@PathVariable Long milestoneId, @RequestParam(IDS) List<Long> projectIds) {
		service.bindProjectsToMilestone(projectIds, milestoneId);
	}

	@RequestMapping(value="/project/{projectId}/milestone/{milestoneIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindMilestoneFromProject(@PathVariable(RequestParams.PROJECT_ID) Long projectId, @PathVariable("milestoneIds") List<Long> milestoneIds){
		service.unbindMilestonesFromProject(milestoneIds, projectId);
	}
	@RequestMapping(value="/milestone/{milestoneId}/project/{projectIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindProjectFromMilestone(@PathVariable("milestoneId") Long milestoneId, @PathVariable("projectIds") List<Long> projectIds){
		service.unbindProjectsFromMilestone(projectIds, milestoneId);
	}


	@RequestMapping(value="/milestone/{milestoneId}/project", method = RequestMethod.GET, params = {RequestParams.S_ECHO_PARAM, "bindable"})
	@ResponseBody
	public DataTableModel getBindableProjectForMilestoneTableModel(@PathVariable Long milestoneId, final DataTableDrawParameters params, final Locale locale){

		PagingAndSorting sorter = new DataTableSorting(params, allProjectsMapper);

		Filtering filter = new DataTableFiltering(params);

		PagedCollectionHolder<List<GenericProject>> holder = service.getAllBindableProjectForMilestone(milestoneId, sorter, filter);

		return new ProjectDataTableModelHelper().buildDataModel(holder, params.getsEcho());
	}



	@RequestMapping(value = "/milestone/{milestoneId}/project", method = RequestMethod.GET, params = {RequestParams.S_ECHO_PARAM, "binded" })
	@ResponseBody
	public 	DataTableModel getBindedProjectForMilestoneTableModel(@PathVariable Long milestoneId, final DataTableDrawParameters params, final Locale locale){

		PagingAndSorting sorter = new DataTableSorting(params, allProjectsMapper);

		Filtering filter = new DataTableFiltering(params);

		PagedCollectionHolder<List<GenericProject>> holder = service.getAllBindedProjectForMilestone(milestoneId, sorter, filter);

		return new ProjectDataTableModelHelper().buildDataModel(holder, params.getsEcho());
	}

	private static final class ProjectDataTableModelHelper extends DataTableModelBuilder<GenericProject> {

		private ProjectDataTableModelHelper() {
		}

		@Override
		public Object buildItemData(GenericProject project) {
			Map<String, Object> data = new HashMap<String, Object>(4);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, project.getId());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			data.put("checkbox", " ");
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, project.getName());
			data.put("label", project.getLabel());
			data.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return data;
		}
	}
}
