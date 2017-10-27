/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.milestone;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;

import com.google.common.base.Optional;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;

@Component
public class ActiveMilestoneHolderImpl implements ActiveMilestoneHolder {

	@Inject
	private MilestoneFinderService milestoneFinderService;

	@Inject
	MilestoneModelService milestoneModelService;

	private final ThreadLocal<Optional<Milestone>> activeMilestoneHolder = new ThreadLocal<>();

	private final ThreadLocal<Long> activeMilestoneIdHolder = new ThreadLocal<>();

	@Inject
	private TestCaseAdvancedSearchService advancedSearchService;

	@Override
	public Optional<Milestone> getActiveMilestone() {

		if (activeMilestoneHolder.get() == null) {
			final Long milestoneId = activeMilestoneIdHolder.get();
			List<JsonMilestone> visibles = advancedSearchService.findAllVisibleMilestonesToCurrentUser();

			Milestone milestone = new Milestone();
			for(JsonMilestone mile : visibles){
				if(Long.valueOf(mile.getId()).equals(milestoneId)){
					milestone = milestoneFinderService.findById(mile.getId());
				}else{
					milestone = null;
				}
			}

			activeMilestoneHolder.set(Optional.fromNullable(milestone));
		}

		return activeMilestoneHolder.get();
	}


	@Override
	public void setActiveMilestone(final Long milestoneId) {
		// just set the id. They milestone will be fetched from database only when asked
		activeMilestoneIdHolder.set(milestoneId);
	}

	@Override
	public void clearContext() {
		activeMilestoneHolder.remove();
		activeMilestoneIdHolder.remove();
	}

	@Override
	public Optional<Long> getActiveMilestoneId() {
		return Optional.fromNullable(activeMilestoneIdHolder.get());
	}

}
