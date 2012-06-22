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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.web.servlet.handler.AuthenticationSuccessCallback;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.service.BugTrackerLocalService;


@Component
public class BugtrackerAutoconnectCallback implements
		AuthenticationSuccessCallback{
	
	private static final Logger logger = LoggerFactory.getLogger(BugtrackerAutoconnectCallback.class);

	private BugTrackerLocalService service;
	
	
	@ServiceReference
	public void setBugTrackerLocalService(BugTrackerLocalService service){
		this.service=service;
	}
	
	@Override
	public void onSuccess(String username, String password) {
		if (service==null){
			logger.info("bugtracker autoconnect : no bugtracker available (service not ready yet). Skipping autologging.");
			return ;
		}
		
		BugTrackerStatus status = service.checkBugTrackerStatus();
		
		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED){
			logger.info("bugtracker autoconnect : no bugtracker available (none configured). Skipping autologging.");
			return ;
		}
		
		try{
			service.setCredentials(username, password);
		}catch(BugTrackerRemoteException ex){
			logger.info("bugtracker autoconnect : failed to connect user '"+username+"' to the bugtracker with the supplied "+
					    "credentials. He will have to connect manually later. Exception thrown is :", ex
			);
			return;
		}

	}

}
