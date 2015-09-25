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
package org.squashtest.tm.service.internal.charts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.EntityType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;


/**
 * <p>This is the class that will find the data matching the criteria supplied as a {@link ChartDefinition}.</p>
 * 
 * <p>A ChartDefinition defines what you need to formulate the query :
 * 
 * <ul>
 * 	<li>What data you want to find (the {@link MeasureColumn}s)</li>
 * 	<li>On what basis you want them (the {@link AxisColumn}s)</li>
 * 	<li>How specific you need the data to be (the {@link Filter}s)</li>
 * </ul>
 * </p>
 * 
 * <p>Here we explain how these roles will be used in a query :</p>
 * 
 * <h5>Filters</h5>
 * 
 * <p>
 * 	In a query they naturally fit the role of a where clause. However a regular where clause is a simple case : sometimes you would have
 * 	it within a Having clause, and in other times it could be a whole subquery.
 * </p>
 * 
 * <h5>AxisColumns</h5>
 * 
 * <p>
 *	These columns will be grouped on (in the group by clause). They also appear in the select clause and should of course not
 *	be subject to any aggregation. In the select clause, they will appear first, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 * 
 * <h5>MeasureColumn</h5>
 * 
 * <p>
 * 	These columns appear in the select clause and will be subject to an aggregation method. The aggregation is specified in the MeasureColumn.
 * 	They appear in the select clause, after the axis columns, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 * <p>
 * 	Most of the time no specific attribute will be specified : in this case the measure column defaults to the id of the observed entity.
 * 	For instance consider a measure which aggregation is 'sum' (count). If the user is interested to know how many test cases match the given filters,
 * 	the aggregation should be made on the test case ids. However if the user picked something more specific - like the test case labels -, the semantics
 * 	becomes how many different labels exist within the test cases that match the filter. This, of course, is a problem for the tool that will design
 * 	the ChartDefinition : the current class will just create an process the query.
 * </p>
 * 
 * 
 * @author bsiri
 *
 */
@Component
public class ChartDataFinder {

	@Inject
	private SessionFactory sessionFactory;


	Object[][] findData(ChartDefinition definition){



		return null;
	}



	/*
	 * That method initiates the query. The base query simply sets which tables will participate in the query and
	 * formulate all the required joins.
	 */
	private Object createBaseQuery(ChartDefinition definition){



	}

	/*
	 * Used in #createBaseQuery.
	 * 
	 * The query is linear : it is travedserd like this
	 */
	private EntityType[] findQueryScope(ChartDefinition definition){

		EntityType[] res = new EntityType[2];


		// get the involved types and flatten all the values in one set.
		Map<ColumnRole, Set<EntityType>> types = definition.getInvolvedEntities();




	}


}
