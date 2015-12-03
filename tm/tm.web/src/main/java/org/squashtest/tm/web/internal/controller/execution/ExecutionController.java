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
package org.squashtest.tm.web.internal.controller.execution;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Controller
@RequestMapping("/executions")
public class ExecutionController {

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private Provider<ExecutionAssignmentComboDataBuilder> assignmentComboBuilderProvider;

	@Inject
	private Provider<ExecutionStatusComboDataBuilder> statusComboDataBuilderProvider;

	@Inject
	private InternationalizationHelper i18n;



	@RequestMapping(value = "/assignment-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildImportanceComboData(Locale locale) {
		return assignmentComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildTypeComboData(Locale locale) {
		return statusComboDataBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(value = "/add-iteration/{campaignId}", method = RequestMethod.POST)
	public @ResponseBody
 void addNewIteration(@PathVariable long campaignId, Locale locale,
			@CurrentMilestone Milestone activeMilestone) throws BindException {

		Iteration iteration = new Iteration();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		iteration.setName(
				i18n.internationalize("label.generatedIT.name", locale) + " " + dateFormat.format(date).toString());
		iteration.setDescription(i18n.internationalize("label.generatedIT.description", locale));
		iteration.setReference("");

		campaignLibraryNavigationService.addIterationToCampaign(iteration, campaignId, false);

	}

}
