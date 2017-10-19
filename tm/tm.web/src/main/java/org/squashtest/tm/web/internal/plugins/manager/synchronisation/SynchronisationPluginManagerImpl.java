/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.plugins.manager.synchronisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.wizard.SynchronisationPlugin;
import org.squashtest.tm.api.wizard.WorkspaceWizard;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;

@Component
public class SynchronisationPluginManagerImpl implements SynchronisationPluginManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronisationPluginManagerImpl.class);

	@Autowired(required = false)
	private Collection<SynchronisationPlugin> plugins = Collections.emptyList();

	@Inject
	@Named("squashtest.tm.service.ThreadPoolTaskScheduler")
	private TaskScheduler taskScheduler;

	@PostConstruct
	public void registerSynchronisationPlugin() {
		for (SynchronisationPlugin plugin : plugins) {
			LOGGER.info("Registering workspace wizard {} for workspace {}", plugin, plugin.getName());
			taskScheduler.scheduleAtFixedRate(plugin.performSynchronisation(), 5000L);
		}
	}

	@Override
	public Collection<SynchronisationPlugin> findAll() {
		return plugins;
	}
}
