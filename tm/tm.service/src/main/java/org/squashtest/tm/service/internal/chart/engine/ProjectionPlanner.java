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

import static org.squashtest.tm.service.internal.chart.engine.QueryBuilder.QueryProfile.MAIN_QUERY;
import static org.squashtest.tm.service.internal.chart.engine.QueryBuilder.QueryProfile.SUBSELECT_QUERY;

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.service.internal.chart.engine.QueryBuilder.QueryProfile;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;

/**
 * <p>
 * 	This class is responsible for adding the "select" and "group by" clauses. See main documentation on
 * 	{@link ChartDataFinder} for more details on how it is done.
 * </p>
 * 
 * <p>
 * 	Depending on the chosen profile, the projection will be :
 * 	<ul>
 * 		<li>MAIN_QUERY : the full projection will be applied (that is, axis then measures)</li>
 * 		<li>SUBSELECT_QUERY : only the measures will be projected - the axis only value is implicit because the outer query will drive it</li>
 * 		<li>SUBWHERE_QUERY : only the axis will be projected - the measure values are implicit because the outer query will drive them  </li>
 * 	</ul>
 * </p>
 * 
 * 
 * @author bsiri
 *
 */
class ProjectionPlanner {

	private DetailedChartQuery definition;

	private ExtendedHibernateQuery<?> query;

	private QuerydslToolbox utils;

	private QueryProfile profile = MAIN_QUERY;

	ProjectionPlanner(DetailedChartQuery definition, ExtendedHibernateQuery<?> query){
		super();
		this.definition = definition;
		this.query = query;
		this.utils = new QuerydslToolbox();
	}

	ProjectionPlanner(DetailedChartQuery definition, ExtendedHibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.definition = definition;
		this.query = query;
		this.utils = utils;
	}

	void setProfile(QueryProfile profile){
		this.profile = profile;
	}

	void modifyQuery(){
		addProjections();
		addGroupBy();
	}

	private void addProjections(){

		List<Expression<?>> selection = new ArrayList<>();

		switch(profile){
		case MAIN_QUERY :
			populateClauses(selection, definition.getAxis());
			populateClauses(selection, definition.getMeasures());
			break;
		case SUBSELECT_QUERY :
			populateClauses(selection, definition.getMeasures());
			break;
		case SUBWHERE_QUERY :
			populateClauses(selection, definition.getAxis());
			break;
		}

		// now stuff the query
		query.select(Projections.tuple(selection.toArray(new Expression[]{}))).distinct();

	}



	private void addGroupBy(){
		// SUBSELECT queries have no group by : this is unneeded because
		// they are correlated subqueries
		if ( profile != SUBSELECT_QUERY){
			List<Expression<?>> groupBy = new ArrayList<>();

			populateClauses(groupBy, definition.getAxis());

			query.groupBy(groupBy.toArray(new Expression[]{}));
		}
	}


	private void populateClauses(List<Expression<?>> toPopulate, List<? extends ColumnPrototypeInstance> columns){
		for (ColumnPrototypeInstance col : columns){

			Expression<?> expr = utils.createAsSelect(col);

			toPopulate.add(expr);
		}

	}


}
