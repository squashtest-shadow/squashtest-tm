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
package org.squashtest.csp.tm.web.internal.handler;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.csp.core.web.servlet.handler.AuthenticationSuccessCallback;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.service.BugTrackersLocalService;
import org.squashtest.csp.tm.service.ProjectFinder;

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

	private static final Logger logger = LoggerFactory.getLogger(BugTrackerAutoconnectCallback.class);

	private BugTrackersLocalService bugTrackersLocalService;
	
	private ProjectFinder projectFinder;
	
	@ServiceReference
	public void setProjectFinder(ProjectFinder projectFinder){
		this.projectFinder = projectFinder;
	}

	public void setBugTrackersLocalService(BugTrackersLocalService service) {
		this.bugTrackersLocalService = service;
	}

	@Override
	public void onSuccess(String username, String password, HttpSession session) {
		if (bugTrackersLocalService == null) {
			logger.info("bugtracker autoconnect : no bugtracker available (service not ready yet). Skipping autologging.");

		} else {
			BugTrackerStatus status;
			BugTrackerContext newContext = new BugTrackerContext();
			
			List<Project> readableProjects = projectFinder.findAllReadable();
			for (Project project : readableProjects) {
				status = bugTrackersLocalService.checkBugTrackerStatus(project);

				if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) {
					logger.info("bugtracker autoconnect : no bugtracker available (none configured). Skipping autologging.");
					
				} else {
					BugTracker bugTracker = project.findBugTracker();
					try {
						
						bugTrackersLocalService.setCredentials(username, password, bugTracker);
						// if success, store the credential in context
						AuthenticationCredentials creds = new AuthenticationCredentials(username, password);
						logger.debug("add credentials for bugtracker : "+bugTracker.getName());
						newContext.setCredentials(bugTracker, creds);

					} catch (BugTrackerRemoteException ex) {
						logger.info("bugtracker autoconnect : failed to connect user '" + username
								+ "' to the bugtracker "+bugTracker.getName()+" with the supplied "
								+ "credentials. He will have to connect manually later. Exception thrown is :", ex);
						
					}
				}
			}
			//store context into session
			session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newContext);
		}

	}
}
