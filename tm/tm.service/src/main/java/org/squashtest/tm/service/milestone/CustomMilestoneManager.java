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

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;

@Transactional
public interface CustomMilestoneManager extends MilestoneFinderService {
	void addMilestone(Milestone milestone);
	List<Milestone>  findAll();
	void removeMilestones(Collection<Long> ids);

	/**
	 * 
	 * @param milestoneId the id of the milestone
	 * @return true if the user has rights to edit the milestone
	 */
	boolean canEditMilestone(long milestoneId);

	/**
	 * Throw exception if the user try do edit a milestone he can't
	 * @param milestoneId the id of the milestone
	 */
	void verifyCanEditMilestone(long milestoneId);

	/**
	 * Throw exception if the user try do edit milestone range and can't
	 * @param milestoneId the id of the milestone
	 */
	void verifyCanEditMilestoneRange();

	/**
	 * 
	 * @return list of Id of editable milestone for current user
	 */
	List<Long> findAllIdsOfEditableMilestone();

	/**
	 * 
	 * @return liste of all milestone the current user can see
	 */
	List<Milestone>  findAllICanSee();

	boolean isBoundToATemplate(Long milestoneId);


	void cloneMilestone(long motherId, Milestone milestone, boolean bindToRequirements, boolean bindToTestCases,
			boolean bindToCampaigns);
	
	void synchronize(long sourceId, long targetId, boolean extendPerimeter, boolean isUnion);

	/**
	 * When a node has been copied to another project some milestones might no longer be available.
	 * This method will trim unbind the member from them.
	 * 
	 * @param member
	 */
	void migrateMilestones(MilestoneHolder member);
}
