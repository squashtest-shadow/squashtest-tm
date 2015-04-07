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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

@Controller
@RequestMapping("/milestones")
public class MilestoneController {

	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;

	@Inject
	private MilestoneFinderService milestoneFinder;

	@Inject
	private InternationalizationHelper i18nHelper;

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildStatusComboData(Locale locale) {
		return statusComboDataBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	@RequestMapping(params="selectable", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel<Milestone> findUserSelectableMilestones(){

		List<Milestone> milestones = milestoneFinder.findAllVisibleToCurrentUser();

		// they must be initially sorted by date descending
		Collections.sort(milestones, new Comparator<Milestone>() {
			@Override
			public int compare(Milestone o1, Milestone o2) {
				return (o2.getEndDate().before(o1.getEndDate())) ? -1 : 1;
			}
		});

		// now make the model
		PagedCollectionHolder<List<Milestone>> holderCollection =
				new SinglePageCollectionHolder<List<Milestone>>(milestones);


		Locale locale = LocaleContextHolder.getLocale();
		return new MilestoneTableModelHelper(i18nHelper, locale).buildDataModel(holderCollection, "0");

	}


}
