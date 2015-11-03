/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.jpql;

import com.querydsl.jpa.HQLTemplates;

/**
 * These templates, that should be natively used by implementations of {@link ExtendedJPQLQuery}
 * such as {@link ExtendedHibernateQuery}, provide support for the extended functions.
 * 
 * @author bsiri
 *
 */
public class ExtHQLTemplates extends HQLTemplates{

	public static final ExtHQLTemplates INSTANCE = new ExtHQLTemplates();

	protected ExtHQLTemplates() {
		this(DEFAULT_ESCAPE);
	}


	protected ExtHQLTemplates(char escape) {
		super(escape);

		add(ExtAggOps.S_AVG, "s_avg({0})");
		add(ExtAggOps.S_COUNT, "s_count({0})");
		add(ExtAggOps.S_SUM, "s_sum({0})");
		add(ExtAggOps.S_MIN, "s_min({0})");
		add(ExtAggOps.S_MAX, "s_max({0})");

		add(ExtAggOps.GROUP_CONCAT, "group_concat({0})");
		add(ExtAggOps.ORDERED_GROUP_CONCAT, "group_concat({0},{1},{2})");
		add(ExtAggOps.ORDERED_GROUP_CONCAT_DIR, "group_concat({0},{1},{2},{3})");

	}




}
