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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.security.UserContextService;

@Service("squashtest.tm.service.MilestoneBindingManagerService")
public class CustomMilestoneBindingServiceImpl implements MilestoneBindingManagerService {

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private GenericProjectDao projectDao;

	@Inject
	private UserContextService userContextService;

	@Override
	public List<Milestone> getAllBindableMilestoneForProject(Long projectId) {

		List<Milestone> milestoneBoundToProject = getAllBindedMilestoneForProject(projectId);
		List<Milestone> allMilestones = milestoneDao.findAll();

		allMilestones.removeAll(milestoneBoundToProject);

		return allMilestones;
	}

	@Override
	public void bindMilestonesToProject(List<Long> milestoneIds, Long projectId) {
		GenericProject project = projectDao.findById(projectId);
		List<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);
		project.bindMilestones(milestones);
	}

	@Override
	public void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId) {
		List<GenericProject> projects = projectDao.findAllByIds(projectIds);
		Milestone milestone = milestoneDao.findById(milestoneId);
	milestone.bindProjects(projects);
	}

	@Override
	public List<Milestone> getAllBindedMilestoneForProject(Long projectId) {
		GenericProject project = projectDao.findById(projectId);	
		return project.getMilestones();
	}

	@Override
	public List<GenericProject> getAllBindableProjectForMilestone(Long milestoneId) {
		List<GenericProject> projectBoundToMilestone = getAllBindedProjectForMilestone(milestoneId);
		List<GenericProject> allProjects = projectDao.findAll();
		allProjects.removeAll(projectBoundToMilestone);
		return allProjects;
	}

	@Override
	public List<GenericProject> getAllBindedProjectForMilestone(Long milestoneId) {
		Milestone milestone = milestoneDao.findById(milestoneId);

		return milestone.getProjects();
	}

	@Override
	public PagedCollectionHolder<List<GenericProject>> getAllBindedProjectForMilestone(Long milestoneId,
			PagingAndSorting sorter, Filtering filter) {

		List<GenericProject> projects = getAllBindedProjectForMilestone(milestoneId);
		return getFilteredAndSortedProjects(projects, sorter, filter);
	}

	private List<GenericProject> filterProject(List<GenericProject> projects, Filtering filter) {

		List<GenericProject> filtered = new ArrayList<GenericProject>();

		for (GenericProject project : projects) {
			if (isFound(project, filter)) {
				filtered.add(project);
			}
		}
		return filtered;
	}

	private boolean isFound(GenericProject project, Filtering filter) {
		String search = filter.getFilter();

		boolean nameFound = project.getName().contains(search);
		if (nameFound) {
			return true;
		}
		return false;
	}

	private void sortProject(List<GenericProject> projects, final PagingAndSorting sorter) {

		Collections.sort(projects, new Comparator<GenericProject>() {

			@Override
			public int compare(GenericProject o1, GenericProject o2) {

				return buildProjectComparator(sorter, o1, o2).toComparison();
			}
		});

	}

	private CompareToBuilder buildProjectComparator(final PagingAndSorting sorter, GenericProject o1, GenericProject o2) {

		CompareToBuilder comp = new CompareToBuilder();

		Object first = null;
		Object second = null;

		if ("name".equals(sorter.getSortedAttribute())) {
			first = o1.getName();
			second = o2.getName();
		}
		// FIXME don't forget an "else" branch that will initialize "first" and "second" eventually

		if (sorter.getSortOrder().equals(SortOrder.DESCENDING)) {
			comp.append(first, second);
		} else {
			comp.append(second, first);
		}

		return comp;
	}

	@Override
	public PagedCollectionHolder<List<GenericProject>> getAllBindableProjectForMilestone(Long milestoneId,
			PagingAndSorting sorter, Filtering filter) {
		List<GenericProject> projects = getAllBindableProjectForMilestone(milestoneId);
		return getFilteredAndSortedProjects(projects, sorter, filter);
	}

	@Override
	public void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId) {

		GenericProject project = projectDao.findById(projectId);
		List<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);
		project.unbindMilestones(milestones);
		
	}

	@Override
	public void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId) {

		Milestone milestone = milestoneDao.findById(milestoneId);
		List<GenericProject> projects = projectDao.findAllByIds(projectIds);
		milestone.unbindProjects(projects);

	}

	@Override
	public PagedCollectionHolder<List<Milestone>> getAllBindableMilestoneForProject(Long projectId,
			PagingAndSorting sorter, Filtering filter, String type) {
		List<Milestone> milestones = getAllBindableMilestoneForProject(projectId);
		milestones = filterByType(milestones, type);
		return getFilteredAndSortedMilestones(milestones, sorter, filter);
	}

	@Override
	public List<Milestone> getAllBindableMilestoneForProject(Long projectId, String type) {
		List<Milestone> milestones = getAllBindableMilestoneForProject(projectId);
		return filterByType(milestones, type);
	}
	
	private List<Milestone> filterByType(List<Milestone> milestones, String type) {

		List<Milestone> filtered = null;
		if ("global".equals(type)) {
			// global milestone
			filtered = getGlobalMilestones(milestones);

		} else if ("personal".equals(type)) {
			// milestone created by the user
			filtered = getMilestoneCreatedBySelf(milestones);
		} else {
			// other milestone
			filtered = getOtherMilestones(milestones);
		}
		return filtered;
	}

	private List<Milestone> getOtherMilestones(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<Milestone>();

		for (Milestone milestone : milestones) {
			if (isRestricted(milestone) && !isCreatedBySelf(milestone)) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private List<Milestone> getMilestoneCreatedBySelf(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<Milestone>();
		for (Milestone milestone : milestones) {
			if (isRestricted(milestone) && isCreatedBySelf(milestone)) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private boolean isRestricted(Milestone milestone) {
		boolean isRestricted = false;
		if (milestone.getRange().equals(MilestoneRange.RESTRICTED)) {
			isRestricted = true;
		}
		return isRestricted;
	}

	private boolean isCreatedBySelf(Milestone milestone) {
		boolean isCreatedBySelf = false;
		String myName = userContextService.getUsername();
		if (myName.equals(milestone.getOwner().getLogin())) {
			isCreatedBySelf = true;
		}
		return isCreatedBySelf;
	}

	private List<Milestone> getGlobalMilestones(List<Milestone> milestones) {
		List<Milestone> filtered = new ArrayList<Milestone>();
		for (Milestone milestone : milestones) {
			if (milestone.getRange().equals(MilestoneRange.GLOBAL)) {
				filtered.add(milestone);
			}
		}
		return filtered;
	}

	private void sortMilestone(List<Milestone> milestones, PagingAndSorting sorter) {
		// TODO do the sorting

	}

	private List<Milestone> filterMilestones(List<Milestone> milestones, Filtering filter) {
		// TODO do the filtering
		return milestones;
	}

	@Override
	public PagedCollectionHolder<List<Milestone>> getAllBindedMilestoneForProject(Long projectId,
			PagingAndSorting sorter, Filtering filter) {
		List<Milestone> milestones = getAllBindedMilestoneForProject(projectId);
		return getFilteredAndSortedMilestones(milestones, sorter, filter);
	}

	private PagingBackedPagedCollectionHolder<List<Milestone>> getFilteredAndSortedMilestones(
			List<Milestone> milestones, PagingAndSorting sorter, Filtering filter) {
		long count = milestones.size();
		milestones = filterMilestones(milestones, filter);
		sortMilestone(milestones, sorter);
		return new PagingBackedPagedCollectionHolder<List<Milestone>>(sorter, count, milestones);
	}

	private PagingBackedPagedCollectionHolder<List<GenericProject>> getFilteredAndSortedProjects(
			List<GenericProject> projects, PagingAndSorting sorter, Filtering filter) {
		long count = projects.size();
		projects = filterProject(projects, filter);
		sortProject(projects, sorter);
		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(sorter, count, projects);
	}
}
