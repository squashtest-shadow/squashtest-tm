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
package org.squashtest.csp.core.bugtracker.spi;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ConnectorUtils;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.domain.thirdpartyservers.AuthenticationMode;
import org.squashtest.tm.domain.thirdpartyservers.Credentials;

/**
 * Interface for bugtrackers that implements OLSC (not many of them).
 *
 */
public interface OslcBugTrackerConnector extends BugtrackerConnectorBase{


	RemoteIssue createReportIssueTemplate(String projectName);

	RemoteIssue findIssue(String key);

	URL makeViewIssueUrl(BugTracker bugTracker, String issueId);

	List<RemoteIssue> findIssues(Collection<String> issueKeys);
}
