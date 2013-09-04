/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.IndexBasedMapper;

@Controller
@RequestMapping("/administration/bugtrackers")
public class BugTrackerAdministrationController {

	@Inject
	private InternationalizationHelper messageSource;
	private BugTrackerManagerService bugTrackerManagerService;
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerAdministrationController.class);



/* remember that the indexes here are supposed to match the visible columns in the bugtracker view */
	private DatatableMapper<Integer> bugtrackerMapper=new IndexBasedMapper()
										.mapAttribute(2, "name", BugTracker.class)
										.mapAttribute(3, "kind", BugTracker.class)
										.mapAttribute(4, "url", BugTracker.class)
										.mapAttribute(5, "iframeFriendly", BugTracker.class);

	@ServiceReference
	public void setBugtrackerManagerService(BugTrackerManagerService bugTrackerManagerService) {
		this.bugTrackerManagerService = bugTrackerManagerService;
	}


	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addProject(@Valid @ModelAttribute("add-bugtracker") BugTracker bugtracker) {

		LOGGER.info("name " + bugtracker.getName());
		LOGGER.info("kind " + bugtracker.getKind());
		LOGGER.info("iframe " + bugtracker.isIframeFriendly());
		LOGGER.info("url " + bugtracker.getUrl());
		bugTrackerManagerService.addBugTracker(bugtracker);

	}


	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showBugtrackers() {
		Set<String> bugtrackerKinds = bugTrackerManagerService.findBugTrackerKinds();
		ModelAndView mav = new ModelAndView("page/bugtrackers/show-bugtrackers");
		mav.addObject("bugtrackers", bugTrackerManagerService.findAll());
		mav.addObject("bugtrackerKinds", bugtrackerKinds);
		return mav;
	}

	@RequestMapping(value = "/list", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getBugtrackerTableModel(final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting filter = createPaging(params, bugtrackerMapper);

		PagedCollectionHolder<List<BugTracker>> holder = bugTrackerManagerService.findSortedBugtrackers(filter);


		BugtrackerDataTableModelHelper helper = new BugtrackerDataTableModelHelper(messageSource);
		helper.setLocale(locale);
		return helper.buildDataModel(holder,  params.getsEcho());

	}

	/* ****************************** data formatters ********************************************** */

	private PagingAndSorting createPaging(final DataTableDrawParameters params, final DatatableMapper<?> mapper) {
		return new DataTableSorting(params, mapper);
	}

}
