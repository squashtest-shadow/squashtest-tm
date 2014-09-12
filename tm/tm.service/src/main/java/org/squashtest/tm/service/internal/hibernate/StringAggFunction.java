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
package org.squashtest.tm.service.internal.hibernate;

import java.util.List;

import org.hibernate.type.Type;

/**
 * Equivalent of GroupConcatFunction for Postgresql
 * 
 */
public final class StringAggFunction extends GroupConcatFunction {

	public StringAggFunction(String name) {
		super(name);
	}

	public StringAggFunction(String name, Type registeredType) {
		super(name, registeredType);
	}

	@Override
	public String createSqlQuery(List<?> arguments, String direction, String separator) {
		return "string_agg( cast(" + arguments.get(0) + " as text),'" + separator + "' order by " + arguments.get(2)
				+ " " + direction + ")";
	}
}
