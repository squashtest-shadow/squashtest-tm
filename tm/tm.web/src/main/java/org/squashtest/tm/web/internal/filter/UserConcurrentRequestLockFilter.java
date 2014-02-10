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
package org.squashtest.tm.web.internal.filter;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

/**
 * This filter handles concurrent requests emitted by a single user. If it receive a "write request" (ie POST / PUT /
 * DELETE), it puts on hold any other request.
 *
 * @author Gregory Fouquet, Regis Amoussou
 *
 */
public class UserConcurrentRequestLockFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserConcurrentRequestLockFilter.class);
	/**
	 * Key used do store lock in http session.
	 */
	public static final String READ_WRITE_LOCK_SESSION_KEY = "squashtest.core.ReadWriteLock";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	/**
	 * This callback method will try to load a previously existing {@link ReadWriteLock} from the session and use it to
	 * make this request wait for any existing write request to complete.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		ReadWriteLock lock = loadLock(request);

		try {
			handleRequest(request, response, chain, lock);
		} finally {
			storeLockInExistingSession(request, lock);
		}
	}

	private void handleRequest(ServletRequest request, ServletResponse response, FilterChain chain, ReadWriteLock lock)
			throws IOException, ServletException {
		if (isWriteRequest(request)) {
			handleWriteRequest(request, response, chain, lock);
		} else {
			handleReadRequest(request, response, chain, lock);
		}
	}

	private void handleReadRequest(ServletRequest request, ServletResponse response, FilterChain chain,
			ReadWriteLock lock) throws IOException, ServletException {
		lock.readLock().lock();

		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Read lock acquired by request '{}'", request);
			}

			chain.doFilter(request, response);
		} finally {
			lock.readLock().unlock();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Read lock released by request '{}'", request);
			}
		}

	}

	private void handleWriteRequest(ServletRequest request, ServletResponse response, FilterChain chain,
			ReadWriteLock lock) throws IOException, ServletException {
		lock.writeLock().lock();

		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Write lock acquired by request '{}'", request);
			}

			chain.doFilter(request, response);
		} finally {
			lock.writeLock().unlock();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Write lock released by request '{}'", request);
			}
		}

	}

	private boolean isWriteRequest(ServletRequest request) {
		String httpMethod = ((HttpServletRequest) request).getMethod();

		return "POST".equals(httpMethod) || "DELETE".equals(httpMethod) || "PUT".equals(httpMethod);
	}

	private void storeLockInExistingSession(ServletRequest request, ReadWriteLock context) {
		HttpSession session = ((HttpServletRequest) request).getSession(false);

		if (session == null) {
			LOGGER.debug("Session was invalidated, ReadWriteLock will not be stored");
			return;
		}

		storeLock(session, context);
		LOGGER.trace("ReadWriteLock stored to session");
	}

	private void storeLock(HttpSession session, ReadWriteLock lock) {
		session.setAttribute(READ_WRITE_LOCK_SESSION_KEY, lock);
	}

	private ReadWriteLock loadLock(ServletRequest request) {
		LOGGER.trace("Loading ReadWriteLock from HTTP session");

		HttpSession session = ((HttpServletRequest) request).getSession();
		ReadWriteLock lock = (ReadWriteLock) session.getAttribute(READ_WRITE_LOCK_SESSION_KEY);

		if (lock == null) {
			LOGGER.debug("No ReadWriteLock available, will create it and eagerly store it in session");
			lock = new ReentrantReadWriteLock();
			storeLock(session, lock);
		}

		return lock;
	}

	@Override
	public void destroy() {
		// NOOP

	}
}
