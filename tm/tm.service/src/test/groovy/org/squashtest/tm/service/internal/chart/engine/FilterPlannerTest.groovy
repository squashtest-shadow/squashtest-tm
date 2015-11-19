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
package org.squashtest.tm.service.internal.chart.engine

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.SpecializedEntityType;

import spock.lang.Specification

class FilterPlannerTest extends Specification {

	def "should cheat with the scope filter and arrange the campaign-or-iteration trick"(){

		given :
		ColumnPrototype campProto = new ColumnPrototype(specializedType : spec(EntityType.CAMPAIGN))
		ColumnPrototype itProto = new ColumnPrototype(specializedType : spec(EntityType.ITERATION))

		Filter campFilter = new Filter(column : campProto)
		Filter itFilter = new Filter(column : itProto)

		and :
		DetailedChartQuery q = new DetailedChartQuery(scopeFilters : [campFilter, itFilter])

		FilterPlanner filterPlanner = new FilterPlanner(q, null)

		when :

		Map<ColumnPrototype, Collection<Filter>> origMap = filterPlanner.sortFilters(q.scopeFilters)

		Map<ColumnPrototype, Collection<Filter>> cheatedFilters = filterPlanner.findScopeFilters()

		then :

		// the original map
		origMap[campProto] == [campFilter]
		origMap[itProto] == [itFilter]

		// the cheated map
		cheatedFilters[campProto] as Set == [campFilter, itFilter] as Set
		cheatedFilters[itProto] == null
	}



	def spec(type){
		return new SpecializedEntityType(type, null)
	}

}
