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

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedIssue;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedProject;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;

public interface AdvancedBugTrackerConnector{

	
	void authenticate(AuthenticationCredentials credentials);
	
	void checkCredentials(AuthenticationCredentials credentials) throws BugTrackerNoCredentialsException, 
	BugTrackerRemoteException;
	
	String makeViewIssueUrlSuffix(String issueId);
	
	AdvancedProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException;
	
	AdvancedProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException;
	
	AdvancedIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException;
	
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();
	
	AdvancedIssue findIssue(String key);
	
	List<AdvancedIssue> findIssues(List<String> issueKeyList);
	
}
