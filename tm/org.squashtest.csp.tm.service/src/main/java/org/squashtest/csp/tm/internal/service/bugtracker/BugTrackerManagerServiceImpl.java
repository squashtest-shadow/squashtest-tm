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
package org.squashtest.csp.tm.internal.service.bugtracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerEntity;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.BugTrackerEntityDao;
import org.squashtest.csp.tm.service.BugTrackerManagerService;

@Service("squashtest.tm.service.BugTrackerManagerService")
@Transactional
public class BugTrackerManagerServiceImpl implements BugTrackerManagerService {

	@Inject
	private BugTrackerEntityDao bugTrackerDao;
	
	@Inject
	private BugTrackerConnectorFactory bugTrackerConnectorFactory;

	
	

	@PostFilter("hasPermission(filterObject, 'READ') or  hasRole('ROLE_ADMIN')")
	@Override
	public List<BugTracker> findAll() {
		List<BugTrackerEntity> bugTrackerEntities = bugTrackerDao.findAll();
		return translateBTEntitiesIntoBTs(bugTrackerEntities);
	}

	private List<BugTracker> translateBTEntitiesIntoBTs(List<BugTrackerEntity> bugTrackerEntities) {
		List<BugTracker> bugTrackers = new ArrayList<BugTracker>(bugTrackerEntities.size());
		for (BugTrackerEntity bugTrackerEntity : bugTrackerEntities) {
			BugTracker bugTracker = new BugTracker(bugTrackerEntity.getId(), bugTrackerEntity.getUrl(),
					bugTrackerEntity.getKind(), bugTrackerEntity.getName(), bugTrackerEntity.isIframeFriendly());
			bugTrackers.add(bugTracker);
		}
		return bugTrackers;
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void addBugTracker(BugTracker bugTracker) {
		bugTrackerDao.checkNameAvailability(bugTracker.getName());
		BugTrackerEntity bugTrackerEntity = new BugTrackerEntity(bugTracker.getName(), bugTracker.getKind(),
				bugTracker.getUrl(), bugTracker.isIframeFriendly());
		bugTrackerDao.persist(bugTrackerEntity);

	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<BugTracker>> findSortedBugtrackers(CollectionSorting filter) {
		List<BugTrackerEntity> bugTrackerEntities = bugTrackerDao.findSortedBugTrackerEntities(filter);
		List<BugTracker> bugtrackers = translateBTEntitiesIntoBTs(bugTrackerEntities);
		long count = bugTrackerDao.countBugTrackerEntities();
		return new FilteredCollectionHolder<List<BugTracker>>(count, bugtrackers);
	}

	@Override
	public Set<String> findBugTrackerKinds() {
		return bugTrackerConnectorFactory.getProviderKinds();
	}

}
