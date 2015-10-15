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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.hibernate.HibernateQuery;


/**
 * This class will apply filters on the query, see doc on ChartDataFinder for rules about
 * logical combination on filters.
 * 
 * @author bsiri
 *
 */
class FilterPlanner {

	private DetailedChartQuery definition;

	private QuerydslToolbox utils;

	private HibernateQuery<?> query;

	FilterPlanner(DetailedChartQuery definition, HibernateQuery<?> query){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = new QuerydslToolbox();
	}

	FilterPlanner(DetailedChartQuery definition, HibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = utils;
	}



	/**
	 * <p>A given column may be filtered multiple time. This is represented by the
	 * multiple {@link Filter} that target the same {@link ColumnPrototype}.</p>
	 * 
	 * <p>All filters for a given prototype are ORed together,
	 * then the ORed expressions are ANded together.</p>
	 * 
	 */
	void modifyQuery(){

		Map<ColumnPrototype, Collection<Filter>> sortedFilters = getSortedFilters();

		BooleanBuilder mainBuilder = new BooleanBuilder();

		for (Entry<ColumnPrototype, Collection<Filter>> entry : sortedFilters.entrySet()) {

			BooleanBuilder orBuilder = new BooleanBuilder();

			for (Filter filter : entry.getValue()) {

				if (filter.getOperation() != Operation.NONE){
					BooleanExpression comparison = utils.createAsPredicate(filter);

					orBuilder.or(comparison);
				}
			}

			mainBuilder.and(orBuilder);
		}

		query.where(mainBuilder);
	}

	// this will regroup filters by column prototype. Filters grouped that way will be
	// OR'ed together.
	private Map<ColumnPrototype, Collection<Filter>> getSortedFilters(){

		Map<ColumnPrototype, Collection<Filter>> res = new HashMap<>();

		for (Filter filter : definition.getFilters()){
			ColumnPrototype prototype = filter.getColumn();

			if (! res.containsKey(prototype)){
				res.put(prototype, new ArrayList<Filter>());
			}

			res.get(prototype).add(filter);

		}

		return res;
	}


}
