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

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.tm.bugtracker.definition.RemoteCategory;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.bugtracker.definition.RemoteVersion;


/**
 * <p>Connector to a bug tracker.</p>
 * 
 * <p>
 * 	This interface describes what services are expected from a BugTrackerConnector. A BugTrackerConnector is supposed to 
 * manipulate interfaces from package org.squashtest.tm.bugtracker.definition. However, actual implementations should be picked from
 * one of those two domains : 
 * <ul>
 * 		<li>simple domain : org.squashtest.csp.core.bugtracker.domain. This package provides simple entities for simple connectors. This is also the 
 * 							legacy entities for older bugtracker plugins</li>
 * 		<li>extended domain : org.squashtest.tm.bugtracker.advanceddomain. This package provides a second set of entities for more complex models.</li>
 * </ul>
 * 
 * Make your choice between those two implementations and Squash will handle your code accordingly.
 * 
 * </p>

 * @author Gregory Fouquet, bsiri
 *
 */
public interface BugTrackerConnector {
	/**
	 * Authenticates to the bug tracker with the given credentials. If authentication does not fail, it should not be
	 * required again at least for the current thread.
	 *
	 * @param credentials
	 * @throws NullArgumentException 
	 */
	void authenticate(AuthenticationCredentials credentials);

	/**
	 * will check if the current credentials are actually acknowledged by the bugtracker
	 *
	 * @param credentials
	 * @return nothing
	 * @throw {@link BugTrackerNoCredentialsException} if the credentials are invalid
	 * @throw {@link BugTrackerRemoteException} for other network exceptions. 
	 */
	void checkCredentials(AuthenticationCredentials credentials) throws BugTrackerNoCredentialsException, 
																		BugTrackerRemoteException;

	/**
	 * <p>Returns the path to an issue identified by 'issueId'. This suffix corresponds to the URL to 
	 * that issue once the base URL of the bugtracker is removed. Since the base URL of the bugtracker includes
	 * the procotol, authority, hostname and port, most of the time the suffix is simply the path to the issue. 
	 * </p>
	 * 
	 * @param issueId the ID of an issue that is supposed to exist on the bugtracker (ie, with an not empty id)
	 * @return the path to the issue, relative to the host root directory.
	 */
	String makeViewIssueUrlSuffix(String issueId);

	/**
	 * Returns the list of priorities available on the remote bugtracker. As of Squash TM 1.5.1, this method is 
	 * deprecated and the application will not call it anymore (it never did anyway). Throwing UnsupportedOperationException 
	 * or returning whatever is fine.
	 *
	 * @return a list of Priority
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	@Deprecated
	List<RemotePriority> getPriorities() throws BugTrackerRemoteException;


	/**
	 * Returns a BTProject, identified by its name. The remote project name must perfectly match the argument.
	 * 
	 * @param projectName the name of the project.
	 * @return a project properly configured, see the specifics of the implementation of the domain you choosed.
	 * @throws ProjectNotFoundException if the project could not be found
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call
	 */
	RemoteProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * See {@link #findProject(String)}.
	 *
	 * @param projectId the id of the project
	 * @return a project properly configured
	 * @throws ProjectNotFoundException if the project could not be found
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call
	 */
	RemoteProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the available versions of the given project (given its name). 
	 *  
	 * @param projectName is the name of the project
	 * @return the list of the versions
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<RemoteVersion> findVersions(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the available versions of the given project (given its id). 
	 *  
	 * @param projectId is the id of the project
	 * @return the list of the versions
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<RemoteVersion> findVersionsById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;



	/**
	 * Will return the list of the assignable users for the given project and current user (given the project name).
	 * The users must be returned with their Permissions if they have some. 
	 *  
	 * @param projectName is the name of the project
	 * @return the list of the assignable users
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<RemoteUser> findUsers(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;


	/**
	 * Will return the list of the assignable users for the given project and current user (given the project id). 
	 * The users must be returned with their Permissions if they have some.  
	 *  
	 * @param projectID is the name of the project
	 * @return the list of assignable users
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 */
	List<RemoteUser> findUsersById(String projectID) throws ProjectNotFoundException, BugTrackerRemoteException;




	/**
	 * @param issue a squash Issue
	 * @return the corresponding new remote Issue, of which the ID must be set.
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call, including validation exception.
	 *
	 */
	<X extends RemoteIssue> X createIssue(X issue) throws BugTrackerRemoteException;


	/**
	 *
	 * Will return the list of the available categories for the given project (given the project itself). 
	 *  
	 * @param projectName is the name of the project
	 * @return the list of the categories
	 * @throws ProjectNotFoundException if the project doesn't exist
	 * @throws BugTrackerRemoteException when something goes wrong with the remote call.
	 *
	 */
	<X extends RemoteProject> List<RemoteCategory> findCategories(X project) throws ProjectNotFoundException, BugTrackerRemoteException;



	/**
	 * Returns an interface descriptor. 
	 *
	 * @return a BugTrackerInterfaceDescriptor
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();

	
	/**
	 * Returns a single issue. The returned issue must use {@link Version#NO_VERSION} and alike when the version etc aren't
	 * set, instead of null. Furthermore, the {@link BTProject} returned by {@link BTIssue#getProject()} MUST be completely 
	 * configured, as documented in {@link #findProject(String)}. 
	 * 
	 * 
	 * @param key the key of the issue.
	 * @return the issue from the remote bugtracker
	 * @throws BugTrackerNotFoundException when the issue wasn't found
	 * 
	 */
	RemoteIssue findIssue(String key);
	
	
	/***
	 * Returns a list of RemoteIssue, identified by their key. The resulting list doesn't have to be sorted according to the 
	 * input list. 
	 *
	 * @param issueKeyList
	 *            the given squash issue list (List<String>)
	 * @return the corresponding BTIssue list
	 */
	List<RemoteIssue> findIssues(List<String> issueKeyList);


}
