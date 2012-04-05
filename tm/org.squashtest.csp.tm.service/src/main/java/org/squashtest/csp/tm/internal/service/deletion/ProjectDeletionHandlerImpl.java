/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.deletion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.CannotDeleteProjectException;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.ProjectDeletionDao;
import org.squashtest.csp.tm.internal.service.CampaignNodeDeletionHandler;
import org.squashtest.csp.tm.internal.service.NodeDeletionHandler;
import org.squashtest.csp.tm.internal.service.ProjectDeletionHandler;
import org.squashtest.csp.tm.internal.service.RequirementNodeDeletionHandler;
import org.squashtest.csp.tm.internal.service.TestCaseNodeDeletionHandler;

@Component("squashtest.tm.service.deletion.ProjectDeletionHandler")
public class ProjectDeletionHandlerImpl implements ProjectDeletionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDeletionHandlerImpl.class);

	@Inject
	private ProjectDao projectDao;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;
	@Inject
	private TestCaseNodeDeletionHandler testCaseDeletionHandker;
	@Inject
	private RequirementNodeDeletionHandler requirementDeletionHandler;
	@Inject
	private ProjectDeletionDao projectDeletionDao;
	@Inject
	private SessionFactory sessionFactory;

	@Override
	public void deleteProject(long projectId) {
		checkProjectContainsOnlyFolders(projectId);

		doDeleteProject(projectId);

	}

	private void checkProjectContainsOnlyFolders(long projectId) {
		Long nonFolder = projectDao.countNonFoldersInProject(projectId);
		LOGGER.debug("The project #" + projectId + " contains " + nonFolder + " non folder library nodes");
		if (nonFolder > 0L) {
			throw new CannotDeleteProjectException("non-folders are found in the project");
		}

	}

	private void doDeleteProject(long projectId) {
		LOGGER.debug("The project #" + projectId + " is being deleted");
		Project project = projectDao.findById(projectId);

		CampaignLibrary campaignLibrary = project.getCampaignLibrary();
		deleteLibraryContent(campaignLibrary, campaignDeletionHandler);

		TestCaseLibrary testCaseLibrary = project.getTestCaseLibrary();
		deleteLibraryContent(testCaseLibrary, testCaseDeletionHandker);

		RequirementLibrary requirementLibrary = project.getRequirementLibrary();
		deleteLibraryContent(requirementLibrary, requirementDeletionHandler);
		sessionFactory.getCurrentSession().evict(project);
		project = projectDao.findById(projectId);
		removeProjectFromFilters(project);

		projectDeletionDao.removeEntity(project);
	}

	private void removeProjectFromFilters(Project project) {
		List<ProjectFilter> projectFilters = projectDao.findProjectFiltersContainingProject(project.getId());
		for (ProjectFilter projectFilter : projectFilters) {
			projectFilter.removeProject(project);
		}
	}

	private void deleteLibraryContent(Library library, NodeDeletionHandler deletionHandler) {
		Set<LibraryNode> folders = library.getRootContent();
		if (!folders.isEmpty()) {
			List<Long> nodesIds = retrieveNodesids(folders);
			deletionHandler.deleteNodes(nodesIds);
		}

	}

	private List<Long> retrieveNodesids(Set<LibraryNode> folders) {
		List<Long> result = new ArrayList<Long>();
		for (LibraryNode libraryNode : folders) {
			result.add(libraryNode.getId());
		}
		return result;
	}
}
