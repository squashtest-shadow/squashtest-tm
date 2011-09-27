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
package org.squashtest.csp.tm.web.internal.model.viewmapper;

import java.lang.reflect.Method;

class AttributeRegister {
	String fieldName; // NOSONAR Class is package visible only
	String fieldPath; // NOSONAR Class is package visible only
	int objectIndex; // NOSONAR Class is package visible only
	int attributeIndex; // NOSONAR Class is package visible only
	Method getter; // NOSONAR Class is package visible only
	Method setter; // NOSONAR Class is package visible only
	Class<?> type; // NOSONAR Class is package visible only
}