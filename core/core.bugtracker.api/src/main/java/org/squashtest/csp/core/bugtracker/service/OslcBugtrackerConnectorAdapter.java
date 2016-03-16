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
import org.squashtest.csp.core.bugtracker.spi.OslcBugTrackerConnector;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;

public class OslcBugtrackerConnectorAdapter implements InternalBugtrackerConnector {

	private OslcBugTrackerConnector connector;

	public OslcBugtrackerConnectorAdapter(OslcBugTrackerConnector connector) {
		super();
		this.connector = connector;
	}

	@Override
	public void authenticate(AuthenticationCredentials credentials) {
		connector.authenticate(credentials);
	}

	@Override
	public void checkCredentials(AuthenticationCredentials credentials)
			throws BugTrackerNoCredentialsException, BugTrackerRemoteException {

		connector.checkCredentials(credentials);
	}

	@Override
	public URL makeViewIssueUrl(BugTracker bugTracker, String issueId) {

		return connector.makeViewIssueUrl(bugTracker, issueId);
	}

	@Override
	public RemoteProject findProject(String projectName) throws ProjectNotFoundException, BugTrackerRemoteException {
		// not used
		return null;
	}

	@Override
	public RemoteProject findProjectById(String projectId) throws ProjectNotFoundException, BugTrackerRemoteException {
		// not used
		return null;
	}

	@Override
	public RemoteIssue createIssue(RemoteIssue issue) throws BugTrackerRemoteException {
		// not used
		return null;
	}

	@Override
	public RemoteIssue createReportIssueTemplate(String projectName) {
		return connector.createReportIssueTemplate(projectName);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return connector.getInterfaceDescriptor();
	}

	@Override
	public RemoteIssue findIssue(String key) {
		return connector.findIssue(key);
	}

	@Override
	public List<RemoteIssue> findIssues(Collection<String> issueKeys) {
		return connector.findIssues(issueKeys);
	}

	@Override
	public void forwardAttachments(String remoteIssueKey, List<Attachment> attachments) {
		// not used

	}

	@Override
	public Object executeDelegateCommand(DelegateCommand command) {
		// not used
		return null;
	}

}
