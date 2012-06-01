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
package org.squashtest.csp.tm.web.internal.listner;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.squashtest.csp.tm.web.internal.interceptor.ObjectViewsInterceptor;
import org.squashtest.csp.tm.web.internal.interceptor.OpenedEntities;

public class HttpSessionListnerImpl implements HttpSessionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionListnerImpl.class);

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		LOGGER.debug("Session Closed");
		ServletContext context = arg0.getSession().getServletContext();
		HttpSession session = arg0.getSession();
		SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
		String login = null ;
		if(securityContext != null){
			Authentication authentication = securityContext.getAuthentication();
			if(authentication != null ){
				login = authentication.getName();
			}
		}
		
		if (login != null) {
			
			for (String managedEntityKey : OpenedEntities.MANAGED_ENTITIES_LIST) {
				removeUserFromViewers(managedEntityKey, login, context);
			}
		}
	}

	private void removeUserFromViewers(String managedEntityKey, String login, ServletContext context) {
		OpenedEntities openedEntities = (OpenedEntities) context.getAttribute(managedEntityKey);
		if(openedEntities != null){openedEntities.removeViewer(login);}
	}

}
