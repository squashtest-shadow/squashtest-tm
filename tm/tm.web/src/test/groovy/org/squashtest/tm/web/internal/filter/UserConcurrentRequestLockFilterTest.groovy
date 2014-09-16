/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.filter

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.squashtest.tm.web.internal.filter.UserConcurrentRequestLockFilter;

import spock.lang.Specification;

class UserConcurrentRequestLockFilterTest extends Specification {
	UserConcurrentRequestLockFilter filter = new UserConcurrentRequestLockFilter()

	// a mock R/W lock
	ReadWriteLock readWriteLock = Mock()
	Lock readLock = Mock()
	Lock writeLock = Mock()

	// container lifecycle stuff
	HttpServletRequest request = Mock()
	HttpServletResponse response = Mock()
	HttpSession session = Mock()
	FilterChain chain = Mock()

	def setup(){
		mockReadWriteLock()
	}

	def mockReadWriteLock() {
		readWriteLock.readLock() >> readLock
		readWriteLock.writeLock() >> writeLock
	}

	def "filter should delegate to filter chain"() {
		given:
		sessionExistsAndHolds readWriteLock


		when:
		filter.doFilter request, response, chain


		then:
		// chain should not be broken
		1 * chain.doFilter(request, response)
	}

	def sessionExistsAndHolds(def context) {
		request.getSession() >> session
		request.getSession(_) >> session
		session.getAttribute(UserConcurrentRequestLockFilter.READ_WRITE_LOCK_SESSION_KEY) >> context
	}

	def "should store lock to session after filter chain processing"() {
		given:
		sessionExistsAndHolds readWriteLock

		when:
		filter.doFilter request, response, chain

		then:
		// lock persisted to session
		1 * session.setAttribute(UserConcurrentRequestLockFilter.READ_WRITE_LOCK_SESSION_KEY, readWriteLock)

	}

	def "should not store lock back to session when session has been invalidated"() {
		given: "session initially holds lock"
		request.getSession() >> session
		session.getAttribute(_) >> readWriteLock

		and: "session invalidated at some point"
		request.getSession(false) >> null

		when:
		filter.doFilter request, response, chain

		then:
		// no lock persistence
		0 * session.setAttribute(UserConcurrentRequestLockFilter.READ_WRITE_LOCK_SESSION_KEY, readWriteLock)

	}

	def sessionHoldsNoLock() {
		request.getSession() >> session
		request.getSession(_) >> session
	}

	def "should eagerly store lock to session when no previously available"() {
		given:
		sessionHoldsNoLock()

		when:
		filter.doFilter request, response, chain


		then:
		2 * session.setAttribute(UserConcurrentRequestLockFilter.READ_WRITE_LOCK_SESSION_KEY, !null)
	}

	def "should try a read lock on a http get request"() {
		given:
		request.getMethod() >> "GET"
		sessionExistsAndHolds readWriteLock

		when :
		filter.doFilter request, response, chain

		then :
		1 * readLock.lock()
		1 * readLock.unlock()

	}

	def "should try a Write lock on a http post request"() {
		given:
		request.getMethod() >> "POST"
		sessionExistsAndHolds readWriteLock

		when :
		filter.doFilter request, response, chain

		then :
		1 * writeLock.lock()
		1 * writeLock.unlock()
	}

	def "should try a Write lock on a http put request"() {
		given:
		request.getMethod() >> "PUT"
		sessionExistsAndHolds readWriteLock

		when :
		filter.doFilter request, response, chain

		then :
		1 * writeLock.lock()
		1 * writeLock.unlock()
	}

	def "should try a Write lock on a http delete request"() {
		given:
		request.getMethod() >> "DELETE"
		sessionExistsAndHolds readWriteLock

		when :
		filter.doFilter request, response, chain

		then :
		1 * writeLock.lock()
		1 * writeLock.unlock()
	}
}
