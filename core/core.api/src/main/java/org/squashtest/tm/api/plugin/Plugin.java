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
package org.squashtest.tm.api.plugin;

import java.util.Map;

/**
 * @author Gregory Fouquet
 *
 */
public interface Plugin {
	
	/**
	 * This plugin persistent, globally unique identifier. A good value would be the osgi service name of this plugin. Should not
	 * return null.
	 * 
	 * @return
	 */
	String getId();
	
	
	/**
	 * Returns the version of this plugin.
	 * 
	 * @return
	 */
	String getVersion();
	
	
	/**
	 * @return the file name (eg a .jar) from which comes this plugin
	 */
	String getFilename();
	
	
	/**
	 * <p>Declares which properties, if any, are used by this plugin for configuration purposes. This map should never be null.</p>
	 * <p>For each entry the key is the property name, and the value is the default value. If there is no default value for a property 
	 * then blank or null is fine.</p>
	 * <p>Depending on what kind of plugin it is (see subinterfaces and/or implementation), these properties will be either global for global plugin, or local if this plugin has 
	 * a specific scope (for instance, a per-project configuration).</p> 
	 * 
	 * @return a Map
	 */
	Map<String, String> getProperties();

}
