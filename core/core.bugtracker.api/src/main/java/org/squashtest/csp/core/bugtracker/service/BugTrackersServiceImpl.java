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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;

/**
 * Basic impementation of {@link BugTrackersService}
 * 
 * @author Gregory Fouquet
 * 
 */
public class BugTrackersServiceImpl implements BugTrackersService {

	private BugTrackerContextHolder contextHolder;

	private BugTrackerConnectorFactory bugTrackerConnectorFactory;

	@Override
	public boolean isCredentialsNeeded(BugTracker bugTracker) {
		return !getBugTrackerContext().hasCredentials(bugTracker);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
			BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
			return connector.getInterfaceDescriptor();
	}
	
	@Override
	public URL getViewIssueUrl(String issueId, BugTracker bugTracker) {
		URL url = null;
		URL baseUrl = bugTracker.getURL();
		try {
			if (baseUrl != null) {
				BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
				String suffix = connector.makeViewIssueUrlSuffix(issueId);
				url = new URL(baseUrl.toString() + suffix);
			} else {
				url = null;
			}
		} catch (MalformedURLException mue) {
			// XXX should throw an exception
			url = null;
		}

		return url;
	}

	private BugTrackerContext getBugTrackerContext() {// TODO BugTrackersContext
		return contextHolder.getContext();
	}

	@Override
	public void setCredentials(String username, String password, BugTracker bugTracker) {

		AuthenticationCredentials credentials = new AuthenticationCredentials(username, password);
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);

		// setcredentials to null first. If the operation succeed then we'll set them in the context.
		getBugTrackerContext().setCredentials(bugTracker, null);

		connector.checkCredentials(credentials);

		getBugTrackerContext().setCredentials(bugTracker, credentials);

	}

	
	@Override
	public RemoteProject findProject(String name, BugTracker bugTracker) {
		return connect(bugTracker).findProject(name);
	}

	@Override
	public RemoteProject findProjectById(String projectId, BugTracker bugTracker) {
		return connect(bugTracker).findProject(projectId);
	}

	private BugTrackerConnector connect(BugTracker bugTracker) {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials(bugTracker));
		return connector;
	}

	@Override
	public BTIssue createIssue(BTIssue issue, BugTracker bugTracker) {
		return connect(bugTracker).createIssue(issue);

	}

	@Override
	public RemoteIssue getIssue(String key, BugTracker bugTracker) {
		return connect(bugTracker).findIssue(key);
	}

	@Override
	public List<RemoteIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker) {

		List<RemoteIssue> issues = connect(bugTracker).findIssues(issueKeyList);

		String bugtrackerName = bugTracker.getName();

		for (RemoteIssue issue : issues) {
			issue.setBugtracker(bugtrackerName);
		}

		return issues;
	}

	@Override
	public Set<String> getProviderKinds() {
		return bugTrackerConnectorFactory.getProviderKinds();

	}

	/**
	 * @param contextHolder the contextHolder to set
	 */
	public void setContextHolder(BugTrackerContextHolder contextHolder) {
		this.contextHolder = contextHolder;
	}

	/**
	 * @param bugTrackerConnectorFactory the bugTrackerConnectorFactory to set
	 */
	public void setBugTrackerConnectorFactory(BugTrackerConnectorFactory bugTrackerConnectorFactory) {
		this.bugTrackerConnectorFactory = bugTrackerConnectorFactory;
	}

}
