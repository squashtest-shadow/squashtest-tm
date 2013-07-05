/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.viewmapper;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultDatatableMapper<KEY> implements DatatableMapper<KEY> {

	private Map<KEY, AttributeRegister<KEY>> attributesMap;

	public DefaultDatatableMapper() {
		super();
		attributesMap = new HashMap<KEY, AttributeRegister<KEY>>();
	}

	public DefaultDatatableMapper(int initialCapacity) {
		super();
		attributesMap = new HashMap<KEY, AttributeRegister<KEY>>(initialCapacity);
	}

	@Override
	public DatatableMapper<KEY> mapAttribute(Class<?> ownerType, String attributeName, Class<?> attributeType, KEY key) {
		AttributeRegister<KEY> register = new AttributeRegister<KEY>(ownerType, attributeName);
		attributesMap.put(key, register);
		return this;
	}

	@Override
	public String attrAt(KEY key) {
		AttributeRegister<KEY> reg = attributesMap.get(key);
		if (reg != null) {
			return reg.getAttributeName();
		} else {
			throw new NoSuchElementException("key '"+key+"' is not mapped");
		}
	}

	@Override
	public String pathAt(KEY key) {
		AttributeRegister<KEY> reg = attributesMap.get(key);
		if (reg != null) {
			return reg.getAttributePath();
		} else {
			throw new NoSuchElementException("key '"+key+"' is not mapped");
		}
	}

	/* ******************** utilities ************************** */

	private static final class AttributeRegister<KEY> {

		private String attributeName;

		private Class<?> ownerType;

		public AttributeRegister(Class<?> ownerType, String attributeName) {
			super();
			this.attributeName = attributeName;
			this.ownerType = ownerType;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public String getAttributePath() {
			return ownerType.getSimpleName() + "." + attributeName;
		}
	}

}
