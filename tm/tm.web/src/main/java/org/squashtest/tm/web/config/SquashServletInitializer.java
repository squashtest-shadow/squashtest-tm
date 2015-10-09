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
package org.squashtest.tm.web.config;

import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.tm.web.internal.filter.AjaxEmptyResponseFilter;
import org.squashtest.tm.web.internal.listener.HttpSessionLifecycleLogger;
import org.squashtest.tm.web.internal.listener.OpenedEntitiesLifecycleListener;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.Collection;

/**
 * This is required to be able to deploy Squash TM in a servlet container (as opposed to using the embedded container).
 * <p/>
 * It replaces web.xml
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class SquashServletInitializer extends SpringBootServletInitializer {
	@Inject
	private BugTrackerContextHolder bugTrackerContextHolder;

	@Bean
	public BugTrackerContextPersistenceFilter bugTrackerContextPersister() {
		BugTrackerContextPersistenceFilter filter = new BugTrackerContextPersistenceFilter();
		filter.setContextHolder(bugTrackerContextHolder);
		filter.setExcludePatterns("/isSquashAlive");

		return filter;
	}

	@Bean
	public AjaxEmptyResponseFilter ajaxEmptyResponseFilter() {
		return new AjaxEmptyResponseFilter();
	}

	@Bean
	public HttpPutFormContentFilter httpPutFormContentFilter() {
		return new HttpPutFormContentFilter();
	}

	@Bean
	public HttpSessionLifecycleLogger httpSessionLifecycleLogger() {
		return new HttpSessionLifecycleLogger();
	}

	@Bean
	public OpenedEntitiesLifecycleListener openedEntitiesLifecycleListener() {
		return new OpenedEntitiesLifecycleListener();
	}

}
