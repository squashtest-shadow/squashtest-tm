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
package org.squashtest.tm.domain;

import java.util.EnumSet;

/**
 * Created by jthebault on 30/09/2016.
 */
public enum Workspace {

	HOME("home"),
	REQUIREMENT("requirement"),
	TEST_CASE("test-case"),
	CAMPAIGN("campaign");

	Workspace(String shortName) {
		this.shortName = shortName;
	}

	private final String shortName;

	public String getShortName() {
		return shortName;
	}

	public static Workspace getWorkspaceFromShortName(String shortName){
		EnumSet<Workspace> workspaces = EnumSet.allOf(Workspace.class);
		for (Workspace workspace : workspaces) {
			if (workspace.getShortName().equals(shortName)){
				return workspace;
			}
		}
		return null;
	}
}