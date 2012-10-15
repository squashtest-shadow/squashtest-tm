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

import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;


public interface CustomProjectModificationService extends ProjectFinder {
	
	void deleteProject(long projectId);

	AdministrableProject findAdministrableProjectById(long projectId);

	void addNewPermissionToProject(long userId, long projectId, String permission);

	void removeProjectPermission(long userId, long projectId);

	List<UserProjectPermissionsBean> findUserPermissionsBeansByProject(long projectId);

	List<PermissionGroup> findAllPossiblePermission();

	List<User> findUserWithoutPermissionByProject(long projectId);

	User findUserByLogin(String userLogin);

	
	//**************************** test automation extension ********************
	
	/**
	 * Returns a TestAutomationServer instance. Either it is a persisted instance that 
	 * the  tm project was bound to lastly (through a ta project), either it will be 
	 * the default server configuration.
	 * 
	 */
	TestAutomationServer getLastBoundServerOrDefault(long projectId);
	
	/**
	 * Will bind the TM project to a TA project. Will persist it if necessary.
	 *  
	 * @param TMprojectId
	 * @param TAproject
	 */
	void bindTestAutomationProject(long TMprojectId, TestAutomationProject TAproject);
	
	
	List<TestAutomationProject> findBoundTestAutomationProjects(long projectId);
	
	void unbindTestAutomationProject(long TMprojectId, long TAProjectId);

	//******************************  bugtracker section ****************************
	
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
	 * Will remove the association the Project has to it's Bugtracker.
	 * 
	 * @param projectId
	 */
	void removeBugTracker(long projectId);
	
	/**
	 * Will change a bugtracker connexion parameter : the name of the bugtracker's project it's associated to.
	 * 
	 * @param projectId the concerned project
	 * @param projectBugTrackerName the name of the bugtracker's project, the Project is connected to
	 */
	void changeBugTrackerProjectName(long projectId, String projectBugTrackerName);
}
