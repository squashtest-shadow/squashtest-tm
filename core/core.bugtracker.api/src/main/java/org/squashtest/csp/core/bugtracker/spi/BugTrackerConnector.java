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
package org.squashtest.csp.core.bugtracker.spi;

import java.util.List;

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;


/**
 * Connector to a bug tracker.
 *
 * @author Gregory Fouquet
 *
 */
public interface BugTrackerConnector {
	/**
	 * Authenticates to the bug tracker with the given credentials. If authentication does not fail, it should not be
	 * required again at least for the current thread.
	 *
	 * @param credentials
	 */
	void authenticate(AuthenticationCredentials credentials);

	/**
	 * will check if the current credentials are actually acknowledged by the bugtracker
	 *
	 * @param credentials
	 * @return nothing
	 * @throw BugTrackerRemoteException if the credentials are invalids
	 */
	void checkCredentials(AuthenticationCredentials credentials);

	/**
	 * will build the suffix of the url to view that issue on the remote bugtracker.
	 * Must be appended to the base url of the bugtracker to be functionnal.
	 * @param issueId the ID of an issue that is supposed to exist on the bugtracker (ie, with an not empty id)
	 * @return an a suffix for an url if success, null if something failed.
	 */
	String makeViewIssueUrlSuffix(String issueId);

	/**
	 * returns the list of priorities.
	 *
	 * @return a list of Priority
	 */
	List<Priority> getPriorities();


	/**
	 * will return a project based on its name.
	 *
	 * @return a project if found, BugTrackerNotFoundException if not found.
	 */
	BTProject findProject(String projectName);


	/**
	 * will return a project based on its Id (bugtracker-sided Id, not the Squash-sided Id).
	 *
	 * @return a project if found, BugTrackerNotFoundException if not found.
	 */
	BTProject findProjectById(String projectId);


	/**
	 *  will return the list of the available version of the given project
	 * @param projectName is the name of the project
	 * @return the list of the version if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<Version> findVersions(String projectName);


	/**
	 *  will return the list of the available version of the given project
	 * @param projectId is the bugtracker id of the project
	 * @return the list of the version if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<Version> findVersionsById(String projectId);

	/**
	 *  will return the list of the available version of the given project
	 * @param project is project of which we need the versions
	 * @return the list of the version if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<Version> findVersions(BTProject project);


	/*
	 * dev note : finding all the users for JIRA is possible but cumbersome.
	 *
	 * Fecthing the project will give you the PermissionScheme. The PermissionScheme will give you the PermissionMapping, which map each
	 * Permissions to a group of RemoteEntity (being either a RemoteGroup or a RemoteUser).
	 *
	 * So we'll need to fetch the PermissionMapping, iterate over all the Mapping to get the users, and eliminate their
	 * occasional multiple occurence
	 *
	 */

	/**
	 * will return the list of the users working on the given project. They'll be fed with their permissions
	 * for that project.
	 *
	 * @return the list of the users with permissions set if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<User> findUsers(String projectName);


	/**
	 * will return the list of the users working on the given project.They'll be fed with their permissions
	 * for that project.
	 *
	 * @return the list of the users with permissions set if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<User> findUsersById(String projectID);


	/**
	 * will return the list of the users working on the given project. They'll be fed with their permissions
	 * for that project.
	 *
	 * @return the list of the users with permissions set if found, BugTrackerNotFoundException if the project doesn't exist
	 */
	List<User> findUsers(BTProject project);


	/**
	 * will submit an issue to the bugtracker.
	 *
	 * @param issue a squash Issue
	 * @return the newly created issue
	 *
	 */
	BTIssue createIssue(BTIssue issue);


	/**
	 *
	 * will the categories for the given project
	 *
	 * @param project : a Squash project.
	 * @return : the list of the categories bound to that project (bugtracker-side).
	 *
	 */
	List<Category> findCategories(BTProject project);




	/**
	 * will return an object holding the label key corresponding to the various fields
	 * of the issue report interface
	 *
	 * @return a BugTrackerInterfaceDescriptor
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();

	/***
	 * will return a BTIssue List corresponding to a given squash issue key List
	 *
	 * @param issueKeyList
	 *            the given squash issue list (List<String>)
	 * @return the corresponding BTIssue list
	 */
	List<BTIssue> findIssues(List<String> issueKeyList);


}
