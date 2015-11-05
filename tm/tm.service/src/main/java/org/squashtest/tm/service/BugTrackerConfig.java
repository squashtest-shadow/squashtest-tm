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
package org.squashtest.tm.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.csp.core.bugtracker.service.BugTrackersServiceImpl;
import org.squashtest.csp.core.bugtracker.service.ThreadLocalBugTrackerContextHolder;

/**
 * Spring configuration for bugtracker connectors subsystem
 *
 * @author gfouquet
 */
@Configuration
public class BugTrackerConfig {
	@Bean(name = "squashtest.core.bugtracker.BugTrackerContextHolder")
	public BugTrackerContextHolder bugTrackerContextHolder() {
		return new ThreadLocalBugTrackerContextHolder();
	}

	@Bean(name = "squashtest.core.bugtracker.BugTrackerConnectorFactory")
	public BugTrackerConnectorFactory bugTrackerConnectorFactory() {
		return new BugTrackerConnectorFactory();
	}

	@Bean
	public BugTrackersService bugTrackersService() {
		BugTrackersServiceImpl service = new BugTrackersServiceImpl();
		service.setBugTrackerConnectorFactory(bugTrackerConnectorFactory());
		service.setContextHolder(bugTrackerContextHolder());

		return service;
	}
}
