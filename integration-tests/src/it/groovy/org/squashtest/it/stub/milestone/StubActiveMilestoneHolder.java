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
package org.squashtest.it.stub.milestone;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

import com.google.common.base.Optional;

public class StubActiveMilestoneHolder implements ActiveMilestoneHolder {

	@Override
	public Optional<Milestone> getActiveMilestone() {
		// TODO Auto-generated method stub
		return Optional.absent();
	}

	@Override
	public void setActiveMilestone(Long milestoneId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearContext() {
		// TODO Auto-generated method stub

	}

}
