/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.report;

/**
 * 
 * A ReportType is a qualifying attribute of a Report. Think of it like an Enum, however we need to extend the number of 
 * possible ReportType in the future so we made it a class.
 * 
 * Implementing a ReportType :
 * ===========================
 * 
 * a ReportType is nothing more than a name. Like for ReportCategory, the used name should be actually a key for
 * a ResourceBundle. Use the setResourceKeyName() method in an init section or in the constructor.
 * 
 *
 * @author bsiri
 *
 */

public abstract class ReportType {
	private String resourceKeyName;

	/**
	 * 
	 * @return the key to look for in a MessageSource
	 */
	public String getResourceKeyName() {
		return resourceKeyName;
	}

	/**
	 * 
	 * @param resourceKeyName the key one assigns to that ReportType.
	 */
	protected void setResourceKeyName(String resourceKeyName) {
		this.resourceKeyName = resourceKeyName;
	}

	
}
