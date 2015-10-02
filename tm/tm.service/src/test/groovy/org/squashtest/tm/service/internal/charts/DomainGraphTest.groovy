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
package org.squashtest.tm.service.internal.charts

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.service.internal.charts.DomainGraph.QueryPlan;

import spock.lang.Specification
import spock.lang.Unroll;
import static org.squashtest.tm.domain.EntityType.*
import org.apache.commons.collections.Transformer

class DomainGraphTest extends Specification {

	// some abreviations

	static def REQ = REQUIREMENT
	static def RV = REQUIREMENT_VERSION
	static def TC = TEST_CASE
	static def ITP = ITEM_TEST_PLAN
	static def IT = ITERATION
	static def CP = CAMPAIGN
	static def EX = EXECUTION


	def checkTreeHierarchy(QueryPlan tree, EntityType nodetype, List<EntityType> childrenTypes ){
		def node = tree.getNode(nodetype)
		return node.children.collect{it.key} as Set == childrenTypes as Set
	}

	def checkAllTreeHierarchy(QueryPlan tree, Map hierarchies){
		def checkall = true;

		hierarchies.each {k,v -> checkall = checkall &&  checkTreeHierarchy(tree, EntityType.valueOf(k), v)}

		return checkall

	}

	def checkIsDirectedEdge(DomainGraph graph, EntityType srcType, EntityType destType){
		return (
		graph.hasEdge(srcType, destType) &&
		! graph.hasEdge(destType, srcType)
		)
	}


	/*
	 * General properties :
	 * 1/ the root node has no inbounds
	 * 2/ the other nodes has 1 inbound only
	 * 3/ all entities are traversed
	 */
	@Unroll("check general asumption about morphed graphs with #rootEntity (see comments)")
	def "check general asumption about morphed graphs (see comments)"(){

		expect :
		def domain = new DomainGraph(definition);
		domain.morphToQueryPlan();

		domain.getNode(rootEntity).getInbounds().size() == 0
		domain.getNodes().findAll{it.key != rootEntity} as Set == domain.getNodes().findAll{it.inbounds.size()==1} as Set
		domain.getNodes().collect{it.key} as Set == [REQUIREMENT, REQUIREMENT_VERSION, TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN, EXECUTION, ISSUE] as Set

		where :
		rootEntity				|	definition
		REQUIREMENT				|	new DetailedChartDefinition(rootEntity : REQUIREMENT)
		REQUIREMENT_VERSION 	|	new DetailedChartDefinition(rootEntity : REQUIREMENT_VERSION)
		TEST_CASE				|	new DetailedChartDefinition(rootEntity : TEST_CASE)
		ITEM_TEST_PLAN			|	new DetailedChartDefinition(rootEntity : ITEM_TEST_PLAN)
		ITERATION				|	new DetailedChartDefinition(rootEntity : ITERATION)
		CAMPAIGN				|	new DetailedChartDefinition(rootEntity : CAMPAIGN)
		EXECUTION				|	new DetailedChartDefinition(rootEntity : EXECUTION)
		ISSUE						|	new DetailedChartDefinition(rootEntity : ISSUE)

	}




	@Unroll
	def "should test many query plans"(){

		expect :
		def plan = DomainGraph.getQueryPlan(new DetailedChartDefinition(rootEntity : rootEntity, targetEntities : targets))

		checkAllTreeHierarchy(plan, hierarchy)

		where :

		// let's use the abbreviations
		rootEntity	|	targets				|	hierarchy
		REQ			|	[REQ, TC]			|	[ REQUIREMENT : [RV] , REQUIREMENT_VERSION : [TC], TEST_CASE : [] ]
		ISSUE			|	[ISSUE, TC, IT]		|	[ ISSUE : [EX], EXECUTION : [ITP], ITEM_TEST_PLAN : [TC, IT], TEST_CASE : [], ITERATION : []]
		IT			|	[IT, ISSUE]			|	[ITERATION : [ITP], ITEM_TEST_PLAN : [EX], EXECUTION : [ISSUE], ISSUE : []]
		CP			|	[REQ, ISSUE]			|	[CAMPAIGN : [IT], ITERATION : [ITP], ITEM_TEST_PLAN : [TC, EX], TEST_CASE : [RV], REQUIREMENT_VERSION : [REQ], REQUIREMENT : [], EXECUTION : [ISSUE], ISSUE : []]
		ITP			|	[REQ, CP, ISSUE]		|	[ITEM_TEST_PLAN : [TC, IT, EX], TEST_CASE : [RV], REQUIREMENT_VERSION : [REQ], REQUIREMENT : [], ITERATION : [CP], CAMPAIGN : [], EXECUTION : [ISSUE], ISSUE : []]
	}



	def "should trim to a directed graph"(){

		given :
		DetailedChartDefinition definition =
				new DetailedChartDefinition(rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		and :
		def domain = new DomainGraph(definition)

		when :
		def plan = domain.morphToQueryPlan();

		then :

		// check how the graph has been modified
		checkIsDirectedEdge domain, TEST_CASE, REQUIREMENT_VERSION
		checkIsDirectedEdge domain, REQUIREMENT_VERSION, REQUIREMENT
		checkIsDirectedEdge domain, TEST_CASE, ITEM_TEST_PLAN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, ITERATION
		checkIsDirectedEdge domain, ITERATION, CAMPAIGN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, EXECUTION
		checkIsDirectedEdge domain, EXECUTION, ISSUE


		// check the resulting tree
		def allroots= plan.getRootNodes()
		allroots.size() == 1

		def root = allroots[0]
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION, EXECUTION])
		checkTreeHierarchy(plan, EXECUTION, [ISSUE])
		checkTreeHierarchy(plan, ISSUE, [])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [])

	}


	def "should find a query plan for root entity TestCase and other target entities : Requirement, Iteration"(){

		given :
		DetailedChartDefinition definition =
				new DetailedChartDefinition(rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, REQUIREMENT, CAMPAIGN])

		when :

		QueryPlan plan = DomainGraph.getQueryPlan(definition);

		then :

		def traversed = plan.collectKeys() as Set

		traversed as Set == [ REQUIREMENT, REQUIREMENT_VERSION, TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN ] as Set

		def root = plan.getRootNodes()[0];
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [])

	}

}
