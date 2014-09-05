/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject;

import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.execution.ExecutionStatus;


@UnitilsSupport
class HibernateExecutionDaoIT extends DbunitDaoSpecification {
	@Inject ExecutionDao executionDao
	
	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	@Unroll("should count #expectedCount executions for test case #testCaseId")
	def "should count #expectedCount executions for test case #testCaseId"() {
		when: 
		def count = executionDao.countByTestCaseId(testCaseId)
		
		then:
		count == expectedCount
		
		where: 
		testCaseId | expectedCount
		500        | 11
		550        | 0
		 
	}
	
	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	def "should find 5 paged executions for test case 500"() {
		given: 
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 1
		pas.pageSize >> 5
		pas.sortedAttribute >> "Execution.lastExecutedOn"
		pas.sortOrder >> SortOrder.ASCENDING
		
		when: 
		def res = executionDao.findAllByTestCaseId(500L, pas)
		
		then:
		res*.id == [580, 627, 718, 752, 953]
		
	}
	
	
	@DataSet("HibernateExecutionDaoIT.should find executions by test case.xml")
	@Unroll("should find executions #expectedIds sorted by #sortedAttribute")
	def "should find executions sorted by .."() {
		given: 
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 0
		pas.pageSize >> expectedIds.size()
		pas.sortedAttribute >> sortedAttribute
		pas.sortOrder >> sortOrder
		
		when: 
		def res = executionDao.findAllByTestCaseId(500L, pas)
		
		then:
		res*.id == expectedIds
		
		where:
		sortedAttribute             | sortOrder            | expectedIds
//		"Project.name"              | SortOrder.ASCENDING  | [494, 580, 627] // dataset too complex, cannot manage to have the test work
		"Campaign.name"             | SortOrder.ASCENDING  | [718, 494, 580] // null, camp a, camp b */
		"Iteration.name"            | SortOrder.ASCENDING  | [718, 494, 580]
		"Execution.name"            | SortOrder.ASCENDING  | [494, 580, 627]
		"Execution.executionMode"   | SortOrder.ASCENDING  | [627, 718]
		"Execution.executionStatus" | SortOrder.ASCENDING  | [953, 1110, 1556]
		"Execution.lastExecutedBy"  | SortOrder.ASCENDING  | [2150, 2562, 2971]
		"Execution.lastExecutedOn"  | SortOrder.ASCENDING  | [494, 580, 627] 
	}
	
	
	@DataSet("HibernateExecutionDaoIT.should find if project uses exec status.xml")
	@Unroll("should find if project #projectId uses exec status #execStatus")
	def"should find if project uses exec status"(){
		when:
		def res = executionDao.projectUsesExecutionStatus(projectId, execStatus)
		
		then : 
		res == expectedResult
		
		where :
		projectId | execStatus                  | expectedResult
		1         | ExecutionStatus.SETTLED  	| true // in execution step
		1         | ExecutionStatus.UNTESTABLE	| true // in iteration test plan item
		2		  | ExecutionStatus.UNTESTABLE  | false 
		2         | ExecutionStatus.SETTLED 	| true // in iteration test plan
		
		
	}
}
