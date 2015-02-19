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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;

public interface CustomRequirementVersionDao {

	/**
	 * Returns a requirement by its ID. Note that we mean Requirement, and not RequirementVersion
	 * 
	 * @param requirementId
	 * @return
	 */
	Requirement findRequirementById(long requirementId);

	/**
	 * Find which version of a requirement that is bound to a given milestone. if MilestoneId is null
	 * the latest version will be returned
	 * 
	 * @param requirementId
	 * @param milestoneId
	 * @return
	 */
	RequirementVersion findByRequirementIdAndMilestone(long requirementId, Long milestoneId);

	/**
	 * @param requirementId
	 * @param pas
	 * @return the paged, sorted versions of the given requirement.
	 */
	List<RequirementVersion> findAllByRequirement(long requirementId, PagingAndSorting pas);

}
