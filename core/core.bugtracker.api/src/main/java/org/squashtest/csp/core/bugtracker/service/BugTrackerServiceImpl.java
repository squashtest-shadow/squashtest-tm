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
package org.squashtest.csp.core.bugtracker.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;

/**
 * Basic impementation of {@link BugTrackerService}
 *
 * @author Gregory Fouquet
 *
 */
public class BugTrackerServiceImpl implements BugTrackerService {
	@Inject
	private BugTracker bugTracker;

	@Inject
	private BugTrackerContextHolder contextHolder;

	private BugTrackerConnectorFactory bugTrackerConnectorFactory;

	@ServiceReference
	public void setBugTrackerConnectorFactory(BugTrackerConnectorFactory bugTrackerConnectorFactory) {
		this.bugTrackerConnectorFactory = bugTrackerConnectorFactory;
	}

	@Override
	public boolean isBugTrackerDefined() {
		boolean undefined = bugTracker == null || BugTracker.NOT_DEFINED.equals(bugTracker);
		return !undefined;
	}

	@Override
	public boolean isCredentialsNeeded() {
		return !getBugTrackerContext().hasCredentials();
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		if (!isBugTrackerDefined()) {
			// XXX should throw an exception or return an "undefined" impl of BTID
			return null;
		} else {
			BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
			return connector.getInterfaceDescriptor();
		}
	}

	@Override
	public URL getBugTrackerUrl() {
		URL url = null;

		try {
			if (isBugTrackerDefined()) {
				String strUrl = bugTracker.getUrl();
				url = new URL(strUrl);
			}
		} catch (MalformedURLException mue) {
			// XXX should throw an exception
			url = null;
		}

		return url;

	}

	@Override
	public URL getViewIssueUrl(String issueId) {
		URL url = null;
		URL baseUrl = getBugTrackerUrl();
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

	private BugTrackerContext getBugTrackerContext() {
		return contextHolder.getContext();
	}

	@Override
	public void setCredentials(String username, String password) {

		AuthenticationCredentials credentials = new AuthenticationCredentials(username, password);
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);

		// setcredentials to null first. If the operation succeed then we'll set them in the context.
		getBugTrackerContext().setCredentials(null);

		connector.checkCredentials(credentials);

		getBugTrackerContext().setCredentials(credentials);

	}

	@Override
	public List<Priority> getPriorities() {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials());
		return connector.getPriorities();
	}

	
	

	@Override
	public BTProject findProject(String name) {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials());

		return connector.findProject(name);
	}

	@Override
	public BTProject findProjectById(String projectId) {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials());

		return connector.findProject(projectId);
	}

	@Override
	public BTIssue createIssue(BTIssue issue) {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials());

		return connector.createIssue(issue);

	}

	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList) {
		BugTrackerConnector connector = bugTrackerConnectorFactory.createConnector(bugTracker);
		connector.authenticate(getBugTrackerContext().getCredentials());

		return connector.findIssues(issueKeyList);
	}
}
