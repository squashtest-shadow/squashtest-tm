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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.service.ProjectManagerService;
import org.squashtest.csp.tm.service.project.GenericProjectFinder;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

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

	/* see bug 33 for details, remove this comment when done */
	/* remember that the indexes here are supposed to match the visible columns in the project view */
	private DataTableMapper projectMapper = new DataTableMapper("projects-table", GenericProject.class).initMapping(9)
			.mapAttribute(GenericProject.class, 2, "name", String.class)
			.mapAttribute(GenericProject.class, 3, "label", String.class)
			.mapAttribute(GenericProject.class, 4, "active", boolean.class)
			.mapAttribute(GenericProject.class, 5, "audit.createdOn", Date.class)
			.mapAttribute(GenericProject.class, 6, "audit.createdBy", String.class)
			.mapAttribute(GenericProject.class, 7, "audit.lastModifiedOn", Date.class)
			.mapAttribute(GenericProject.class, 8, "audit.lastModifiedBy", String.class);

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addProject(@Valid @ModelAttribute("add-project") Project project) {

		LOGGER.info("description " + project.getDescription());
		LOGGER.info("name " + project.getName());
		LOGGER.info("label " + project.getLabel());

		projectManagerService.addProject(project);

	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {

		ModelAndView mav = new ModelAndView("page/projects/show-projects");
		mav.addObject("projects", projectFinder.findAllOrderedByName());
		return mav;
	}

	@RequestMapping(value = "/list", params = "sEcho", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getProjectsTableModel(final DataTableDrawParameters params, final Locale locale) {
		PagingAndSorting filter = new DataTableMapperPagingAndSortingAdapter(params, projectMapper, SortedAttributeSource.SINGLE_ENTITY);

		PagedCollectionHolder<List<GenericProject>> holder = projectFinder.findSortedProjects(filter);

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
		public Object[] buildItemData(GenericProject item) {

			final AuditableMixin auditable = (AuditableMixin) item;
			return new Object[] { item.getId(), getCurrentIndex(), formatString(item.getName(), locale, messageSource),
					formatString(item.getLabel(), locale, messageSource),
					formatBoolean(item.isActive(), locale, messageSource),
					formatDate(auditable.getCreatedOn(), locale, messageSource),
					formatString(auditable.getCreatedBy(), locale, messageSource),
					formatDate(auditable.getLastModifiedOn(), locale, messageSource),
					formatString(auditable.getLastModifiedBy(), locale, messageSource) };
		}
	}

	/* ****************************** data formatters ********************************************** */

	private static String formatString(String arg, Locale locale, InternationalizationHelper messageSource) {
		if (arg == null) {
			return formatNoData(locale, messageSource);
		} else {
			return arg;
		}
	}

	private static String formatDate(Date date, Locale locale, InternationalizationHelper messageSource) {
			return messageSource.localizeDate(date, locale);
	}

	private static String formatBoolean(boolean arg, Locale locale, InternationalizationHelper messageSource) {
		try {
			return messageSource.internationalize("squashtm.yesno." + arg, locale);
		} catch (NoSuchMessageException ex) {
			LOGGER.warn("Internationalization key not found : " + ex.getMessage(), ex);
			return formatNoData(locale, messageSource);
		}
	}

	private static String formatNoData(Locale locale, InternationalizationHelper messageSource) {
		return messageSource.internationalize("squashtm.nodata", locale);
	}

}
