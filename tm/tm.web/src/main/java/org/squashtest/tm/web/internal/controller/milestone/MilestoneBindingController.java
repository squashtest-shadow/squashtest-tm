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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Collection;
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
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.administration.MilestoneDataTableModelHelper;
import org.squashtest.tm.web.internal.helper.ProjectHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;


@Controller
@RequestMapping("/milestones-binding")
public class MilestoneBindingController {


	private static final String IDS = "Ids[]";

	@Inject
	private InternationalizationHelper messageSource;
	@Inject
	private MilestoneBindingManagerService service;
	@Inject
	private MilestoneManagerService milestoneService;
	

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
	@RequestMapping(value="/project/{projectId}/milestone", method = RequestMethod.POST, params = {IDS, "bindObjects"})
	@ResponseBody
	public void bindMilestonesToProjectAndBindObject(@PathVariable Long projectId, @RequestParam(IDS) List<Long> milestoneIds) {
		service.bindMilestonesToProjectAndBindObject(projectId, milestoneIds);
	}

	
	
	
	@RequestMapping(value="/project/{projectId}/milestone/{milestoneIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindMilestoneFromProject(@PathVariable(RequestParams.PROJECT_ID) Long projectId, @PathVariable("milestoneIds") List<Long> milestoneIds){
		service.unbindMilestonesFromProject(milestoneIds, projectId);
	}
	
	
	@RequestMapping(value="/milestone/{milestoneId}/template", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindTemplateFroMilestone( @PathVariable("milestoneId") Long milestoneId){
		service.unbindTemplateFrom(milestoneId);
	}
	
	
	
	@RequestMapping(value="/milestone/{milestoneId}/project/{projectIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindProjectFromMilestone(@PathVariable("milestoneId") Long milestoneId, @PathVariable("projectIds") List<Long> projectIds){
		service.unbindProjectsFromMilestone(projectIds, milestoneId);
	}

	@RequestMapping(value="/milestone/{milestoneId}/project/{projectIds}/keep-in-perimeter", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindProjectFromMilestoneKeepInPerimeter(@PathVariable("milestoneId") Long milestoneId, @PathVariable("projectIds") List<Long> projectIds){
		service.unbindProjectsFromMilestoneKeepInPerimeter(projectIds, milestoneId);
	}
	

	@RequestMapping(value="/milestone/{milestoneId}/project", method = RequestMethod.GET, params = {"bindable"})
	@ResponseBody
	public DataTableModel getBindableProjectForMilestoneTableModel(@PathVariable Long milestoneId, final Locale locale){
		Milestone milestone = milestoneService.findById(milestoneId);
		Collection<GenericProject> data = service.getAllBindableProjectForMilestone(milestoneId);
		return buildProjectTableModel(data, milestone, locale);
	}

	private DataTableModel buildProjectTableModel(Collection<GenericProject> data, Milestone milestone, Locale locale){
		ProjectDataTableModelHelper helper = new ProjectDataTableModelHelper(milestone, messageSource, locale);
		Collection<Object> aaData = helper.buildRawModel(data);
	    DataTableModel model = new DataTableModel("");
	    model.setAaData((List<Object>) aaData);
		return model;	
	}
	
	private DataTableModel buildMilestoneTableModel(Collection<Milestone> data){
		MilestoneDataTableModelHelper helper = new MilestoneDataTableModelHelper(messageSource);
		Collection<Object> aaData = helper.buildRawModel(data);
	    DataTableModel model = new DataTableModel("");
	    model.setAaData((List<Object>) aaData);
		return model;	
	}
	

	@RequestMapping(value="/project/{projectId}/milestone", method = RequestMethod.GET, params = { "bindable", "type"})
	@ResponseBody
	public DataTableModel getBindableMilestoneForProjectTableModel(@PathVariable Long projectId, final Locale locale, @RequestParam("type") String type){

		Collection<Milestone> data = service.getAllBindableMilestoneForProject(projectId, type);
	
		return buildMilestoneTableModel(data);			
	}
	
	
	
	@RequestMapping(value = "/project/{projectId}/milestone", method = RequestMethod.GET, params = {"binded" })
	@ResponseBody
	public 	DataTableModel getBindedMilestoneForProjectTableModel(@PathVariable Long projectId, final Locale locale){
		
		Collection<Milestone> data = service.getAllBindedMilestoneForProject(projectId);
		
		return buildMilestoneTableModel(data);			
	}
	


	@RequestMapping(value = "/milestone/{milestoneId}/project", method = RequestMethod.GET, params = {"binded" })
	@ResponseBody
	public 	DataTableModel getBindedOrPerimeterProjectForMilestoneTableModel(@PathVariable Long milestoneId, final Locale locale){
		Milestone milestone = milestoneService.findById(milestoneId);
		Collection<GenericProject> data = service.getAllProjectForMilestone(milestoneId);
		return buildProjectTableModel(data, milestone, locale);

	}

	private static final class ProjectDataTableModelHelper extends DataTableModelBuilder<GenericProject> {

		private Milestone milestone;
		private InternationalizationHelper messageSource;
		
		private Locale locale;

		
		public ProjectDataTableModelHelper(Milestone milestone, InternationalizationHelper messageSource, Locale locale) {
			this.milestone = milestone;
			this.messageSource = messageSource;
			this.locale = locale;
		}

		@Override
		public Object buildItemData(GenericProject project) {
			Map<String, Object> data = new HashMap<String, Object>(4);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, project.getId());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex() +1);
			data.put("type", "&nbsp;");
			data.put("raw-type", ProjectHelper.isTemplate(project) ? "template" : "project");
			data.put("checkbox", " ");
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, project.getName());
			data.put("label", project.getLabel());
			data.put("binded", messageSource.internationalizeYesNo(project.isBoundToMilestone(milestone), locale) );
			data.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return data;
		}
	}
}
