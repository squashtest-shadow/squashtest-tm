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

import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.SessionFactory
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService
import org.squashtest.tm.service.internal.chart.engine.ScopePlanner.ScopeUtils;
import org.squashtest.tm.service.internal.chart.engine.ScopePlanner.SubScope;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService

import spock.lang.Specification
import spock.lang.Unroll;


class ScopePlannerTest extends Specification {

	static def shorthands = [
		P	:	"PROJECT",
		TCL : "TEST_CASE_LIBRARY",
		TCF : "TEST_CASE_FOLDER",
		TC : "TEST_CASE",
		RL : "REQUIREMENT_LIBRARY",
		RF : "REQUIREMENT_FOLDER",
		R : "REQUIREMENT",
		RV : "REQUIREMENT_VERSION",
		CL : "CAMPAIGN_LIBRARY",
		CF : "CAMPAIGN_FOLDER",
		C : "CAMPAIGN",
		IT : "ITERATION",
		ITP : "ITEM_TEST_PLAN",
		EX : "EXECUTION",
		ISS : "ISSUE"
	]






	SessionFactory sessionFactory
	PermissionEvaluationService permissionService
	TestCaseLibraryFinderService tcFinder
	RequirementLibraryFinderService rFinder
	CampaignLibraryFinderService cFinder
	ScopeUtils utils


	ScopePlanner scopePlanner = Mock()

	def setup(){
		sessionFactory = Mock()
		permissionService = Mock()
		tcFinder = Mock()
		rFinder = Mock()
		cFinder = Mock()
		utils = Mock()

		scopePlanner = new ScopePlanner();
		scopePlanner.sessionFactory = sessionFactory
		scopePlanner.permissionService = permissionService
		scopePlanner.tcFinder = tcFinder
		scopePlanner.rFinder = rFinder
		scopePlanner.cFinder = cFinder
		scopePlanner.utils = utils
	}

	@Unroll("should detect that mode is #humanmsg")
	def "should detect whether it is blanket mode or specified mode"(){


		given :
		scopePlanner.scope = scope

		expect :
		scopePlanner.isBlanketProjectMode() == isBlanket

		where :

		scope			|	isBlanket	| humanmsg
		[ref('P', 1)]	|	true		| "blanket project mode"
		[ref('TC', 1)]	|	false		| "specific mode"

	}

	@Unroll("should determine the query subscopes")
	def "should find query scopes"(){

		given :
		DetailedChartQuery q = new DetailedChartQuery(targetEntities : targets)

		scopePlanner.chartQuery = q

		expect :
		scopePlanner.findQuerySubScopes() == subscopes as Set

		where :

		targets 					|	subscopes
		[iet('TC'), iet('RV')]		|	[ss('TC'), ss('R')]
		[iet('EX'), iet('ISS')]		|	[ss('C')]
	}


	def "should simplify, sort and aggregate references from the scope"(){

		given :
		def scope = [ref('TCL', 1), ref('TCF', 2), ref('TCF', 3), ref('TCL', 2)]
		scopePlanner.scope = scope;

		when :

		def res = scopePlanner.aggregateReferences()

		then :

		res[et('TCL')] == [1,2]
		res[et('TCF')] == [2,3]

	}


	def "should just test that at least one collection contains something"(){

		given :
		def col1 = []
		def col2 = [1]
		def col3 = []

		when :
		def res = scopePlanner.nonEmpty(col1, col2, col3)

		then :
		res == true

	}

	def "should return the aggregate result of a lookup for multiple entries in a map"(){

		given :
		def map = new MultiValueMap()
		map.putAll(et('TC'), [1, 2, 3])
		map.putAll(et('TCF'), [4,5,6])
		map.putAll(et('TCL'), [7,8,9])

		when :
		def res = scopePlanner.fetchForTypes(map, et('TC'), et('TCL'))


		then :
		res as Set == [1,2,3,7,8,9] as Set

	}


	def "should build a blanket project filter but restricted to campaign subscope"(){

		given :
		def refP = ref('P',1l)
		def scope = [refP]

		utils.canReadProject(refP) >> true

		and :
		def query = new DetailedChartQuery(
				targetEntities : [ iet('C'), iet('EX')] )

		and :
		Filter mockf = Mock()
		utils.createFilter('CAMPAIGN_PROJECT', 1l) >> mockf

		and :
		scopePlanner.scope = scope
		scopePlanner.chartQuery = query

		when :
		def filters = scopePlanner.generateBlanketFilters()

		then :
		filters as Set == [mockf] as Set

	}


	def "should create specific filters"(){
		given :
		def scope = [
			ref('TCL', 1),
			ref('TCF', 2),
			ref('TCF', 3),
			ref('TCL', 4),
			ref('TC', 5)
		]

		and :
		def query = new DetailedChartQuery(
				targetEntities : [ iet('C'), iet('TC')] )

		and :
		def tcIds = Arrays.asList([11l, 23l, 45l, 67l, 89l, 5l] as Long[])
		tcFinder.findTestCaseIdsFromSelection([1, 4], [5, 2, 3]) >> tcIds

		and :
		Filter mockf = Mock()
		utils.createFilter('TEST_CASE_ID', tcIds) >> mockf

		and :
		scopePlanner.scope = scope
		scopePlanner.chartQuery = query

		when :

		def filters = scopePlanner.generateSpecificFilters()

		then :
		filters as Set == [mockf] as Set
		0 * rFinder.findRequirementIdsFromSelection(_,_)
		0 * cFinder.findCampaignIdsFromSelection(_,_)

	}


	// **************** test utils ************************

	def ref(entityname, id){
		EntityType type = EntityType.valueOf(expand(entityname));
		return new EntityReference(type, id)
	}



	def expand(shorthand){
		def expanded = shorthands[shorthand]

		return (expanded != null) ? expanded : shorthand
	}

	// stands for EntityType
	def et(name){
		return EntityType.valueOf(expand(name))
	}

	// stands for InternalEntityType
	def iet(name){
		return InternalEntityType.valueOf(expand(name))
	}

	// stands for SubScope
	def ss(name){
		return SubScope.valueOf(expand(name))
	}

}
