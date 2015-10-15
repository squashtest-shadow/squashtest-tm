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

import java.util.Iterator;
import java.util.Set;

import org.squashtest.tm.service.internal.chart.engine.PlannedJoin.JoinType;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

/**
 * <p>
 * 	This class will plan which table must be joined together and return the result as a HibernateQuery.
 * 	Whenever possible the natural joins will be used; however we are dependent on the way the entities were mapped : when no natural join
 * 	is available a where clause will be used.
 * </p>
 * 
 * <p>
 * 	In this query the entities are all aliased with the camel case version of the class name. Explicitly : testCase, requirementVersion etc
 * </p>
 * 
 * <p>
 * 	Remember that the query created is detached from the session, don't forget to attach it via query.clone(session)
 * </p>
 * 
 * <p>See javadoc on {@link ChartDataFinder}</p>
 * 
 * 
 * @author bsiri
 *
 */

class QueryPlanner {

	private DetailedChartDefinition definition;

	private HibernateQuery<?> query;

	private QuerydslToolbox utils;

	QueryPlanner(DetailedChartDefinition definition){
		super();
		this.definition = definition;
		this.utils = new QuerydslToolbox();
	}

	QueryPlanner(DetailedChartDefinition definition, QuerydslToolbox utils){
		this.definition = definition;
		this.utils = utils;
	}


	HibernateQuery<?> createQuery(){

		query = new HibernateQuery();

		// get the query plan : the orderly set of joins this
		// class must now put together
		QueryPlan plan = DomainGraph.getQueryPlan(definition);

		// now get the query done
		init();

		for (Iterator<PlannedJoin> iter = plan.joinIterator(); iter.hasNext();) {

			PlannedJoin join = iter.next();

			addJoin(join);

		}

		return query;

	}

	@SuppressWarnings("rawtypes")
	private void init(){
		InternalEntityType rootType = definition.getRootEntity();
		EntityPathBase rootPath = utils.getQBean(rootType);
		query.from(rootPath);
	}


	private void addJoin(PlannedJoin joininfo){
		if (joininfo.getType() == JoinType.NATURAL){
			addNaturalJoin(joininfo);
		}
		else{
			addWhereJoin(joininfo);
		}
	}

	@SuppressWarnings("rawtypes")
	private void addNaturalJoin(PlannedJoin joininfo){

		PathBuilder join = utils.makePath(joininfo.getSrc(), joininfo.getDest(), joininfo.getAttribute());

		EntityPathBase dest = utils.getQBean(joininfo.getDest());

		query.innerJoin(join, dest);

	}

	/*
	 * This method will check first whether the entities needs to be
	 * added to the "from" clause. Then, a where clause will be added,
	 * the condition being that dest.attribute = src.id
	 */
	private void addWhereJoin(PlannedJoin joininfo){

		// check that tables are known
		prepareFromClause(joininfo);

		// now make the join
		// remember that the join is made from the dest to the source in this case
		PathBuilder<?> destForeignkey = utils.makePath(joininfo.getDest(), joininfo.getSrc(), joininfo.getAttribute());

		Predicate condition  = Expressions.booleanOperation(Ops.EQ, destForeignkey, utils.getQBean(joininfo.getSrc()));

		query.where(condition);

	}


	private void prepareFromClause(PlannedJoin joininfo){

		Set<String> allAliases = utils.getJoinedAliases(query);

		InternalEntityType src = joininfo.getSrc();
		InternalEntityType dest = joininfo.getDest();

		String srcAlias = utils.getQName(src);
		String destAlias = utils.getQName(dest);

		if (! allAliases.contains(srcAlias)){
			query.from(utils.getQBean(src));
		}

		if (! allAliases.contains(destAlias)){
			query.from(utils.getQBean(dest));
		}
	}




}
