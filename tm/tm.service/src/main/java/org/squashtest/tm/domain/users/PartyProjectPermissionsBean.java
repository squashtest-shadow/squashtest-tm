/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.domain.users;

import org.squashtest.tm.service.security.acls.PermissionGroup;

/**
 * This class represents a party and an aggregation of permissions (read, write and so on) which can be given to this
 * party and which have a scope of object identities.
 * 
 * This class is used to populate a permission table for given project
 * 
 * @author flaurens
 * 
 */
public class PartyProjectPermissionsBean {

	private final static String TEAM_CLASS = "org.squashtest.tm.domain.users.Team"; 
	private final static String USER_CLASS = "org.squashtest.tm.domain.users.User"; 
	private Party party;
	private PermissionGroup permissionGroup;

	public PartyProjectPermissionsBean(Party party, PermissionGroup permissionGroup) {
		this.party = party;
		this.permissionGroup = permissionGroup;
	}

	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}
	
	public boolean isTeam(){
		boolean result = false;
		if(party.getClass().getName().equals(TEAM_CLASS)){
			result = true;
		}
		return result;
	}
	
	public boolean isUser(){
		boolean result = false;
		if(party.getClass().getName().equals(USER_CLASS)){
			result = true;
		}
		return result;
	}
	
	public PermissionGroup getPermissionGroup() {
		return permissionGroup;
	}

	public void setPermissionGroup(PermissionGroup permissionGroup) {
		this.permissionGroup = permissionGroup;
	}
}
