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


public class Version implements Identifiable{
	
	private static final char nonPrintableKey = 0x06;
	private static final String hopefullyUniqueKey= new String(new char[]{nonPrintableKey,nonPrintableKey,nonPrintableKey}); 
	
	/**
	 * Note : this field uses a version having no printable character as an Id. This enforce
	 * slightly the uniqueness of that id regarding real ids you may find in an actual bugtracker.
	 * However it's not totally safe. Refer to code if you need to implement something clever.
	 */
	public static final Version NO_VERSION = new Version(hopefullyUniqueKey,"");
	
	private final String id;
	private final String name;

	public Version(String id, String name) {
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
	
	@Override
	public boolean isEmpty(){
		return this.id.equals(Version.NO_VERSION.getId());
	}
	

}
