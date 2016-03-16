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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.exception.BugTrackerNameAlreadyExistsException;

public interface BugTrackerDao extends EntityDao<BugTracker> {
	
	
	/**
	 * 
	 * @return number of all bugtrackers in squash database
	 */
	long countBugTrackers();
	
	/**
	 * 
	 * @param filter
	 * @return a page of bugtrackers according to the filter
	 */
	List<BugTracker> findSortedBugTrackers(PagingAndSorting filter);
	
	/**
	 * checks if there is a Bugtracker of the same name in the database.<br>
	 * If so, raises a {@linkplain BugTrackerNameAlreadyExistsException}
	 * @param name
	 */
	void checkNameAvailability(String name);
	
	/**
	 * 
	 * @param projectIds
	 * @return the list of distinct BugTrackers concerned by the given projects;
	 */
	List<BugTracker> findDistinctBugTrackersForProjects(List<Long> projectIds);
	
	/**
	 * Given its name, returns a bugtracker
	 * 
	 * @param bugtrackerName
	 * @return
	 */
	BugTracker findByName(String bugtrackerName);
}
