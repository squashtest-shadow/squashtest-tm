/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;

public interface InternalBugtrackerConnector {
	
	/**
	 * Must set the credentials in the connector context for remote authentication challenges 
	 * 
	 * @param credentials
	 */
	void authenticate(AuthenticationCredentials credentials);
	
	/**
	 * Must set the credentials as in {@link #authenticate(AuthenticationCredentials)} and immediately test them 
	 * against the endpoint to check their validity 
	 * 
	 * @param credentials
	 * @throws BugTrackerNoCredentialsException for null arguments
	 * @throws BugTrackerRemoteException for else.
	 */
	void checkCredentials(AuthenticationCredentials credentials) throws BugTrackerNoCredentialsException, 
	BugTrackerRemoteException;
	

	/**
	 * Must return the URL where one can browse the issue.
	 * 
	 * @param issueId
	 * @param bugTracker
	 * @return
	 */
	URL makeViewIssueUrl(BugTracker bugTracker, String issueId) ;
	
	
	/**
	 * Must return a project, given its name, with metadata such as which versions or categories are defined in there. 
	 * 
	 * @param projectName
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	RemoteProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;
	
	
	/**
	 * @see #findProject(String), except that one uses the Id.
	 * @param projectId
	 * @return
	 * @throws ProjectNotFoundException
	 * @throws BugTrackerRemoteException
	 */
	RemoteProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;
	
	/**
	 * Must create an issue on the remote bugtracker, then return the 'persisted' version of it (ie, having its id)
	 * 
	 * @param issue
	 * @return
	 * @throws BugTrackerRemoteException
	 */
	RemoteIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException;
	
	/**
	 * Must return ready-to-fill issue, ie with empty fields and its project configured with as many metadata as possible related to issue creation.
	 * 
	 * @param projectName
	 * @return
	 */
	RemoteIssue createReportIssueTemplate(String projectName);
	
	/**
	 * Returns an {@link BugTrackerInterfaceDescriptor}
	 * 
	 * @return
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();
	
	/**
	 * Retrieve a remote issue
	 * 
	 * @param key
	 * @return
	 */
	RemoteIssue findIssue(String key);
	
	/**
	 * Retrieve many remote issues
	 * 
	 * @param issueKeys
	 * @return
	 */
	List<RemoteIssue> findIssues(Collection<String> issueKeys);
	
	
	/**
	 * Given a remote issue key, will ask the bugtracker to attach the attachments to that issue.
	 * 
	 * @param remoteIssueKey
	 * @param attachments
	 */
	void forwardAttachments(String remoteIssueKey, List<Attachment> attachments);
	

	/**
	 * Executes a delegate command and may return a result. The resulting object must be string-serializable, as it will be jsonified and brought to the 
	 * Squash UI.
	 * 
	 * @param command
	 * @return
	 */
	Object executeDelegateCommand(DelegateCommand command); 
	
}
