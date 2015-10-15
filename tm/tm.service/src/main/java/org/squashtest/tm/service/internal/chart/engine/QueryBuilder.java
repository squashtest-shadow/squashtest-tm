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

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.hibernate.HibernateQuery;

class QueryBuilder {

	private QuerydslToolbox utils;


	QueryBuilder(){
		this(null);
		utils = new QuerydslToolbox();
	}

	QueryBuilder(String contextName){
		super();
		this.utils = new QuerydslToolbox(contextName);
	}

	EntityPathBase<?> getQBean(InternalEntityType type){
		return utils.getQBean(type);
	}


	HibernateQuery<?> createQuery(DetailedChartDefinition definition){

		HibernateQuery detachedQuery;

		// *********** step 1 : define the query plan ************************

		QueryPlanner mainPlanner = new QueryPlanner(definition, utils);
		detachedQuery = mainPlanner.createQuery();

		// *********** step 2 : add the projection and group by clauses ******

		ProjectionPlanner projectionPlanner = new ProjectionPlanner(definition, detachedQuery, utils);
		projectionPlanner.modifyQuery();

		// ******************* step 3 : the filters **************************

		FilterPlanner filterPlanner = new FilterPlanner(definition, detachedQuery);
		filterPlanner.modifyQuery();

		return detachedQuery;
	}


}
