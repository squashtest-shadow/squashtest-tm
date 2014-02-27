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

package org.squashtest.tm.search.bridge;

import org.hibernate.search.bridge.StringBridge;
import org.squashtest.tm.domain.Level;

/**
 * StringBridge which indexes a {@link Level} enum using the pattern "<level>-<name>"
 * 
 * TODO I wish it could be a AppliedOnTypeAwareBridge + TwoWayStringBridge but it dont seem to work due to
 * "ignoreBridge" directives on queries. HAve to investigate on why this ignoreBridge.
 * 
 * @author Gregory Fouquet
 * 
 */
public class LevelEnumBridge implements StringBridge {
//	@SuppressWarnings("rawtypes")
//	private Class<? extends Enum> targetClass;

	/**
	 * 
	 */
	public LevelEnumBridge() {
		super();
	}

	@Override
	public String objectToString(Object value) {
		return ((Level) value).getLevel() + "-" + ((Enum<?>) value).name();
	}

	/**
	 * @see org.hibernate.search.bridge.TwoWayStringBridge#stringToObject(java.lang.String)
	 */
	// @SuppressWarnings("unchecked")
	// @Override
	// public Object stringToObject(String str) {
	// String name = str.substring(str.indexOf('-') + 1);
	//
	// return Enum.valueOf(targetClass, name);
	// }

	/**
	 * @see org.hibernate.search.bridge.AppliedOnTypeAwareBridge#setAppliedOnType(java.lang.Class)
	 */
	// @SuppressWarnings("unchecked")
	// @Override
	// public void setAppliedOnType(Class<?> returnType) {
	// targetClass = (Class<? extends Enum<?>>) returnType;
	//
	// }
}
