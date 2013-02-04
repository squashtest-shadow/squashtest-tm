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
package org.squashtest.tm.web.internal.security.authentication;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.security.authentication.AuthenticationSuccessCallback;

/*
 * 
 * Warning : its job partly overlaps the one of BugTrackerContextPersistenceFilter because
 * it creates a BugtrackerContext.
 * 
 * If you really want to know the reason is that when the hook is invoked we're still in the security chain, 
 * not in the regular filter chain. So the context does not exist yet, therefore there is no place to store the 
 * credentials even if the bugtracker auto auth is a success. 
 * 
 *
 */
public class BugTrackerAutoconnectCallback implements AuthenticationSuccessCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerAutoconnectCallback.class);

	private BugTrackersLocalService bugTrackersLocalService;

	private ProjectFinder projectFinder;

	private BugTrackerFinderService bugTrackerFinder;

	public void setProjectFinder(ProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	public void setBugTrackersLocalService(BugTrackersLocalService service) {
		this.bugTrackersLocalService = service;
	}
	
	public void setBugTrackerFinder(BugTrackerFinderService bugTrackerFinder){
		this.bugTrackerFinder = bugTrackerFinder;
	}

	@Override
	public void onSuccess(String username, String password, HttpSession session) {
		if (bugTrackersLocalService == null) {
			LOGGER.info("no bugtracker available (service not ready yet). Skipping autologging.");

		} else {
			BugTrackerContext newContext = new BugTrackerContext();
			List<BugTracker> bugTrackers = findBugTrackers();
			
			for (BugTracker bugTracker : bugTrackers) {
				try {
					LOGGER.debug("try connexion of bug-tracker : {}", bugTracker.getName());
					bugTrackersLocalService.setCredentials(username, password, bugTracker);
					// if success, store the credential in context
					LOGGER.debug("add credentials for bug-tracker : {}", bugTracker.getName());
					AuthenticationCredentials creds = new AuthenticationCredentials(username, password);
					newContext.setCredentials(bugTracker, creds);
					
				} catch (BugTrackerRemoteException ex) {
					LOGGER.info("failed to connect user '" + username + "' to the bugtracker " + bugTracker.getName()
							+ " with the supplied "
							+ "credentials. He will have to connect manually later. Exception thrown is :", ex);
				}
			}
			// store context into session
			session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newContext);
			LOGGER.debug("BugTrackerContext stored to session");
		}

	}

	private List<BugTracker> findBugTrackers() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = IdentifiedUtil.extractIds(readableProjects);
		return bugTrackerFinder.findDistinctBugTrackersForProjects(projectIds);
	}

	

}
