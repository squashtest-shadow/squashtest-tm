/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.service.internal.repository.IssueDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateIssueDaoIT extends DbunitDaoSpecification {
	@Inject IssueDao issueDao

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from execs/exec-steps"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]
		PagingAndSorting sorter = new PagingAndSorting() {

					@Override
					public int getFirstItemIndex() {
						return 0
					}

					@Override
					public SortOrder getSortOrder() {
						return SortOrder.ASCENDING
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 2
					}

					@Override
					public boolean shouldDisplayAll() {
						return false;
					}
				}
		when: def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds,sorter)

		then:
		result.size() <= 2
		result == [
			[100L, "11", 1L, 1L],
			[1000L, "22", 2L, 1L]
		]
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from execs/exec-steps2"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]
		PagingAndSorting sorter = new PagingAndSorting() {

					@Override
					public int getFirstItemIndex() {
						return 1
					}

					@Override
					public SortOrder getSortOrder() {
						return SortOrder.ASCENDING
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 7
					}

					boolean shouldDisplayAll() {
						return false
					};
				}
		when: def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds,sorter)

		then:
		result.size() <= 7
		result == [
			[1000L, "22", 2L, 1L],
			[1011L, "33", 3L, 1L],
			[2010L, "66", 6L, 1L]
		]
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should count issues for execution and execution steps"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]

		when: def result = issueDao.countIssuesfromExecutionAndExecutionSteps(execIds, execStepIds)

		then:
		result == 4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from issue list ids"(){
		given:
		List<Long> issueListIds = [
			101L,
			400L,
			201L,
			100L,
			1011L,
			2010L,
			1000L
		]
		def bugTrackerId = 1L
		PagingAndSorting sorter = new PagingAndSorting() {

					@Override
					public int getFirstItemIndex() {
						return 1
					}

					@Override
					public SortOrder getSortOrder() {
						return SortOrder.ASCENDING
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 7
					}

					boolean shouldDisplayAll(){
						return false;
					}
				}
		when: def result = issueDao.findSortedIssuesFromIssuesLists (issueListIds, sorter, bugTrackerId)

		then:
		result.size() <= 7
		result == [
			[1000L, "22", 2L],
			[1011L, "33", 3L],
			[2010L, "66", 6L]
		]
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should count issues for issue list ids and bugtracker id"(){
		given:
		List<Long> issueListIds = [
			101L,
			400L,
			201L,
			100L,
			1010L,
			1011L,
			2010L,
			1000L
		]
		def bugTrackerId = 1L

		when: def result = issueDao.countIssuesfromIssueList(issueListIds, bugTrackerId)

		then:
		result == 4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return issues for iteration"(){
		given :
		def iterationId = 1L

		when : def result = issueDao.findAllForIteration(iterationId)

		then :
		result.size() == 3;
		result.collect({it.id}).containsAll([1L, 2L, 3L]);
	}

	@DataSet("HibernateIssueDaoIT.test suite.xml")
	def "should return issues for test suite"(){
		given :
		def testSuiteId = 30L

		when : def result = issueDao.findAllForTestSuite(testSuiteId)

		then :
		result.size() == 3;
		result.collect({it.id}).containsAll([1L, 2L, 3L]);
	}
	
	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution as issue detector"(){
		given :
		def issueId = 7L

		when : def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 400L
	}
	
	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution step as issue detector"(){
		given :
		def issueId = 5L

		when : def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 2010L
	}
}