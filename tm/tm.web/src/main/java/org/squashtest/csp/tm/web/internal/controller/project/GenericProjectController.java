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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.service.project.GenericProjectManagerService;
import org.squashtest.csp.tm.web.internal.helper.ProjectHelper;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

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

	private DataTableMapper projectMapper = new DataTableMapper("projects-table", GenericProject.class).initMapping(9)
			.mapAttribute(GenericProject.class, 2, "name", String.class)
			.mapAttribute(GenericProject.class, 3, "label", String.class)
			.mapAttribute(GenericProject.class, 4, "active", boolean.class)
			.mapAttribute(GenericProject.class, 5, "audit.createdOn", Date.class)
			.mapAttribute(GenericProject.class, 6, "audit.createdBy", String.class)
			.mapAttribute(GenericProject.class, 7, "audit.lastModifiedOn", Date.class)
			.mapAttribute(GenericProject.class, 8, "audit.lastModifiedBy", String.class);

	@RequestMapping(value = "", params = "sEcho", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getProjectsTableModel(final DataTableDrawParameters params, final Locale locale) {
		PagingAndSorting filter = new DataTableMapperPagingAndSortingAdapter(params, projectMapper,
				SortedAttributeSource.SINGLE_ENTITY);

		PagedCollectionHolder<List<GenericProject>> holder = projectManager.findSortedProjects(filter);

		return new ProjectDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());

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

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "isTemplate=false")
	public @ResponseBody void createNewProject(@Valid @ModelAttribute("add-project") Project project) {
		projectManager.persist(project);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "isTemplate=true")
	public @ResponseBody void createNewProject(@Valid @ModelAttribute("add-project") ProjectTemplate template) {
		projectManager.persist(template);
	}
}
