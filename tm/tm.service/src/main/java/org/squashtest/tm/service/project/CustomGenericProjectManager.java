/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.project;

import java.util.List;
import java.util.Map;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.users.Party;

/**
 * @author Gregory Fouquet
 *
 */
public interface CustomGenericProjectManager extends CustomGenericProjectFinder{
	/**
	 * Will find all Projects and Templates to which the user has management access to and return them ordered according to the given params.
	 * 
	 * @param pagingAndSorting the {@link PagingAndSorting} that holds order and paging params
	 * @param filter the filter to apply on the result
	 * @return a {@link PagedCollectionHolder} containing all projects the user has management access to, ordered according to the given params.
	 */
	PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting, Filtering filtering);

	/**
	 * @param project
	 */
	void persist(GenericProject project);

	/**
	 * 
	 * @param templateId
	 */
	void coerceTemplateIntoProject(long templateId);
	
	/************************************************************************************************/
	void deleteProject(long projectId);

	void addNewPermissionToProject(long userId, long projectId, String permission);

	void removeProjectPermission(long userId, long projectId);

	Party findPartyById(long partyId);
	
	// **************************** test automation extension ********************

	
	/**
	 * Will bind the TM project to a TA project. Will persist it if necessary.
	 * 
	 * @param TMprojectId
	 * @param TAproject
	 */
	void bindTestAutomationProject(long tmProjectId, TestAutomationProject taProject);

	void unbindTestAutomationProject(long projectId, long taProjectId);

	// ****************************** bugtracker section ****************************

	/**
	 * Change the Bugtracker the Project is associated-to.<br>
	 * If the Project had no Bugtracker, will add a new association.<br>
	 * If the Project had a already a Bugtracker, it will keep the project-Name information
	 * 
	 * @param projectId
	 * @param newBugtrackerId
	 */
	void changeBugTracker(long projectId, Long newBugtrackerId);
	
	/**
	 * Change the Bugtracker the Project is associated-to.<br>
	 * If the Project had no Bugtracker, will add a new association.<br>
	 * If the Project had a already a Bugtracker, it will keep the project-Name information
	 * 
	 * @param project : the concerned GenericProject
	 * @param bugtracker : the bugtracker to bind the project to
	 */
	void changeBugTracker(GenericProject project, BugTracker bugtracker);

	/**
	 * Will remove the association the Project has to it's Bugtracker.
	 * 
	 * @param projectId
	 */
	void removeBugTracker(long projectId);

	/**
	 * Will change a bugtracker connexion parameter : the name of the bugtracker's project it's associated to.
	 * 
	 * @param projectId
	 *            the concerned project
	 * @param projectBugTrackerName
	 *            the name of the bugtracker's project, the Project is connected to
	 */
	void changeBugTrackerProjectName(long projectId, String projectBugTrackerName);
	
	
	
	// ****************************** wizards management ***********************
	
	/**
	 * enables the given wizard for the given workspace of the given project
	 */
	void enableWizardForWorkspace(long projectId, WorkspaceType workspace, String wizardId);
	
	/**
	 * enables the given wizard for the given workspace of the given project
	 */
	void disableWizardForWorkspace(long projectId, WorkspaceType workspace, String wizardId);
	
	
	/**
	 * Returns the configuration of a given wizard for a given project. Returns an empty map if 
	 * the wizard is not bound to this project.
	 */
	Map<String, String> getWizardConfiguration(long projectId, WorkspaceType workspace, String wizardId);
	
	/**
	 * Applies the given configuration to a wizard for a given project. If the wizard wasn't enabled for this project already, it will be during the process.
	 * 
	 * @param projectId
	 * @param workspace
	 * @param wizardId
	 * @param configuration
	 */
	void setWizardConfiguration(long projectId, WorkspaceType workspace, String wizardId, Map<String, String> configuration);
}
