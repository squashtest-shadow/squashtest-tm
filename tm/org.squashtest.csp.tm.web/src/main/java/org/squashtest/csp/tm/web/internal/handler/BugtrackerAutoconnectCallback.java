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

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.csp.core.web.servlet.handler.AuthenticationSuccessCallback;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.service.BugTrackersLocalService;


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
public class BugtrackerAutoconnectCallback implements
		AuthenticationSuccessCallback{
	
	private static final Logger logger = LoggerFactory.getLogger(BugtrackerAutoconnectCallback.class);

	private BugTrackersLocalService bugTrackersLocalService;
	

	public void setBugTrackersLocalService(BugTrackersLocalService service){
		this.bugTrackersLocalService=service;
	}
	
	@Override
	public void onSuccess(String username, String password, HttpSession session) {
		if (bugTrackersLocalService==null){
			logger.info("bugtracker autoconnect : no bugtracker available (service not ready yet). Skipping autologging.");
			return ;
		}
		
		BugTrackerStatus status = bugTrackersLocalService.checkBugTrackerStatus();
		
		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED){
			logger.info("bugtracker autoconnect : no bugtracker available (none configured). Skipping autologging.");
			return ;
		}
		
		try{
//			bugTrackersLocalService.setCredentials(username, password);
//			
//			//if success, store the new context in the session
//			createBugTrackerContext(session, username, password);
//			TODO [Feat 1194]
			
		}catch(BugTrackerRemoteException ex){
			logger.info("bugtracker autoconnect : failed to connect user '"+username+"' to the bugtracker with the supplied "+
					    "credentials. He will have to connect manually later. Exception thrown is :", ex
			);
			return;
		}

	}
	
	private void createBugTrackerContext(HttpSession session, String username, String password){
		AuthenticationCredentials creds = new AuthenticationCredentials(username, password);
		BugTrackerContext newContext = new BugTrackerContext();
		newContext.setCredentials(creds);
		
		session.setAttribute(BugTrackerContextPersistenceFilter.BUG_TRACKER_CONTEXT_SESSION_KEY, newContext);
	}

}
