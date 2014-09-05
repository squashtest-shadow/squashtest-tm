/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.library.structures;

/**
 * classes extending that one should preferably be comparable only on a subset of their attributes. More formally,
 * 
 * a.equals(b) regardless of the result of (a.hashCode() == b.hashCode())
 * 
 * @author bsiri
 * 
 * @param <KEY_TYPE>
 *            the type of the key used for comparison.
 */
public abstract class NodeData<KEY_TYPE> {

	private final KEY_TYPE key;

	public NodeData(KEY_TYPE key) {
		this.key = key;
	}

	public final KEY_TYPE getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override//NOSONAR code generation, assumed to be safe
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NodeData<?> other = (NodeData<?>) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}// GENERATED:END
	

	public final boolean equals(NodeData<KEY_TYPE> otherData) {	//NOSONAR : there COULD be some bugs but for now it's ok
		if (otherData == null) {
			return false;
		}
		if ((getKey() == null) && (otherData.getKey() != null)) {
			return false;
		}
		if ((getKey() == null) && (otherData.getKey()) == null) {
			return true;
		}
		return getKey().equals(otherData.getKey());
	}

	public abstract void updateWith(NodeData<KEY_TYPE> newData);

}
