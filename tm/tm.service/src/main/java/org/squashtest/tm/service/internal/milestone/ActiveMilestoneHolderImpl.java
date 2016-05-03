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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;

import com.google.common.base.Optional;

@Component
public class ActiveMilestoneHolderImpl implements ActiveMilestoneHolder {

	@Inject
	private MilestoneFinderService milestoneFinderService;

	private final ThreadLocal<Optional<Milestone>> activeMilestoneHolder = new ThreadLocal<>();

	private final ThreadLocal<Long> activeMilestoneIdHolder = new ThreadLocal<>();

	@Override
	public Optional<Milestone> getActiveMilestone() {

		if (activeMilestoneHolder.get() == null) {
			List<Milestone> visibles = milestoneFinderService.findAllVisibleToCurrentUser();
			final Long milestoneId = activeMilestoneIdHolder.get();
			Milestone milestone = (Milestone) CollectionUtils.find(visibles, new Predicate() {
				@Override
				public boolean evaluate(Object milestone) {
					return ((Milestone) milestone).getId().equals(milestoneId);
				}
			});
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

}
