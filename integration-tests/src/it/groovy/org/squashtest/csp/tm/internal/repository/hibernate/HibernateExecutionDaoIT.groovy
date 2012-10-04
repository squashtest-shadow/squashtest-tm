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

import javax.inject.Inject;

import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;


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
		pas.pageSize >> 3
		pas.sortedAttribute >> sortedAttribute
		pas.sortOrder >> SortOrder.ASCENDING
		
		when: 
		def res = executionDao.findAllByTestCaseId(500L, pas)
		
		then:
		res*.id == expectedIds
		
		where:
		sortedAttribute             | expectedIds
		"Project.name"              | [494, 580, 627]
		"Campaign.name"             | [718, 494, 580]
		"Iteration.name"            | [718, 494, 580]
		"Execution.name"            | [494, 580, 627]
		"Execution.executionMode"   | [494, 580, 627]
		"TestSuite.name"            | [494, 580, 627]
		"Execution.executionStatus" | [494, 580, 627]
		"Execution.lastExecutedBy"  | [494, 580, 627]
		"Execution.lastExecutedOn"  | [494, 580, 627]
	}
}
