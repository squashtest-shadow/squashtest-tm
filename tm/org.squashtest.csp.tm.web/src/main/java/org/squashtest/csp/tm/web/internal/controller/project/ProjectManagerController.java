/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.ProjectManagerService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

@Controller
@RequestMapping("/projects")
public class ProjectManagerController {

	private ProjectManagerService projectManagerService;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectManagerController.class);



	@Inject
	private MessageSource messageSource;


	/* see bug 33 for details, remove this comment when done */
	/* remember that the indexes here are supposed to match the visible columns in the project view */
	private DataTableMapper projectMapper=new DataTableMapper("projects-table", Project.class)
										.initMapping(9)
										.mapAttribute(Project.class, 2, "name", String.class)
										.mapAttribute(Project.class, 3, "description", String.class)
										.mapAttribute(Project.class, 4, "active", Boolean.class)
										.mapAttribute(Project.class, 5, "audit.createdOn", Date.class)
										.mapAttribute(Project.class, 6, "audit.createdBy", String.class)
										.mapAttribute(Project.class, 7, "audit.lastModifiedOn", Date.class)
										.mapAttribute(Project.class, 8, "audit.lastModifiedBy", String.class);

	@ServiceReference
	public void setProjectManagerService(ProjectManagerService projectService) {
		this.projectManagerService = projectService;
	}


	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addProject(@Valid @ModelAttribute("add-project") Project project) {

		LOGGER.info("description " + project.getDescription());
		LOGGER.info("name " + project.getName());
		LOGGER.info("name " + project.getLabel());

		projectManagerService.addProject(project);

	}


	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {

		ModelAndView mav = new ModelAndView("page/projects/show-project");
		mav.addObject("projects", projectManagerService.findAll());
		return mav;
	}



	@RequestMapping(value = "/list", params = "sEcho")
	public @ResponseBody
	DataTableModel getProjectsTableModel(final DataTableDrawParameters params, final Locale locale) {

		CollectionSorting filter = createCollectionFilter(params, projectMapper);

		FilteredCollectionHolder<List<Project>> holder = projectManagerService
				.findSortedProjects(filter);


		return new DataTableModelHelper<Project>(){
			@Override
			public Object[] buildItemData(Project item) {

				final AuditableMixin newP = (AuditableMixin) item;
				return new Object[]{
						item.getId(),
						getCurrentIndex(),
						formatString(item.getName(), locale),
						formatString(item.getDescription(),locale),
						formatBoolean(item.isActive(),locale),
						formatDate(newP.getCreatedOn(),locale),
						formatString(newP.getCreatedBy(),locale),
						formatDate(newP.getLastModifiedOn(),locale),
						formatString(newP.getLastModifiedBy(),locale)
				};
			}
		}.buildDataModel(holder, filter.getFirstItemIndex()+1, params.getsEcho());

	}


	/* ****************************** data formatters ********************************************** */

	private CollectionSorting createCollectionFilter(final DataTableDrawParameters params,
			final DataTableMapper dtMapper) {
		CollectionSorting filter = new CollectionSorting() {
			@Override
			public int getMaxNumberOfItems() {
				return params.getiDisplayLength();
			}

			@Override
			public int getFirstItemIndex() {
				return params.getiDisplayStart();
			}

			@Override
			public String getSortedAttribute() {
				return dtMapper.pathAt(params.getiSortCol_0());
			}

			@Override
			public String getSortingOrder() {
				return params.getsSortDir_0();
			}

			@Override
			public int getPageSize() {
				return getMaxNumberOfItems();
			}
		};
		return filter;
	}


	private String formatString(String arg, Locale locale){
		if (arg==null){
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale){
		try{
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		}
		catch(Exception anyException){
			return formatNoData(locale);
		}

	}

	private String formatBoolean(Boolean arg, Locale locale){
		try{
			return messageSource.getMessage("squashtm.yesno."+arg.toString(), null, locale);
		}
		catch(Exception anyException){
			return formatNoData(locale);
		}
	}


	private String formatNoData(Locale locale){
		return messageSource.getMessage("squashtm.nodata",null, locale);
	}


}
