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
package org.squashtest.tm.web.internal.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.api.export.ExportPlugin;
import org.squashtest.tm.api.workspace.WorkspaceType;

public class ExportPluginManagerImpl implements ExportPluginManager,
		ExportPluginRegistry {
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	private final MultiValueMap pluginsByWorkspace = new MultiValueMap();

	@Override
	public void registerPlugin(ExportPlugin plugin, Map<?, ?> properties) {
		if (plugin != null){
			try{
				lock.writeLock().lock();
				pluginsByWorkspace.put(plugin.getDisplayWorkspace(), plugin);
			}
			finally{
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public void unregisterPlugin(ExportPlugin plugin, Map<?, ?> properties) {
		if (plugin != null){
			try{
				lock.writeLock().lock();
				pluginsByWorkspace.remove(plugin.getDisplayWorkspace(), plugin);
			}
			finally{
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ExportPlugin> findAllByWorkspace(WorkspaceType workspace) {
		try{
			lock.readLock().lock();
			Collection<ExportPlugin> plugins  = pluginsByWorkspace.getCollection(workspace);
			if (plugins == null){
				plugins = Collections.EMPTY_SET;
			}
			return new ArrayList<ExportPlugin>(plugins);
		}
		finally {
			lock.readLock().unlock();
		}
	}

}
