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
package org.squashtest.tm.service.milestone;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.users.User;


@Transactional
@DynamicManager(name="squashtest.tm.service.MilestoneManagerService", entity = Milestone.class)
public interface MilestoneManagerService extends CustomMilestoneManager{
	void changeDescription(long milestoneId, String newDescription);
	void changeLabel(long milestoneId, String newLabel);
	void changeStatus(long milestoneId, MilestoneStatus newStatus);
	void changeEndDate(long milestoneId, Date newEndDate);
	void changeOwner(long milestoneId, User Owner);
	void changeRange(long milestoneId, MilestoneRange newRange);


}
