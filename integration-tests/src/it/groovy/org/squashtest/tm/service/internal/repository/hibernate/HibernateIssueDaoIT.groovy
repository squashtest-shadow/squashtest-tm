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


	def expected(issue, ppt){
		assert issue.issueList.id == ppt[0]
		assert issue.remoteIssueId == ppt[1]
		assert issue.id == ppt[2]
		if (ppt[3] != null) {
			assert issue.bugtracker.id == ppt[3]
		}
		true
		//		def b1 = issue.issueList.id = ppt[0]
		//		def b2 = issue.remoteIssueId == ppt[1]
		//		def b3 = issue.id == ppt[2]
		//		def b4 = (ppt[3] != null) ? (issue.bugtracker.id == ppt[3]) : true
		//		return b1 && b2 && b3 && b4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from execs/exec-steps"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]
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

		def issue1 = result[0]
		def issue2 = result[1]

		expected (issue1, [10000100L, "1000011", 100001L, 100001L])
		expected (issue2, [100001000L, "1000022", 100002L, 100001L])

	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from execs/exec-steps2"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]
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
		result.size() == 3

		def issue1 = result[0]
		def issue2 = result[1]
		def issue3 = result[2]

		expected (issue1, [100001000L, "1000022", 100002L, 100001L])
		expected (issue2, [100001011L, "1000033", 100003L, 100001L])
		expected (issue3, [100002010L, "1000066", 100006L, 100001L])

	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should count issues for execution and execution steps"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]

		when: def result = issueDao.countIssuesfromExecutionAndExecutionSteps(execIds, execStepIds)

		then:
		result == 4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from issue list ids"(){
		given:
		List<Long> issueListIds = [
			10000101L,
			10000400L,
			10000201L,
			10000100L,
			100001011L,
			100002010L,
			100001000L
		]
		def bugTrackerId = 100001L
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
		result.size() == 3

		def issue1 = result[0]
		def issue2 = result[1]
		def issue3 = result[2]

		expected (issue1, [100001000L, "1000022", 100002L])
		expected (issue2, [100001011L, "1000033", 100003L])
		expected (issue3, [100002010L, "1000066", 100006L])

	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should count issues for issue list ids and bugtracker id"(){
		given:
		List<Long> issueListIds = [
			10000101L,
			10000400L,
			10000201L,
			10000100L,
			100001010L,
			100001011L,
			100002010L,
			100001000L
		]
		def bugTrackerId = 100001L

		when: def result = issueDao.countIssuesfromIssueList(issueListIds, bugTrackerId)

		then:
		result == 4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return issues for iteration"(){
		given :
		def iterationId = 100001L

		when : def result = issueDao.findAllForIteration(iterationId)

		then :
		result.size() == 3;
		result*.id.containsAll([100001L, 100002L, 100003L]);
	}

	@DataSet("HibernateIssueDaoIT.test suite.xml")
	def "should return issues for test suite"(){
		given :
		def testSuiteId = 1000030L

		when : def result = issueDao.findAllForTestSuite(testSuiteId)

		then :
		result.size() == 3;
		result*.id.containsAll([100001L, 100002L, 100003L]);
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution as issue detector"(){
		given :
		def issueId = 100007L

		when : def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 10000400L
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution step as issue detector"(){
		given :
		def issueId = 100005L

		when : def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 100002010L
	}
}