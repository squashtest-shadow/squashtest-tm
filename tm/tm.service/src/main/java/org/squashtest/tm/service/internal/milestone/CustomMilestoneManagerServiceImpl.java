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
package org.squashtest.tm.service.internal.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.milestone.ExpandedMilestone;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.milestone.CustomMilestoneManager;

@Service("CustomMilestoneManager")
public class CustomMilestoneManagerServiceImpl implements CustomMilestoneManager {

	@Inject
	private MilestoneDao milestoneDao;

	@Override
	public void addMilestone(Milestone milestone) {
		milestoneDao.checkLabelAvailability(milestone.getLabel());
		milestoneDao.persist(milestone);
	}

	@Override
	public List<Milestone> findAll() {
		return milestoneDao.findAll();
	}

	@Override
	public List<Milestone> findSortedMilestones(PagingAndSorting sorter) {

		return milestoneDao.findSortedMilestones(sorter);
	}

	@Override
	public void removeMilestones(Collection<Long> ids) {
		for (final Long id : ids) {
			// TODO enlever les associations aux divers objects de l'application avant de retirer le jalon
			deleteMilestone(id);
		}
	}

	private void deleteMilestone(final Long id) {
		final Milestone milestone = milestoneDao.findById(id);
		milestoneDao.remove(milestone);
	}

	@Override
	public PagedCollectionHolder<List<Milestone>> filterMilestone(List<ExpandedMilestone> expandedMilestones,
			Filtering filter, PagingAndSorting sorter) {
		List<Milestone> milestones = doFilter(expandedMilestones, filter);
		long count = milestoneDao.countMilestones();
		return new PagingBackedPagedCollectionHolder<List<Milestone>>(sorter, count, milestones);
	}

	private List<Milestone> doFilter(List<ExpandedMilestone> expandedMilestones, Filtering filter) {

		List<Milestone> filtered = new ArrayList<Milestone>();
		
		for (ExpandedMilestone milestone : expandedMilestones){	
			if(isFound(milestone, filter)){
				filtered.add(milestone.getMilestone());
			}
		}

		return filtered;
	}

	private boolean isFound(ExpandedMilestone milestone, Filtering filter) {
		
		String search = filter.getFilter();
		boolean statusFound = milestone.getTranslatedStatus().contains(search);
		
		boolean labelFound = milestone.getMilestone().getLabel().contains(search);
		
		boolean endDateFound = milestone.getTranslatedEndDate().contains(search);
		
		if (statusFound || labelFound || endDateFound){
			return true;
		}
		return false;
	}

	@Override
	public Milestone findById(long milestoneId) {
		return milestoneDao.findById(milestoneId);
	}
}
