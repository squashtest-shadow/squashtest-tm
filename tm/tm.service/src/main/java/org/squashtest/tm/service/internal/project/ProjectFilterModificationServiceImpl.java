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
package org.squashtest.tm.service.internal.project;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectFilterDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.security.UserContextService;

@Service("squashtest.tm.service.ProjectFilterModificationService")
@Transactional
public class ProjectFilterModificationServiceImpl implements ProjectFilterModificationService {

	@Inject
	private ProjectFilterDao projectFilterDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private ProjectManagerService projectManager;

	@Inject
	private UserContextService userContextService;

	@Override
	public ProjectFilter findProjectFilterByUserLogin() {
		return findOrCreateProjectFilter();
	}

	@Override
	public void saveOrUpdateProjectFilter(List<Long> projectIdList, boolean isActive) {
		// get the filter or create one
		ProjectFilter projectFilter = findOrCreateProjectFilter();
		// set the values
		// convert the project id list in project list
		projectFilter.setProjects(convertLongListToProjectList(projectIdList));
		projectFilter.setActivated(isActive);
		projectFilterDao.persistProjectFilter(projectFilter);
	}

	@Override
	public void updateProjectFilterStatus(boolean status) {
		findProjectFilterByUserLogin().setActivated(status);
	}

	/***
	 * This method checks if a filter already exists, returns it or create a new one
	 * 
	 * @return the current user filter or create a new one
	 */
	private ProjectFilter findOrCreateProjectFilter() {
		String userLogin = userContextService.getUsername();
		ProjectFilter toReturn = projectFilterDao.findProjectFilterByUserLogin(userLogin);
		if (toReturn == null) {
			toReturn = new ProjectFilter();
			toReturn.setProjects(getAllProjects());
			toReturn.setUserLogin(userLogin);
			toReturn.setActivated(false);
			projectFilterDao.persistProjectFilter(toReturn);
		}
		return toReturn;
	}

	/***
	 * The method convert the project id list into project lists
	 * 
	 * @param givenList
	 *            the project id list (List<Long>)
	 * @return the corresponding project list (List<Project>)
	 */
	private List<Project> convertLongListToProjectList(List<Long> givenList) {
		List<Project> projectList = new ArrayList<Project>();
		for (Long projectId : givenList) {
			Project searchedProject = projectDao.findById(projectId);
			if (searchedProject != null) {
				projectList.add(projectDao.findById(projectId));
			}
		}
		return projectList;
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public List<Project> getAllProjects() {
		return projectManager.findAllOrderedByName();
	}

}
