/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService
import org.squashtest.tm.service.internal.chart.ColumnPrototypeModification;
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
	ScopeUtils utils


	ScopePlanner scopePlanner = Mock()

	def setup(){
		sessionFactory = Mock()
		permissionService = Mock()
		utils = Mock()

		scopePlanner = new ScopePlanner();
		scopePlanner.sessionFactory = sessionFactory
		scopePlanner.permissionService = permissionService
		scopePlanner.utils = utils
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

		def res = scopePlanner.mapScopeByType()

		then :

		res[et('TCL')] == [1,2]
		res[et('TCF')] == [2,3]

	}


	def "should return the aggregate result of a lookup for multiple entries in a map"(){

		given :
		def map = [:];
		map.put(et('TC'), [1, 2, 3])
		map.put(et('TCF'), [4,5,6])
		map.put(et('TCL'), [7,8,9])

		when :
		def res = scopePlanner.fetchForTypes(map, et('TC'), et('TCL'))


		then :
		res as Set == [1,2,3,7,8,9] as Set

	}

	def "should trim from the scope because of failed ACL test"(){
		
		given :
			def ref1 = ref('P', 5)
			def ref2 = ref('TCF', 15)
			utils.checkPermissions(_) >>> [false, true]
			scopePlanner.scope = [ ref1, ref2]
		
		when :
			scopePlanner.filterByACLs()
		
		then :
			scopePlanner.scope == [ref2]
		
	}

	
	def "should declare that the scope is relevant because is can be applied to campaigns"(){
		
		// chart encompass test cases and campaign
		// scope defined campaign folders and requirement library
		// -> they have the campaign in common
		given :
			DetailedChartQuery q = new DetailedChartQuery(targetEntities : [iet('TC'), iet('C')])
			List<EntityReference> scope = [ref('CF', 10), ref('RL', 65)]
			
			scopePlanner.chartQuery = q
			scopePlanner.scope = scope
		
		when :
			def res = scopePlanner.isScopeRelevant()
		
		then :
			res == true
		
	}
	
	def "should declare a scope irrelevant because the Scope and the content of the query are disjoint"(){
		
		// chart encompass test cases 
		// scope defined campaign folders
		// -> they have nothing in common
		given :
			DetailedChartQuery q = new DetailedChartQuery(targetEntities : [iet('TC')])
			List<EntityReference> scope = [ref('CF', 10)]
			
			scopePlanner.chartQuery = q
			scopePlanner.scope = scope
		
		when :
			def res = scopePlanner.isScopeRelevant()		

		then :
			res == false
		
	}
	
	def "should decalre a scope irrelevant because the scope is empty"(){
		
		// chart encompass test cases
		// scope is empty
		// -> they have nothing in common
		given :
			DetailedChartQuery q = new DetailedChartQuery(targetEntities : [iet('TC')])
			List<EntityReference> scope = []
			
			scopePlanner.chartQuery = q
			scopePlanner.scope = scope
		
		when :
			def res = scopePlanner.isScopeRelevant()		

		then :
			res == false
		
	}
	
	def "should add an impossible condition to a query in order to make it return no data"(){
		
		given :
			def tc = QTestCase.testCase
			ExtendedHibernateQuery hibQuery = new ExtendedHibernateQuery().select(tc.id).from(tc)
		
			scopePlanner.hibQuery = hibQuery
			
		when :
			scopePlanner.addImpossibleCondition()
			
		then :
			scopePlanner.hibQuery.toString() == 
"""select testCase.id
from TestCase testCase
where ?1 = ?2"""	
		
		
	}
	
	
	def "should find which extra columns should be joined on for the purposes of the Scope"(){
		
		given :
			DetailedChartQuery q = new DetailedChartQuery(targetEntities : [iet('TC'), iet('RV')])
			List<EntityReference> scope = [ref('RL', 132)]
		
			scopePlanner.chartQuery = q
			scopePlanner.scope = scope
			
		when :
			def extraColumns = scopePlanner.findExtraJoinColumnNames()
		
		then :
			extraColumns == ['REQUIREMENT_ID']as Set
		
	}
	
	
	def "should create a mock query for the purpose of extending the main query with required joins"(){
		
		given :
			def axis = Mock(AxisColumn)
			DetailedChartQuery q = new DetailedChartQuery(axis : [axis])
			
			scopePlanner.chartQuery = q
			def fakeColumns = ['REQUIREMENT_ID', 'CAMPAIGN_ID']
			
		and :
			def fakeReqidProto = Mock(ColumnPrototype)
			def fakeCidProto = Mock(ColumnPrototype)
			utils.findColumnPrototype('REQUIREMENT_ID') >> fakeReqidProto
			utils.findColumnPrototype('CAMPAIGN_ID') >> fakeCidProto
			
		when :
			ChartQuery dummy = scopePlanner.createDummyQuery(fakeColumns as Set)
		
		then :	
			dummy.axis == [ axis]
			dummy.measures.collect{it.column} as Set == [fakeReqidProto, fakeCidProto] as Set
		
	}
	
	def "should generate where clauses testing that a test case belongs to a library or a folder"(){
		
		given :
			def refmap = [:]
			refmap.put(et('TCL') , [10])
			refmap.put(et('TCF'),  [15])
			
		and :
			def tc = QTestCase.testCase
			def testquery = new ExtendedHibernateQuery()
			testquery.from(tc).select(tc.id)
			
		when :
			def builder = scopePlanner.whereClauseForTestcases(refmap)
			testquery.where(builder)
		then :
			testquery.toString() == """select testCase.id
from TestCase testCase
where testCase.project.testCaseLibrary.id = ?1 or exists (select 1
from TestCasePathEdge testCasePathEdge
where testCasePathEdge.ancestorId = ?2 and testCase.id = testCasePathEdge.descendantId)"""
		
		
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
