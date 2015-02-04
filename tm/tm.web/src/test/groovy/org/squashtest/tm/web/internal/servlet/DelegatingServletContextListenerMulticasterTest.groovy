/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.servlet

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.web.context.WebApplicationContext
import org.squashtest.tm.web.internal.listener.DelegatingServletContextListenerMulticaster

import spock.lang.Specification

class DelegatingServletContextListenerMulticasterTest extends Specification {
	DelegatingServletContextListenerMulticaster multicaster = new DelegatingServletContextListenerMulticaster()

	def "should delegate initialization event to servlet context listeners"() {
		given: "a delegate listener"
		ServletContextListener delegate = Mock()

		and: "a bean factory containing the delegate"
		ListableBeanFactory beanFactory = Mock()
		beanFactory.getBeansOfType(ServletContextListener) >> ["toto" : delegate]

		and: "a servlet context containing the bean factory"
		ServletContext servletContext = Mock()
		servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) >> beanFactory

		and:
		ServletContextEvent event = Mock()
		event.getServletContext() >> servletContext

		when:
		multicaster.contextInitialized event

		then:
		1 * delegate.contextInitialized(event)
	}
	def "should delegate destruction event to servlet context listeners"() {
		given: "a delegate listener"
		ServletContextListener delegate = Mock()

		and: "a bean factory containing the delegate"
		ListableBeanFactory beanFactory = Mock()
		beanFactory.getBeansOfType(ServletContextListener) >> ["toto" : delegate]

		and: "a servlet context containing the bean factory"
		ServletContext servletContext = Mock()
		servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) >> beanFactory

		and:
		ServletContextEvent event = Mock()
		event.getServletContext() >> servletContext

		when:
		multicaster.contextDestroyed event

		then:
		1 * delegate.contextDestroyed(event)
	}
}
