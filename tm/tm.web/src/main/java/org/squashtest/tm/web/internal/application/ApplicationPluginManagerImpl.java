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
package org.squashtest.tm.web.internal.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.application.ApplicationPlugin;
import org.squashtest.tm.web.internal.controller.generic.NavigationButton;

/**
 * @author mpagnon
 * 
 */
public class ApplicationPluginManagerImpl implements ApplicationPluginManager, ApplicationPluginRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPluginManagerImpl.class);

	/**
	 * ApplicationPlugins. This should only be accessed after requesting a read or write {@link #lock}
	 */
	private final Set<ApplicationPlugin> applicationPlugins = new HashSet<ApplicationPlugin>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * @see org.squashtest.tm.web.internal.wizard.ApplicationPluginRegistry#registerApplicationPlugin(org.squashtest.tm.api.wizard.ApplicationPlugin,
	 *      java.util.Map)
	 */
	@Override
	public void registerApplicationPlugin(ApplicationPlugin appPlugin, Map<?, ?> properties) {
		if (appPlugin != null) {
			LOGGER.info("Registering applicationPlugin {} ", appPlugin);
			LOGGER.trace("Registration properties : {}", properties);
			try {
				lock.writeLock().lock();
				applicationPlugins.add(appPlugin);
			} finally {
				lock.writeLock().unlock();
			}

		}
	}

	/**
	 * @see org.squashtest.tm.web.internal.wizard.ApplicationPluginRegistry#unregisterApplicationPlugin(org.squashtest.tm.api.wizard.ApplicationPlugin,
	 *      java.util.Map)
	 */
	@Override
	public void unregisterApplicationPlugin(ApplicationPlugin appPlugin, Map<?, ?> properties) {
		if (appPlugin != null) {
			LOGGER.info("Unregistering application plugin {} ", appPlugin);
			LOGGER.trace("Unregistration properties : {}", properties);
			try {
				lock.writeLock().lock();
				applicationPlugins.remove(appPlugin);
			} finally {
				lock.writeLock().unlock();
			}
		}

	}

	@Override
	public ApplicationPlugin findById(String applicationPluginId) {
		for (ApplicationPlugin appPlugin : findAll()) {
			if (appPlugin.getId().equals(applicationPluginId)) {
				LOGGER.debug("Found application plugin of id = {}", applicationPluginId);
				return appPlugin;
			}
		}
		throw new NoSuchElementException("cannot find ApplicationPlugin with id " + applicationPluginId);
	}

	@Override
	public Collection<ApplicationPlugin> findAll() {
		try {
			lock.readLock().lock();
			LOGGER.debug("Found {} registered application plugins.", applicationPlugins.size());
			return applicationPlugins;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<NavigationButton> getNavigationButtons() {
		Collection<ApplicationPlugin> appPlugins = findAll();

		return menuItems(appPlugins);
	}

	/**
	 * @param appPlugins
	 *            : the collection of appPlugins to get the {@link NavigationButton} from
	 * @return the list of {@link NavigationButton} for the given {@link ApplicationPlugin}s
	 */
	private List<NavigationButton> menuItems(Collection<ApplicationPlugin> appPlugins) {
		List<NavigationButton> res = new ArrayList<NavigationButton>(appPlugins.size());
		
		for (ApplicationPlugin appPlugin : appPlugins) {
			res.add(createMenuItem(appPlugin));
			
		}

		return res;
	}

	/**
	 * @param appPlugin
	 * @return
	 */
	private NavigationButton createMenuItem(ApplicationPlugin appPlugin) {
		NavigationButton item = new NavigationButton();
		item.setId(appPlugin.getId());
		item.setLabel(appPlugin.getNavBarMenu().getLabel());
		item.setTooltip(appPlugin.getNavBarMenu().getTooltip());
		item.setUrl(appPlugin.getNavBarMenu().getUrl());
		item.setImageOffUrl(appPlugin.getNavBarMenu().getImageOffUrl());
		item.setImageOnUrl(appPlugin.getNavBarMenu().getImageOnUrl());
		return item;
	}

}
