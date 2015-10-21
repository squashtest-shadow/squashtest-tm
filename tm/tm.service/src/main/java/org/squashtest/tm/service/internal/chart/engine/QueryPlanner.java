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

import java.util.HashSet;
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

	private DetailedChartQuery definition;

	private QuerydslToolbox utils;

	// ******** work variables ************

	private Set<String> aliases = new HashSet<>();


	// ***** optional argument, you may specify them if using a ChartQuery with strategy INLINED ****
	// ***** see section "configuration builder" to check what they do *************

	private HibernateQuery<?> query;



	QueryPlanner(DetailedChartQuery definition){
		super();
		this.definition = definition;
		this.utils = new QuerydslToolbox();
	}


	QueryPlanner(DetailedChartQuery definition, QuerydslToolbox utils){
		this.definition = definition;
		this.utils = utils;
	}


	// ====================== configuration builder =================

	/**
	 * Use if you intend to use the queryplanner to append on a mainquery,
	 * before invoking {@link #modifyQuery()}. This method supplies the
	 * said main query.
	 * 
	 * @param existingQuery
	 * @return
	 */
	QueryPlanner appendToQuery(HibernateQuery<?> existingQuery){
		this.query = existingQuery;
		return this;
	}

	/**
	 * Use if you intend to use the queryplanner to append on a mainquery,
	 * before invoking {@link #modifyQuery()}. This method supplies
	 * the root entity, where the join should be made between the main query and
	 * this query
	 * 
	 * @param existingQuery
	 * @return
	 */
	QueryPlanner joinRootEntityOn(EntityPathBase<?> axeEntity){
		utils.forceAlias(definition.getRootEntity(), axeEntity.getMetadata().getName());
		return this;
	}


	// ====================== main API ===================================

	/**
	 * Will create a new query from scratch, based on the ChartQuery. No conf asked.
	 * 
	 * @return
	 */
	HibernateQuery<?> createQuery(){
		query = new HibernateQuery();
		doTheJob();
		return query;
	}


	/**
	 * Will append to the query (configured with {@link #appendToQuery(HibernateQuery)})
	 * the joins defined in the ChartQuery. This new set of joins will be attached to the
	 * main query on the root entity of this ChartQuery.
	 * 
	 * 
	 */

	void modifyQuery(){
		doTheJob();
	}

	// *********************** internal job **************************

	private void doTheJob(){

		init();

		// get the query plan : the orderly set of joins this
		// planner must now put together

		DomainGraph domain = new DomainGraph(definition);
		QueryPlan plan = domain.getQueryPlan();

		// now get the query done

		for (Iterator<PlannedJoin> iter = plan.joinIterator(); iter.hasNext();) {

			PlannedJoin join = iter.next();

			addJoin(join);

		}
	}

	@SuppressWarnings("rawtypes")
	private void init(){

		// register the content of the query,
		// it is useful mostly in the append mode.
		aliases = utils.getJoinedAliases(query);

		// initialize the query if needed
		EntityPathBase<?> rootPath = utils.getQBean(definition.getRootEntity());
		if (! isKnown(rootPath)){
			query.from(rootPath);
		}

	}


	private void addJoin(PlannedJoin joininfo){

		EntityPathBase<?> src = utils.getQBean(joininfo.getSrc());
		EntityPathBase<?> dest = utils.getQBean(joininfo.getDest());
		String attribute = joininfo.getAttribute();

		if (joininfo.getType() == JoinType.NATURAL){
			addNaturalJoin(src, dest, attribute);
		}
		else{
			addWhereJoin(src, dest, attribute);
		}

		registerAlias(src);
		registerAlias(dest);
	}


	@SuppressWarnings("rawtypes")
	private void addNaturalJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute){

		// check first that such join doesn't exist yet
		if (! isKnown(dest)){

			PathBuilder join = utils.makePath(src, dest, attribute);

			switch(definition.getJoinStyle()){
			case INNER_JOIN :
				query.innerJoin(join, dest);
				break;
			case LEFT_JOIN :
				query.leftJoin(join, dest);
				break;
			}
		}
	}

	private void addWhereJoin(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute){

		// check that the entities are known
		if (! isKnown(src)){
			query.from(src);
		}

		if (! isKnown(dest)){
			query.from(dest);
		}

		// remember that the join is made from the dest to the source in this case
		PathBuilder<?> destForeignKey = utils.makePath(dest, src, attribute);

		Predicate condition = Expressions.booleanOperation(Ops.EQ, destForeignKey, src);

		query.where(condition);
	}


	private boolean isKnown(InternalEntityType type){
		return isKnown(utils.getQBean(type));
	}

	private boolean isKnown(EntityPathBase<?> path){
		return aliases.contains(path.getMetadata().getName());
	}

	private void registerAlias(InternalEntityType type){
		registerAlias(utils.getQBean(type));
	}

	private void registerAlias(EntityPathBase<?> path){
		aliases.add(path.getMetadata().getName());
	}

}
