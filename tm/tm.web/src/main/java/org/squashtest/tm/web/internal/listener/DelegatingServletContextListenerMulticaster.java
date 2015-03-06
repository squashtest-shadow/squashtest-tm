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
package org.squashtest.tm.web.internal.listener;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * This {@link ServletContextListener} delegates events to any bean declared in Spring app context which implements
 * {@link ServletContextListener}.
 * 
 * This listener should be declared AFTER the Spring bootstrapper (ContextLoaderListener).
 * 
 * @author Gregory Fouquet
 * 
 */
public class DelegatingServletContextListenerMulticaster implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingServletContextListenerMulticaster.class);

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		Map<String, ServletContextListener> servletContextListeners = findDelegatesByBeanName(servletContext);

		delegateContextDestroyed(event, servletContextListeners);

	}

	private void delegateContextDestroyed(ServletContextEvent event,
			Map<String, ServletContextListener> servletContextListeners) {
		for (Entry<String, ServletContextListener> entry : servletContextListeners.entrySet()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Delegating destruction event to bean {} of type {}", entry.getKey(), entry.getValue());
			}

			entry.getValue().contextDestroyed(event);
		}

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		Map<String, ServletContextListener> servletContextListeners = findDelegatesByBeanName(servletContext);

		delegateContextInitialized(event, servletContextListeners);
	}

	private void delegateContextInitialized(ServletContextEvent event,
			Map<String, ServletContextListener> servletContextListeners) {
		for (Entry<String, ServletContextListener> entry : servletContextListeners.entrySet()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Delegating initialization event to bean {} of type {}", entry.getKey(), entry.getValue());
			}

			entry.getValue().contextInitialized(event);
		}
	}

	private Map<String, ServletContextListener> findDelegatesByBeanName(ServletContext servletContext) {
		ListableBeanFactory beanFactory = (ListableBeanFactory) servletContext
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		Map<String, ServletContextListener> servletContextListeners = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				beanFactory, ServletContextListener.class);
		return servletContextListeners;
	}
}
