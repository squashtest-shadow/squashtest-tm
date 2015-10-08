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

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateOperation;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

/**
 * <p>
 * 	This class is responsible for adding the "select" and "group by" clauses. See main documentation on
 * 	{@link ChartDataFinder} for more details on how it is done.
 * </p>
 * 
 * 
 * @author bsiri
 *
 */
class ProjectionPlanner {

	private DetailedChartDefinition definition;

	private HibernateQuery<?> query;

	private QuerydslUtils utils = QuerydslUtils.INSTANCE;

	ProjectionPlanner(DetailedChartDefinition definition, HibernateQuery<?> query){
		this.definition = definition;
		this.query = query;
	}

	void modifyQuery(){
		addProjections();
		addGroupBy();
	}

	private void addProjections(){

		List<Expression<?>> selection = new ArrayList<>();

		// first the axis, second the measures
		populateExpressions(selection, definition.getAxis());
		populateExpressions(selection, definition.getMeasures());

		// now stuff the query
		query.select(Projections.tuple(selection.toArray(new Expression[]{})));

	}



	private void addGroupBy(){

		List<Expression<?>> groupBy = new ArrayList<>();

		populateExpressions(groupBy, definition.getAxis());

		query.groupBy(groupBy.toArray(new Expression[]{}));

	}


	private void populateExpressions(List<Expression<?>> toPopulate, List<? extends ColumnPrototypeInstance> columns){
		for (ColumnPrototypeInstance col : columns){

			DataType datatype = col.getDataType();
			Operation operation = col.getOperation();

			Expression columnExpr =  utils.makePath(col.getColumn());

			if (col.getOperation() != Operation.NONE){
				columnExpr = utils.addOperation(datatype, operation, columnExpr);
			}

			toPopulate.add(columnExpr);
		}

	}


}
