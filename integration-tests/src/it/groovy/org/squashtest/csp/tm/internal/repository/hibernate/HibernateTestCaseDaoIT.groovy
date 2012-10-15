/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.requirement.RequirementCategory
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tools.unittest.assertions.ListAssertions
import org.squashtest.tm.core.foundation.collection.Paging
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestCaseDaoIT extends DbunitDaoSpecification {
	@Inject TestCaseDao testCaseDao
	
	def setupSpec() {
		List.metaClass.containsSameIdentifiers << { ids ->
			assert delegate.size() == ids.size()
			assert (delegate.collect { it.id }).containsAll(ids)
			true
		}
		
		ListAssertions.declareIdsEqual() 
	}

	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should find filtered steps by test case id"() {
		given:
		Paging filter = Mock()
		filter.firstItemIndex >> 1
		filter.pageSize >> 2

		when:
		def steps = testCaseDao.findAllStepsByIdFiltered(10, filter)

		then:
		steps.collect { it.id } == [200, 300]
	}
	
	@DataSet("HibernateTestCaseDaoIT.should count calling test steps.xml")
	def "should count calling test steps"() {
		when:
		def callers = testCaseDao.countCallingTestSteps(10L)

		then:
		callers == 1
	}
	
	@DataSet("HibernateTestCaseDaoIT.should count calling test steps.xml")
	def "should count no calling test steps"() {
		when:
		def callers = testCaseDao.countCallingTestSteps(20L)

		then:
		callers == 0
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find called test cases.xml")
	def "should find called test cases"() {
		when:
		def calleds = testCaseDao.findTestCasesHavingCaller([10L, 30L])

		then:
		calleds == [10L]
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find called test cases.xml")
	def "should find no called test cases"() {
		when:
		def calleds = testCaseDao.findTestCasesHavingCaller([30L])

		then:
		calleds == []
	}

	@DataSet("HibernateTestCaseDaoIT.should find ids of test cases called by a test case.xml")
	def "should find ids of test cases called by a test case"() {
		when:
		def callees = testCaseDao.findAllTestCasesIdsCalledByTestCase(110L)

		then:
		callees == [10L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find ids of test cases called by several test cases.xml")
	def "should find ids of test cases called by several test cases"() {
		when:
		def callees = testCaseDao.findAllTestCasesIdsCalledByTestCases([110L, 120L])

		then:
		callees == [10L, 20L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find distinct ids of test cases called by several test cases.xml")
	def "should find distinct ids of test cases called by several test cases"() {
		when:
		def callees = testCaseDao.findAllTestCasesIdsCalledByTestCases([110L, 120L])

		then:
		callees == [10L]
	}

	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def " (*) should find the UNIQUES calling test cases sorted by test case name"(){
		given :
		CollectionSorting sorting = new CollectionSorting() {

			@Override
			public int getFirstItemIndex() {
				return 0;
			}

			@Override
			public int getPageSize() {
				return 10;
			}

			@Override
			public String getSortingOrder() {
				return "asc";
			}


			@Override
			public String getSortedAttribute() {
				return "TestCase.name";
			}
		};

		and :
		def resultNames = [
			"first test case",
			"second test case",
			"third test case"
		]
		def resultIds = [101l, 102l, 103l]


		when :
		def testCaseList = testCaseDao.findAllCallingTestCases(100l, sorting);

		then :
		testCaseList.collect{it.id} == resultIds
		testCaseList.collect{it.name} == resultNames
	}


	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should find the id and names of pairs of caller and called test cases"(){
		given :

		List.metaClass.containsValue = { Object[] arg -> return containsValue(delegate, arg)  }

		when :
		def result = testCaseDao.findTestCasesHavingCallerDetails([100l, 50l]);
		then :
		result.size == 6



		def array1 = [
			101,
			"first test case",
			50,
			"other bottom test case"
		].toArray();
		def array2 = [
			102,
			"second test case",
			50,
			"other bottom test case"
		].toArray();
		def array3 = [
			103,
			"third test case",
			50,
			"other bottom test case"
		].toArray();
		def array4 = [
			101,
			"first test case",
			100,
			"bottom test case"
		].toArray();
		def array5 = [
			102,
			"second test case",
			100,
			"bottom test case"
		].toArray();
		def array6 = [
			103,
			"third test case",
			100,
			"bottom test case"
		].toArray();


		result.containsValue (array1)
		result.containsValue (array2)
		result.containsValue (array3)
		result.containsValue (array4)
		result.containsValue (array5)
		result.containsValue (array6)
	}

	@DataSet("HibernateTestCaseDaoIT.should find the calling test cases.xml")
	def "should return a list of caller/caller pair padded with null values when no caller is found"(){

		given :
		List.metaClass.containsValue = { Object[] arg -> return containsValue(delegate, arg)  }

		when :
		def result = testCaseDao.findTestCasesHavingCallerDetails([101l, 102l, 103l]);
		then :
		result.size == 3



		def array1 = [
			null,
			null,
			101,
			"first test case"
		].toArray();
		def array2 = [
			null,
			null,
			102,
			"second test case"
		].toArray();
		def array3 = [
			null,
			null,
			103,
			"third test case"
		].toArray();


		result.containsValue (array1)
		result.containsValue (array2)
		result.containsValue (array3)
	}

	@DataSet("HibernateTestCaseDaoIT.deletiontest.xml")
	def "should delete a test case and cascade-remove some relationships"(){

		when :
		def tc = testCaseDao.findById(1l)
		testCaseDao.remove(tc)

		def retc = testCaseDao.findById(1l)

		then :

		tc != null

		retc == null
	}






	//if there is a groovy way to do that please tell me
	private boolean containsValue(List<Object[]> list, Object[] value){
		for (Object[] item : list){
			boolean match = true;
			for (int i=0;i<value.length;i++){
				if ( item[i] != value[i]){
					match=false;
					break;
				}
			}
			if (match) return true;
		}
		return false;

	}

	@DataSet("HibernateTestCaseDaoIT.should find test cases by requirement name token.xml")
	def "should find test cases by requirement name token"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.name >> "token"
		req.criticalities >> []
		req.categories >> []

		when:
		def res = testCaseDao.findAllByRequirement(req, false);

		then:
		res.containsSameIdentifiers([202L, 102L, 103L])
	}

	@DataSet("HibernateTestCaseDaoIT.should find test cases by requirement reference token.xml")
	def "should find test cases by requirement reference token"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.reference >> "token"
		req.criticalities >> []
		req.categories >> []

		when:
		def res = testCaseDao.findAllByRequirement(req, false);

		then:
		res.containsSameIdentifiers([202L, 102L, 103L])
	}

	@DataSet("HibernateTestCaseDaoIT.should find test cases by requirement reference and name token.xml")
	def "should find test cases by requirement reference and name token"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.reference >> "token"
		req.name >> "foo"
		req.criticalities >> []
		req.categories >> []

		when:
		def res = testCaseDao.findAllByRequirement(req, false);

		then:
		res.size() == 2
		res.containsSameIdentifiers([103L, 102L])
	}

	@DataSet("HibernateTestCaseDaoIT.should find test cases by requirement criticalities.xml")
	def "should find test cases by requirement criticalities"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> [
			RequirementCriticality.MINOR,
			RequirementCriticality.MAJOR
		]
		req.categories >> []
		when:
		def res = testCaseDao.findAllByRequirement(req, false);

		then:
		res.size() == 3
		res.containsSameIdentifiers([302L, 103L, 102L])
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find test cases by requirement categories.xml")
	def "should find test cases by requirement categories"() {
		given:
		RequirementSearchCriteria req = Mock()
		req.criticalities >> []
		req.categories >> [RequirementCategory.UNDEFINED, RequirementCategory.FUNCTIONAL]
		when:
		def res = testCaseDao.findAllByRequirement(req, false);

		then:
		res.size() == 3
		res.containsSameIdentifiers([302L, 103L, 102L])
	}
	
	@DataSet("HibernateTestCaseDaoIT.search-by-criteria-setup.xml")
	def "should find the test cases and folders ordered by names, not grouped by project and no importance filter"(){
		
		given :
			
			def criteria = Mock(TestCaseSearchCriteria)
			criteria.getName() >> "roject"
			criteria.isGroupByProject() >> false
			criteria.getImportanceFilterSet() >> Collections.emptyList();
		
		when :
			def result = testCaseDao.findBySearchCriteria(criteria)
			
		then :
			result.collect{it.id} == [ 211,121,  112, 221, 212, 111 ]
			result.collect{it.name} == [
				"aaa project Ed test case 1",
				"aaa project Ion folder 1",
				"aaa project Ion test case 2",
				"bbb project Ed folder 1",
				"bbb project Ed test case 2",
				"bbb project Ion test case 1"
			]
	}
	
	
	@DataSet("HibernateTestCaseDaoIT.search-by-criteria-setup.xml")
	def "should find the test cases and folders ordered by names, grouped by project and no importance filter"(){
		
		given :
			
			def criteria = Mock(TestCaseSearchCriteria)
			criteria.getName() >> "roject"
			criteria.isGroupByProject() >> true
			criteria.getImportanceFilterSet() >> Collections.emptyList();
		
		when :
			def result = testCaseDao.findBySearchCriteria(criteria)
			
		then :
			result.collect{it.id} == [ 121, 112, 111, 211, 221, 212]
			result.collect{it.name} == [
				"aaa project Ion folder 1",
				"aaa project Ion test case 2",
				"bbb project Ion test case 1",
				"aaa project Ed test case 1",
				"bbb project Ed folder 1",
				"bbb project Ed test case 2",
			]
	}
	
	@DataSet("HibernateTestCaseDaoIT.search-by-criteria-setup.xml")
	def "should find test cases ordered by names, not grouped by project and having importance MEDIUM and HIGH"(){
		
		given :
			def criteria = Mock(TestCaseSearchCriteria)
			criteria.getName() >> "roject"
			criteria.isGroupByProject() >> false
			criteria.getImportanceFilterSet() >> [TestCaseImportance.MEDIUM, TestCaseImportance.HIGH] 
		
		when :
			def result = testCaseDao.findBySearchCriteria(criteria)
		
		then :
		
			result.collect{it.id} == [ 112, 212 ]
			result.collect{it.name} == [
				"aaa project Ion test case 2", "bbb project Ed test case 2"
			]
	
	}
	
	@DataSet("HibernateTestCaseDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def id = 10L;
		def result = testCaseDao.findAllExecutionByTestCase(id)
		
		then:
		result.size() == 3
		result.each {it.name == "testCase1-execution"}
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find on name.xml")
	def "should find names in folder starting with"(){
		when:
		def folderId = 1L
		def nameStart = "nameStart"
		def result = testCaseDao.findNamesInFolderStartingWith(folderId, nameStart)
		
		then:
		result.size() == 2
		result.any {((String) it) == "nameStart-deux" }
		result.any {((String) it) == "nameStart-quatre" }
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find on name.xml")
	def "should find names in library starting with"(){
		when:
		def libraryId = 1L
		def nameStart = "nameStart"
		def result = testCaseDao.findNamesInLibraryStartingWith(libraryId, nameStart)
		
		then:
		result.size() == 2
		result.any {((String) it) == "nameStart-cinq" }
		result.any {((String) it) == "nameStart-six" }
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find on name.xml")
	def "should find all by name containing "(){
		when:
		def token = "token"
		def groupedByProject = false
		def result = testCaseDao.findAllByNameContaining(token, groupedByProject);
		
		then:
		result.size() == 4
		def name = result.collectNested {  it.name }
		name.containsAll(["un-nameStart-token", "neuf-token","token douze", "dix token foo"])
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find on name.xml")
	def "should find all by name containing grouped by project"(){
		when:
		def token = "token"
		def groupedByProject = true
		def result = testCaseDao.findAllByNameContaining(token, groupedByProject);
		
		then:
		result.size() == 4
		result.get(0).name == "un-nameStart-token"
		def name = result.collectNested {  it.name }
		name.containsAll(["un-nameStart-token", "neuf-token","token douze", "dix token foo"])
	}
}
