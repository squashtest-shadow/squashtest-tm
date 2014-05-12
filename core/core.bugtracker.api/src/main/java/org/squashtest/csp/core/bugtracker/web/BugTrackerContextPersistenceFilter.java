/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;

/**
 * This filter is responsible for retrieving the {@link BugTrackerContext}, making it available to the current
 * request's thread and storing it for future use at the end of the request.
 *
 * It should be instantiated using Spring and accessed by the webapp through a DelegatingFilterProxy
 *
 * @author Gregory Fouquet
 *
 */
public final class BugTrackerContextPersistenceFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerContextPersistenceFilter.class);
	/**
	 * Key used do store BT context in http session.
	 */
	public static final String BUG_TRACKER_CONTEXT_SESSION_KEY = "squashtest.bugtracker.BugTrackerContext";

	private BugTrackerContextHolder contextHolder;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	/**
	 * This callback method will try to load a previously existing {@link BugTrackerContext}, expose it to the current
	 * thread through {@link BugTrackerContextHolder} and store it after filter chain processing.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		BugTrackerContext context = loadContext(request);

		try {
			contextHolder.setContext(context);
			chain.doFilter(request, response);
		} finally {
			contextHolder.clearContext();
			storeContextInExistingSession(request, context);
		}
	}

	private void storeContextInExistingSession(ServletRequest request, BugTrackerContext context) {
		HttpSession session = ((HttpServletRequest) request).getSession(false);

		if (session == null) {
			LOGGER.info("Session was invalidated, BugTrackerContext will not be stored");
			return;
		}

		storeContext(session, context);
		LOGGER.debug("BugTrackerContext stored to session");
	}

	private void storeContext(HttpSession session, BugTrackerContext context) {
		session.setAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY, context);
	}

	private BugTrackerContext loadContext(ServletRequest request) {
		LOGGER.debug("Loading BugTrackerContext from HTTP session");

		HttpSession session = ((HttpServletRequest) request).getSession();
		BugTrackerContext context = (BugTrackerContext) session.getAttribute(BUG_TRACKER_CONTEXT_SESSION_KEY);

		if (context == null) {
			LOGGER.info("No BugTrackerContext available, will create it and eagerly store it in session");
			context = new BugTrackerContext();
			storeContext(session, context);
		}

		return context;
	}

	@Override
	public void destroy() {
		// NOOP
	}

	public void setContextHolder(BugTrackerContextHolder contextHolder) {
		this.contextHolder = contextHolder;
	}
}
