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
package org.squashtest.tm.domain.users.preferences;

/**
 * Convenient enumeration to declare all user prefs in SquashTM core
 * Created by jthebault on 30/03/2016.
 */
public enum CorePartyPreference {

	//value should be "dashboard" or "message"
	HOME_WORKSPACE_CONTENT("squash.core.home.content"),
	//value should be a CustomReportLibraryNode id
	FAVORITE_DASHBOARD("squash.core.favorite.dashboard");

	private String preferenceKey;

	CorePartyPreference(String preferenceKey) {
		this.preferenceKey = preferenceKey;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

}
