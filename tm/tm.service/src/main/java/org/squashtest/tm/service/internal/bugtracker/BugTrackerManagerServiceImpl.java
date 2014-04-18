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
package org.squashtest.tm.service.internal.bugtracker;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;

@Service("squashtest.tm.service.BugTrackerManagerService")
public class BugTrackerManagerServiceImpl implements BugTrackerManagerService {

	@Inject
	private BugTrackerDao bugTrackerDao;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void addBugTracker(BugTracker bugTracker) {
		bugTrackerDao.checkNameAvailability(bugTracker.getName());
		bugTrackerDao.persist(bugTracker);

	}
	@Inject
	private BugTrackersLocalService bugtrackersLocalService;

	@PostFilter("hasPermission(filterObject, 'READ') or  hasRole('ROLE_ADMIN')")
	@Override
	public List<BugTracker> findAll() {
		return bugTrackerDao.findAll();

	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<BugTracker>> findSortedBugtrackers(PagingAndSorting filter) {
		List<BugTracker> bugTrackers = bugTrackerDao.findSortedBugTrackers(filter);
		long count = bugTrackerDao.countBugTrackers();
		return new PagingBackedPagedCollectionHolder<List<BugTracker>>(filter, count, bugTrackers);
	}

	@Override
	public Set<String> findBugTrackerKinds() {
		return bugtrackersLocalService.getProviderKinds();
	}

	@Override
	public String findBugtrackerName(Long bugtrackerId) {
		return bugTrackerDao.findById(bugtrackerId).getName();
	}

	@Override
	public BugTracker getBugtrackerFromKeyIfExists(String bugTrackerName){
		return bugTrackerDao.findByName(bugTrackerName);
	}

	@Override
	public BugTracker findById(long bugTrackerId) {
		return bugTrackerDao.findById(bugTrackerId);
	}

	@Override
	public List<BugTracker> findDistinctBugTrackersForProjects(List<Long> projectIds) {
		return bugTrackerDao.findDistinctBugTrackersForProjects(projectIds);
	}
}