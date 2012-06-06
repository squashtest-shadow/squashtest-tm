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
package org.squashtest.csp.core.bugtracker.service

import java.net.URL;
import java.util.List;

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Permission;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;

//@Component
class StubBugTrackerService implements BugTrackerService {

	@Override
	public boolean isBugTrackerDefined() {
		return false;
	}

	@Override
	public boolean isCredentialsNeeded() {
		return false;
	}

	@Override
	public void setCredentials(String username, String password) {
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return null;
	}

	@Override
	public URL getBugTrackerUrl() {
		return null;
	}

	@Override
	public URL getViewIssueUrl(String issueId) {
		return null;
	}

	@Override
	public List<Priority> getPriorities() {
		return null;
	}

	@Override
	public List<Permission> getPermissions() {
		return null;
	}

	@Override
	public BTProject findProject(String name) {
		return null;
	}

	@Override
	public BTProject findProject(Long id) {
		return null;
	}

	@Override
	public BTIssue createIssue(BTIssue issue) {
		return null;
	}

	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList){
		return null;
	}

	@Override
	public BTProject findProjectById(String id) {
		return null;
	}
	
	@Override
	public String getBugTrackerName() {
		return null;
	}
}
