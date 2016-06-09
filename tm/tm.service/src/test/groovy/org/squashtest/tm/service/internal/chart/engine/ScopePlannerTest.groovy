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

import javax.persistence.EntityManager;

import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.SessionFactory
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService
import org.squashtest.tm.service.internal.chart.ColumnPrototypeModification;
import org.squashtest.tm.service.internal.chart.engine.ScopePlanner.ScopeUtils;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService
import org.squashtest.tm.service.internal.chart.engine.ScopePlanner.ScopedEntities
import org.squashtest.tm.service.internal.chart.engine.ScopePlanner.ExtraJoinColumns
import org.squashtest.tm.domain.chart.SpecializedEntityType
import org.squashtest.tm.domain.chart.ColumnType

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

	EntityManager em
	PermissionEvaluationService permissionService
	ScopeUtils utils


	ScopePlanner scopePlanner = Mock()

	def setup(){
		em = Mock()
		permissionService = Mock()
		utils = Mock()

		scopePlanner = new ScopePlanner();
		scopePlanner.em = em
		scopePlanner.permissionService = permissionService
		scopePlanner.utils = utils
	}

        // ******************* ACL check tests *********************


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

        // ********************* ScopedEntities test ***********************
    
	def "should build a ScopedEntities instance from the scope"(){

		given :
		def scope = [ref('TCL', 1), ref('TCF', 2), ref('TCF', 3), ref('TCL', 2)]

		when :

		def res = new ScopedEntities(scope)

		then :

		res[et('TCL')] == [1,2]
		res[et('TCF')] == [2,3]

	}


	def "ScopedEntities should return all the ids for given entity types"(){

		given :
		def scopedEntities = new ScopedEntities();
                
                def tTC = et('TC')
                def tTCF = et('TCF')
                def tTCL = et('TCL')
        
                and :
		scopedEntities.put(tTC, [1, 2, 3])
		scopedEntities.put(tTCF, [4,5,6])
		scopedEntities.put(tTCL, [7,8,9])

		when :
		def res = scopedEntities.getIds(tTC, tTCL)


		then :
		res as Set == [1,2,3,7,8,9] as Set

	}

        
        @Unroll("should find which extra columns a scope spans across")
        def "should find which extra columns the Scope span across"(){
            
            expect :
                new ScopedEntities(scope).getExtraJoinColumns() == extraJoins
            
            where :
            
            scope                                   |   extraJoins
                [ref('P', 1l)]                      |   ["TEST_CASE_ID", "REQUIREMENT_ID", "CAMPAIGN_ID"] as Set
                [ref('TC', 1l), ref('R', 1l)]       |   ["TEST_CASE_ID", "REQUIREMENT_ID"] as Set
                [ref('R', 1l), ref('RL', 1l)]       |   ["REQUIREMENT_ID"] as Set
                [ref('CL', 1L), ref('CF', 1L)]      |   ["CAMPAIGN_ID"] as Set
                [ref('TCF', 1L), ref('IT', 1L)]     |   ["TEST_CASE_ID", "ITERATION_ID"] as Set
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
      
        def "should generate the required extra joins between the main query and the scope"(){
            
   		given : "scope"
                    
                    scopePlanner.scope = [ref('TCL', 10L), ref('TCF', 15L)]
			
		and : "chart query"                        
                        scopePlanner.chartQuery = mockQuery(EntityType.REQUIREMENT, EntityType.REQUIREMENT)       
        
                and : "scope columns"
			def scopeProto = Mock(ColumnPrototype)
                        scopeProto.specializedType >> new SpecializedEntityType(entityType:EntityType.TEST_CASE)
			utils.findColumnPrototype('TEST_CASE_ID') >> scopeProto
			
		
                and : "(the corresponding main query)"
			def r = QRequirement.requirement
			def testquery = new ExtendedHibernateQuery()
			testquery.from(r).select(r.id)
                        scopePlanner.hibQuery = testquery
		when :
			scopePlanner.addExtraJoins()
                
		then :
        System.out.println(testquery.toString())
        testquery.toString() == """select requirement.id
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase"""
        }
	
	def "should generate where clauses testing that a test case belongs to a library or a folder"(){
		
		given :
			def refmap = new ScopedEntities()
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
        
        def mockQuery(axType, meaType){
            def saxType = new SpecializedEntityType(entityType : axType)
            def smeaType = new SpecializedEntityType(entityType : meaType)
        
            // axis
            def mainAxisProto = new ColumnPrototype(specializedType : saxType)
            def mainAxis = new AxisColumn(column : mainAxisProto)
            
            // measure
            def mainMeasureProto = new ColumnPrototype(specializedType : smeaType)
            def mainMeasure = new MeasureColumn(column : mainMeasureProto)
            

            new DetailedChartQuery(axis : [mainAxis], measures : [mainMeasure])
        }

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


}
