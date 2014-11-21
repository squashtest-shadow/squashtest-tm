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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.GenericProject;

@Transactional
public interface CustomMilestoneBindingManager {
	List<Milestone> getAllBindableMilestoneForProject(Long projectId);
	
	void bindMilestonesToProject(List<Long> milestoneIds, Long projectId);

	void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId);

	List<Milestone> getAllBindedMilestoneForProject(Long projectId);


	List<GenericProject> getAllBindableProjectForMilestone(Long milestoneId);

	List<GenericProject> getAllBindedProjectForMilestone(Long milestoneId);

	PagedCollectionHolder<List<GenericProject>> getAllBindedProjectForMilestone(Long milestoneId,
			PagingAndSorting sorter, Filtering filter);

	PagedCollectionHolder<List<GenericProject>> getAllBindableProjectForMilestone(Long milestoneId,
			PagingAndSorting sorter, Filtering filter);

	void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId);

	void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId);


	PagedCollectionHolder<List<Milestone>> getAllBindedMilestoneForProject(Long projectId, PagingAndSorting sorter,
			Filtering filter);

	PagedCollectionHolder<List<Milestone>> getAllBindableMilestoneForProject(Long projectId, PagingAndSorting sorter,
			Filtering filter, String type);

}
