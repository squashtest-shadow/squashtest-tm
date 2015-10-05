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
package org.squashtest.tm.service.internal.chart.engine;

import org.squashtest.tm.domain.chart.EntityType;


/**
 * This enum extends {@link EntityType} and includes table real names and hidden tables that aren't officially
 * disclosed to the end user. Internal usage only.
 * 
 * 
 * @author bsiri
 *
 */
enum InternalEntityType {
	// @formatter:off
	REQUIREMENT,
	REQUIREMENT_VERSION,
	REQUIREMENT_VERSION_COVERAGE,
	TEST_CASE,
	CAMPAIGN,
	ITERATION,
	ITEM_TEST_PLAN,
	EXECUTION,
	ISSUE;
	// @formatter:on

	static InternalEntityType fromDomainType(EntityType domainType){
		InternalEntityType converted;

		switch(domainType){
		case REQUIREMENT : converted = REQUIREMENT; break;
		case REQUIREMENT_VERSION : converted = REQUIREMENT_VERSION; break;
		case TEST_CASE : converted = TEST_CASE; break;
		case CAMPAIGN : converted = CAMPAIGN; break;
		case ITERATION : converted = ITERATION; break;
		case ITEM_TEST_PLAN : converted = ITEM_TEST_PLAN; break;
		case EXECUTION : converted = EXECUTION; break;
		case BUG : converted = ISSUE; break;
		default : throw new IllegalArgumentException("Unimplemented : cannot convert type '"+domainType+"' to a corresponding internal type");
		}

		return converted;
	}
}
