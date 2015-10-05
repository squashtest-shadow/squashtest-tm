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

import org.squashtest.tm.service.internal.chart.engine.InternalEntityType;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.*;
import org.squashtest.tm.service.internal.chart.engine.DetailedChartDefinition;
import org.squashtest.tm.service.internal.chart.engine.DomainGraph;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan;

import spock.lang.Specification
import spock.lang.Unroll;
import static org.squashtest.tm.domain.chart.EntityType.*
import org.apache.commons.collections.Transformer

class DomainGraphTest extends Specification {

	// some abreviations

	static InternalEntityType REQ = REQUIREMENT
	static InternalEntityType RV = REQUIREMENT_VERSION
	static InternalEntityType COV = REQUIREMENT_VERSION_COVERAGE
	static InternalEntityType TC = TEST_CASE
	static InternalEntityType ITP = ITEM_TEST_PLAN
	static InternalEntityType IT = ITERATION
	static InternalEntityType CP = CAMPAIGN
	static InternalEntityType EX = EXECUTION
	static InternalEntityType ISS = ISSUE




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
		domain.getNodes().collect{it.key} as Set == [REQ, RV, COV, TC, ITP, IT, CP, EX, ISS] as Set

		where :
		rootEntity				|	definition
		REQUIREMENT				|	new DetailedChartDefinition(rootEntity : REQ)
		REQUIREMENT_VERSION 	|	new DetailedChartDefinition(rootEntity : RV)
		COV					 	|	new DetailedChartDefinition(rootEntity : COV)
		TEST_CASE				|	new DetailedChartDefinition(rootEntity : TC)
		ITEM_TEST_PLAN			|	new DetailedChartDefinition(rootEntity : ITP)
		ITERATION				|	new DetailedChartDefinition(rootEntity : IT)
		CAMPAIGN				|	new DetailedChartDefinition(rootEntity : CP)
		EXECUTION				|	new DetailedChartDefinition(rootEntity : EX)
		ISS						|	new DetailedChartDefinition(rootEntity : ISS)

	}




	@Unroll
	def "should test many query plans"(){

		expect :
		def plan = DomainGraph.getQueryPlan(new DetailedChartDefinition(rootEntity : rootEntity, targetEntities : targets))

		checkAllTreeHierarchy(plan, hierarchy)

		where :

		// let's use the abbreviations
		rootEntity	|	targets				|	hierarchy
		REQ			|	[REQ, TC]			|	[ REQ : [RV], RV : [COV], COV : [TC], TC : [] ]
		ISS			|	[ISS, TC, IT]		|	[ ISS : [EX], EX : [ITP], ITP : [TC, IT], TC : [], IT : []]
		IT			|	[IT, ISS]			|	[ IT : [ITP], ITP : [EX], EX : [ISS], ISS : []]
		CP			|	[REQ, ISS]			|	[ CP : [IT], IT : [ITP], ITP : [TC, EX], TC : [COV], COV : [RV], RV : [REQ], REQ : [], EX : [ISS], ISS : []]
		ITP			|	[REQ, CP, ISS]		|	[ ITP : [TC, IT, EX], TC : [COV], COV : [RV], RV: [REQ], REQ : [], IT : [CP], CP : [], EX : [ISS], ISS : []]
	}



	def "should morph to a directed graph and generate an oversized query plan"(){

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
		checkIsDirectedEdge domain, TEST_CASE, REQUIREMENT_VERSION_COVERAGE
		checkIsDirectedEdge domain, REQUIREMENT_VERSION_COVERAGE, REQUIREMENT_VERSION
		checkIsDirectedEdge domain, REQUIREMENT_VERSION, REQUIREMENT
		checkIsDirectedEdge domain, TEST_CASE, ITEM_TEST_PLAN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, ITERATION
		checkIsDirectedEdge domain, ITERATION, CAMPAIGN
		checkIsDirectedEdge domain, ITEM_TEST_PLAN, EXECUTION
		checkIsDirectedEdge domain, EXECUTION, ISS


		// check the resulting tree
		def allroots= plan.getRootNodes()
		allroots.size() == 1

		def root = allroots[0]
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION_COVERAGE]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION, EXECUTION])
		checkTreeHierarchy(plan, EXECUTION, [ISS])
		checkTreeHierarchy(plan, ISS, [])
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

		traversed as Set == [ REQUIREMENT, REQUIREMENT_VERSION, REQUIREMENT_VERSION_COVERAGE, TEST_CASE, ITEM_TEST_PLAN, ITERATION, CAMPAIGN ] as Set

		def root = plan.getRootNodes()[0];
		root.key == TEST_CASE

		checkTreeHierarchy(plan, TEST_CASE, [ITEM_TEST_PLAN, REQUIREMENT_VERSION_COVERAGE]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION_COVERAGE, [REQUIREMENT_VERSION]);
		checkTreeHierarchy(plan, REQUIREMENT_VERSION, [REQUIREMENT]);
		checkTreeHierarchy(plan, REQUIREMENT, [])
		checkTreeHierarchy(plan, ITEM_TEST_PLAN, [ITERATION])
		checkTreeHierarchy(plan, ITERATION, [CAMPAIGN])
		checkTreeHierarchy(plan, CAMPAIGN, [])

	}


	def checkTreeHierarchy(QueryPlan tree, InternalEntityType nodetype, List<InternalEntityType> childrenTypes ){
		def node = tree.getNode(nodetype)
		return node.children.collect{it.key} as Set == childrenTypes as Set
	}



	def checkAllTreeHierarchy(QueryPlan tree, Map hierarchies){
		def checkall = true;

		hierarchies.each {k,v -> checkall = checkall &&  checkTreeHierarchy(tree, expand(k), v)}

		return checkall

	}

	def checkIsDirectedEdge(DomainGraph graph, InternalEntityType srcType, InternalEntityType destType){
		return (
		graph.hasEdge(srcType, destType) &&
		! graph.hasEdge(destType, srcType)
		)
	}

	def expand(String shortname){
		switch(shortname){
			case "REQ" : return REQUIREMENT;
			case "RV" : return REQUIREMENT_VERSION
			case "COV" : return REQUIREMENT_VERSION_COVERAGE
			case "TC" : return TEST_CASE
			case "ITP" : return ITEM_TEST_PLAN
			case "IT" : return ITERATION
			case "CP" : return CAMPAIGN
			case "EX" : return EXECUTION
			case "ISS" : return ISSUE
		}
	}

}
