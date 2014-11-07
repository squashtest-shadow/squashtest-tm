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
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.milestone.ExpandedMilestone;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneStatusComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;


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

	private DatatableMapper<String> milestoneMapper = new NameBasedMapper().map("label", "label")
			.map("description", "description").map("range", "range").map("status", "status").map("endDate", "endDate")
			.map("created-on", "audit.createdOn").map("created-by", "audit.createdBy")
			.map("last-mod-on", "audit.lastModifiedOn").map("last-mod-by", "audit.lastModifiedBy");

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody void addMilestone(@Valid @ModelAttribute("add-milestone") Milestone milestone) {

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			milestone.setRange(MilestoneRange.GLOBAL);
		} else {
			milestone.setRange(MilestoneRange.RESTRICTED);
		}
		LOGGER.info("description " + milestone.getDescription());
		LOGGER.info("label " + milestone.getLabel());
		LOGGER.info("range " + milestone.getRange());
		LOGGER.info("status " + milestone.getStatus());
		LOGGER.info("end date " + milestone.getEndDate());
		milestoneManager.addMilestone(milestone);
	}

	@RequestMapping(value = "/{milestoneIds}", method = RequestMethod.DELETE)
	public @ResponseBody void removeMilestones(@PathVariable("milestoneIds") List<Long> milestoneIds) {
		milestoneManager.removeMilestones(milestoneIds);
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showMilestones(Locale locale) {
		ModelAndView mav = new ModelAndView("page/milestones/show-milestones");
		mav.addObject("milestones", milestoneManager.findAll());
		mav.addObject("milestoneStatus", statusComboDataBuilderProvider.get().useLocale(locale).buildMap());
		return mav;
	}

	@RequestMapping(value = "/list", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody DataTableModel getMilestonesTableModel(final DataTableDrawParameters params,
			final Locale locale) {

		PagingAndSorting sorter = createPaging(params, milestoneMapper);

		List<Milestone> sortedMilestone = milestoneManager.findSortedMilestones(sorter);
		
		Filtering filter = new DataTableFiltering(params);

		List<ExpandedMilestone> expandedMilestones = getExpandedMilestone(sortedMilestone, locale);

		PagedCollectionHolder<List<Milestone>> holder = milestoneManager.filterMilestone(expandedMilestones, filter, sorter);

		MilestoneDataTableModelHelper helper = new MilestoneDataTableModelHelper(messageSource);
		helper.setLocale(locale);
		return helper.buildDataModel(holder, params.getsEcho());

	}

	private List<ExpandedMilestone> getExpandedMilestone(List<Milestone> sortedMilestone, Locale locale) {
		List<ExpandedMilestone> expandedMilestones = new ArrayList<ExpandedMilestone>();
		for (Milestone milestone : sortedMilestone) {
			ExpandedMilestone expMilestone = new ExpandedMilestone();
			expMilestone.setMilestone(milestone);
			expMilestone.setTranslatedStatus(messageSource.internationalize(milestone.getStatus(), locale));
			expMilestone.setTranslatedEndDate(messageSource.localizeDate(milestone.getEndDate(), locale));
			expandedMilestones.add(expMilestone);
		}
		return expandedMilestones;
	}

	/* ****************************** data formatters ********************************************** */

	private PagingAndSorting createPaging(final DataTableDrawParameters params, final DatatableMapper<?> mapper) {
		return new DataTableSorting(params, mapper);
	}

}
