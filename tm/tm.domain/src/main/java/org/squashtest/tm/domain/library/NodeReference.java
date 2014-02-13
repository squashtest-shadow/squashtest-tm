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
package org.squashtest.tm.domain.library;

/**
 * POJO holding basic informations regarding nodes, when one do not need the full data held in the Session cache.
 * 
 * 
 * @author bsiri
 * 
 */
public class NodeReference {

	private Long id;
	private String name;
	private boolean directory;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isDirectory() {
		return directory;
	}

	public NodeReference(Long id, String name, boolean isDirectory) {
		super();
		this.id = id;
		this.name = name;
		this.directory = isDirectory;
	}

	/**
	 * this one accepts an object array formatted as { Long, String, Boolean }
	 * 
	 * @param rawData
	 */
	public NodeReference(Object[] rawData) {	//NOSONAR the array is not stored as is, and its data are immutable
		super();
		this.id = (Long) rawData[0];
		this.name = (String) rawData[1];
		this.directory = (Boolean) rawData[2];
	}

	@Override // NOSONAR generated code
	public int hashCode() { // NOSONAR generated code
		final int prime = 31;
		int result = 1;
		result = prime * result + (directory ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override // NOSONAR generated code
	public boolean equals(Object obj) { // NOSONAR generated code
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NodeReference other = (NodeReference) obj;
		if (directory != other.directory) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
