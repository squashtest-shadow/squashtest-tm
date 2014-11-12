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
import org.squashtest.tm.domain.milestone.MilestoneBinding;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.MilestoneBindingDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.milestone.CustomMilestoneBindingManager;

@Service("CustomMilestoneBindingManager")
public class CustomMilestoneBindingServiceImpl implements CustomMilestoneBindingManager {

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private GenericProjectDao projectDao;

	@Inject
	private MilestoneBindingDao milestoneBindingDao;

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
		List<MilestoneBinding> bindings = new ArrayList<MilestoneBinding>();

		for (Milestone milestone : milestones) {
			MilestoneBinding binding = new MilestoneBinding();
			binding.setBoundProject(project);
			binding.setMilestone(milestone);
			bindings.add(binding);
		}
		milestoneBindingDao.persist(bindings);

	}

	@Override
	public void bindProjectsToMilestone(List<Long> projectIds, Long milestoneId) {
		List<GenericProject> projects = projectDao.findAllByIds(projectIds);
		Milestone milestone = milestoneDao.findById(milestoneId);
		List<MilestoneBinding> bindings = new ArrayList<MilestoneBinding>();
		for (GenericProject project : projects) {
			MilestoneBinding binding = new MilestoneBinding();
			binding.setBoundProject(project);
			binding.setMilestone(milestone);
			bindings.add(binding);
		}
		milestoneBindingDao.persist(bindings);
	}

	@Override
	public List<Milestone> getAllBindedMilestoneForProject(Long projectId) {

		List<MilestoneBinding> milestoneBinding = milestoneBindingDao.findAllByProject(projectId);
		List<Milestone> milestoneBoundToProject = new ArrayList<Milestone>();
		for (MilestoneBinding binding : milestoneBinding) {
			milestoneBoundToProject.add(binding.getMilestone());
		}
		return milestoneBoundToProject;
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
		List<MilestoneBinding> milestoneBinding = milestoneBindingDao.findAllByMilestone(milestoneId);
		List<GenericProject> projectBoundToMilestone = new ArrayList<GenericProject>();
		for (MilestoneBinding binding : milestoneBinding) {
			projectBoundToMilestone.add(binding.getBoundProject());
		}
		return projectBoundToMilestone;
	}

	@Override
	public PagedCollectionHolder<List<GenericProject>> getAllBindedProjectForMilestone(Long milestoneId,
			PagingAndSorting sorter, Filtering filter) {

		List<GenericProject> projects = getAllBindedProjectForMilestone(milestoneId);
		long count = projects.size();
		projects = filterProject(projects, filter);
		sortProject(projects, sorter);

		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(sorter, count, projects);
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


	private CompareToBuilder buildProjectComparator(final PagingAndSorting sorter, GenericProject o1,
			GenericProject o2) {

		CompareToBuilder comp = new CompareToBuilder();

		Object first = null;
		Object second = null;

		if ("name".equals(sorter.getSortedAttribute())){
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
		long count = projects.size();
		projects = filterProject(projects, filter);
		sortProject(projects, sorter);
		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(sorter, count, projects);
	}

	@Override
	public void unbindMilestonesFromProject(List<Long> milestoneIds, Long projectId) {
		List<MilestoneBinding> bindings = milestoneBindingDao.findAllByProjectAndMilestones(projectId, milestoneIds);
		milestoneBindingDao.removeAll(bindings);
	}

	@Override
	public void unbindProjectsFromMilestone(List<Long> projectIds, Long milestoneId) {
		List<MilestoneBinding> bindings = milestoneBindingDao.findAllByMilestoneAndProjects(milestoneId, projectIds);
		milestoneBindingDao.removeAll(bindings);
	}

}
