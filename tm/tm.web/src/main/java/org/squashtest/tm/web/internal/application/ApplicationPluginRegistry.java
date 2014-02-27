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

import java.util.Map;

import org.squashtest.tm.api.application.ApplicationPlugin;

/**
 * Interface of an object which should be notified when an application plugin is available or removed.
 * 
 * @author mpagnon
 * 
 */
public interface ApplicationPluginRegistry {
	void registerApplicationPlugin(ApplicationPlugin plugin, Map<?, ?> properties);

	void unregisterApplicationPlugin(ApplicationPlugin plugin, Map<?, ?> properties);
}
