/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.service;

import java.util.List;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

public interface BugTrackerManagerService {
	/**
	 * 
	 * @return all bugtrackers the user has read access to
	 */
	List<BugTracker> findAll();

	/**
	 * add a new bugtracker in the database 
	 * @throws DuplicateNameException
	 * 
	 * @param bugTracker
	 */
	void addBugTracker(BugTracker bugTracker);

	/**
	 * 
	 * @param filter
	 * @return sorted list of bugtrackers
	 */
	FilteredCollectionHolder<List<BugTracker>> findSortedBugtrackers(CollectionSorting filter);
	
	/**
	 * 
	 * @return a list of bugtracker kinds
	 */
	Set<String> findBugTrackerKinds();

}
