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
package org.squashtest.tm.service.milestone;

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.users.User;
import static org.squashtest.tm.service.security.Authorizations.MILESTONE_FEAT_ENABLED;

@Transactional
@DynamicManager(name = "squashtest.tm.service.MilestoneManagerService", entity = Milestone.class)
public interface MilestoneManagerService extends CustomMilestoneManager {
	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeDescription(long milestoneId, String newDescription);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeLabel(long milestoneId, String newLabel);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeStatus(long milestoneId, MilestoneStatus newStatus);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeEndDate(long milestoneId, Date newEndDate);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeOwner(long milestoneId, User Owner);

	@PreAuthorize(MILESTONE_FEAT_ENABLED)
	void changeRange(long milestoneId, MilestoneRange newRange);



}
