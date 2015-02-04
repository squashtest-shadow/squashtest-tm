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
package org.squashtest.tm.service.bugtracker;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;


@Transactional(readOnly = true)
public interface BugTrackerFinderService {

	/**
	 * 
	 * @return all bugtrackers the user has read access to
	 */
	List<BugTracker> findAll();
	
	/**
	 * 
	 * @param bugTrackerId
	 * @return the bugTracker of the given id
	 */
	BugTracker findById(long bugTrackerId);
	/**
	 * 
	 * @param filter
	 * @return sorted list of bugtrackers
	 */
	PagedCollectionHolder<List<BugTracker>> findSortedBugtrackers(PagingAndSorting filter);
	
	/**
	 * 
	 * @return a list of bugtracker kinds
	 */
	Set<String> findBugTrackerKinds();
	
	/**
	 * @param bugtrackerId
	 * @return the name of the bugtracker
	 */
	String findBugtrackerName(Long bugtrackerId);
	
	/**
	 * 
	 * @param projectIds
	 * @return a list of distinct BugTrackers concerned by the given projects;
	 */
	List<BugTracker> findDistinctBugTrackersForProjects(List<Long> projectIds);
}
