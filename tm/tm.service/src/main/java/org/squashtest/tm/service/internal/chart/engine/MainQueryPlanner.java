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

import com.querydsl.jpa.hibernate.HibernateQuery;

/**
 * <p></p>
 * 
 * <p>
 * 	This class will generate the main query, that is a query that joins together the sequence of tables required for the given chart.
 * 	Whenever possible the natural joins will be used; however we are dependent on the way the entities were mapped : when no natural join
 * 	is available a where clause will be used.
 * </p>
 * 
 * <p>See javadoc on {@link ChartDataFinder}</p>
 * 
 * 
 * @author bsiri
 *
 */

class MainQueryPlanner {


	private DetailedChartDefinition definition;

	MainQueryPlanner(DetailedChartDefinition definition){
		this.definition = definition;

	}


	HibernateQuery<?> createMainQuery(){

		// get the query plan : the orderly set of joins this
		// class must now put together
		QueryPlan plan = DomainGraph.getQueryPlan(definition);

		// now get the query done
		//TraversedEntity rootNode = plan.getR
		return null;

	}


}
