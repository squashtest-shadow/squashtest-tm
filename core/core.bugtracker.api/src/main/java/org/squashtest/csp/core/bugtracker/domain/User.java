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
package org.squashtest.csp.core.bugtracker.domain;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * @author bsiri
 *
 */

public class User implements Identifiable{

	private static final char nonPrintableKey = 0x06;
	private static final String hopefullyUniqueKey = new String(new char[] { nonPrintableKey, nonPrintableKey,
			nonPrintableKey });

	/**
	 * Note : this field uses a version having no printable character as an Id. This enforce slightly the uniqueness of
	 * that id regarding real ids you may find in an actual bugtracker. However it's not totally safe. Refer to code if
	 * you need to implement something clever.
	 */
	public static final User NO_USER = new User(hopefullyUniqueKey, "");

	private final String id;
	private final String name;
	private List<Permission> permissions = new LinkedList<Permission>();

	public User(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId(){
		return id;
	}

	@Override
	public String getName(){
		return name;
	}


	public void setPermissions(List<Permission> permissions){
		this.permissions=permissions;
	}

	public List<Permission> getPermissions(){
		return permissions;
	}

	public void addUser(Permission permission){
		this.permissions.add(permission);
	}

	public void addPermission(Permission permission){
		permissions.add(permission);
	}

	public void addAllPermissions(Collection<Permission> permissions){
		this.permissions.addAll(permissions);
	}

	public Permission findPermissionByName(String permissionName){
		for (Permission permission: permissions){
			if (permission.getName().equals(permissionName)){
				return permission;
			}
		}
		return null;
	}

	public Permission findPermissionById(String userId){
		for (Permission permission : permissions){
			if (permission.getId().equals(userId)){
				return permission;
			}
		}
		return null;
	}
	
	
	@Override
	public boolean isEmpty(){
		return this.id.equals(User.NO_USER.getId());
	}

}
