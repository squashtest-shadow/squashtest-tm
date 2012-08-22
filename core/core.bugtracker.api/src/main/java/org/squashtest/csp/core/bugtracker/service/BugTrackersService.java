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
package org.squashtest.csp.core.bugtracker.service;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;


/**
 * Service / Facade to access the bug-trackers from the rest of the application.
 *
 * @author Gregory Fouquet
 *
 */
public interface BugTrackersService {
	/**
	 * Tells if a bug tracker is defined. If not, each bug tracker access method should throw an exception.
	 *
	 * @return
	 */
	boolean isBugTrackerDefined();

	/**
	 * Tell if this service should be given authentication credentials before being able to perform any BT operation.
	 *
	 * @return
	 */
	boolean isCredentialsNeeded();

	/**
	 * Sets the credentials to use for bug tracker authentication. Once set,
	 * {@link BugTrackersService#isCredentialsNeeded()} should no longer be <code>false</code> unless an authentication
	 * error happens at some point.
	 *
	 * @param username
	 * @param password
	 * @param bugTracker the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are invalid
	 */
	void  setCredentials(String username, String password, BugTracker bugTracker);


	/**
	 *
	 * returns a descriptor for the interface in TM
	 * @param bugTracker the concerned BugTracker
	 * @return just what I said
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugtracker);
	

	/**
	 * returns an url like for getBugTrackerUrl. That method will build an url pointing to the issue
	 * hosted on the remote bugtracker.
	 *
	 * @param issueId the ID of an issue that should already exist on the bugtracker (i.e., fed with an ID).
	 * @param bugTracker the concerned BugTracker
	 * @return the url if success, or null if no bugtracker is defined or if malformed.
	 */
	URL getViewIssueUrl(String issueId, BugTracker bugTracker);


	/**
	 * will return the list of the priorities one can set a bug to. The values actually returned depends on the
	 * actual bugtracker at the other end of the line.
	 * 
	 * @param bugTracker the concerned BugTracker
	 * @return the list of Priority.
	 */
	List<Priority> getPriorities(BugTracker bugTracker);




	/**
	 * will return a project, matching by its name
	 *	 * 
	 * @param name of the project
	 * @param bugTracker the concerned BugTracker
	 * @return the project if found, shipped with all known versions, categories and users.
	 * @throws various subclasses of BugTrackerManagerException
	 */
	BTProject findProject(String name, BugTracker bugTracker);


	/**
	 * will return a project, matching by its id. The id we look for is the one from the bugtracker, not from Squash.
	 *
	 * @param id of the project
	 * @param bugTracker the concerned BugTracker
	 * @return the project if found, shipped with all known versions, categories and users.
	 * @throws various subclasses of BugTrackerManagerException
	 */
	BTProject findProjectById(String id, BugTracker bugTracker);

	/**
	 * will send an issue to the bugtracker.
	 *
	 * @param issue a squash Issue
	 * @param bugTracker the concerned BugTracker
	 * @return the newly created issue
	 *
	 */
	BTIssue createIssue(BTIssue issue, BugTracker bugTracker);

	
	
	/**
	 * given a key, returns an issue
	 * 
	 * @param key
	 * @param bugTracker the concerned BugTracker
	 * @return the issue
	 * @throws BugTrackerNotFoundException
	 */
	BTIssue getIssue(String key, BugTracker bugTracker);
	

	/***
	 * This method returns a BTIssue list corresponding to the given Squash Issue List
	 *
	 * @param issueKeyList
	 *            the Squash issue key List (List<String>)
	 * @param bugTracker the concerned BugTracker
	 * @return the corresponding BTIssue List
	 */
	List<BTIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker);

	Set<String> getProviderKinds();

}
