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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

import squashtm.testautomation.domain.TestAutomationProject;

public interface ProjectDao extends EntityDao<Project> {
	List<Project> findAllOrderedByName();

	List<Project> findSortedProjects(CollectionSorting filter);

	long countProjects();

	long countNonFoldersInProject(long projectId);

	List<ProjectFilter> findProjectFiltersContainingProject(Long id);

	// ************************* test automation section **********************

	List<TestAutomationProject> findBoundTestAutomationProjects(long id);
}
