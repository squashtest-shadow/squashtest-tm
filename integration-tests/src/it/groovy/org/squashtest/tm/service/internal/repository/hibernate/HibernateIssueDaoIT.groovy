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
package org.squashtest.tm.service.internal.repository.hibernate

import org.hibernate.SessionFactory
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.service.internal.repository.IssueDao
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Ignore
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@SuppressWarnings("GroovyUnusedDeclaration")
@UnitilsSupport
class HibernateIssueDaoIT extends DbunitDaoSpecification {
	@Inject
	IssueDao issueDao

	@Inject
	SessionFactory sessionFactory

/**
 *
 * @param ppt Map of {issue, issueListId, remoteIssueId, issueId, bugtrackerId}* @return
 */
	def expected(Map ppt) {
		assert ppt.issue.id == ppt.issueId
		assert ppt.issue.remoteIssueId == ppt.remoteIssueId
		assert ppt.issue.issueList.id == ppt.issueListId
		if (ppt.bugtrackerId != null) {
			assert ppt.issue.bugtracker.id == ppt.bugtrackerId
		}
		return true // used in "then", return true so that assertion does not fail
	}

	@DataSet("HibernateIssueDaoIT.xml")
	@Ignore("broken yet tested method will probably not be used anymore")
	def "should return sorted issues from execs/exec-steps"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]

		when:
		def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds, sorter(firstItemIndex: 1))

		then:
		result.size() == 2

		def issue1 = result[0]
		def issue2 = result[1]

		expected(issue: issue1, issueListId: 10000100L, remoteIssueId: "1000011", issueId: 100001L, bugtrackerId: 100001L)
		expected(issue: issue2, issueListId: 100001000L, remoteIssueId: "1000022", issueId: 100002L, bugtrackerId: 100001L)

	}

	PagingAndSorting sorter(Map props = [:]) {
		new PagingAndSorting() {
			@Override
			int getFirstItemIndex() {
				(props.firstItemIndex ?: 0)
			}

			@Override
			int getPageSize() {
				(props.pageSize ?: 10)
			}

			@Override
			boolean shouldDisplayAll() {
				(props.shouldDisplayAll ?: false)
			}

			@Override
			String getSortedAttribute() {
				"Issue.id"
			}

			@Override
			SortOrder getSortOrder() {
				(props.sortOrder ?: SortOrder.ASCENDING)
			}
		}
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return sorted issues from execs/exec-steps2"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]

		when:
		def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds, sorter(firstItemIndex: 1))

		then:
		result.size() == 3

		def issue1 = result[0]
		def issue2 = result[1]
		def issue3 = result[2]

		expected(issue: issue1, issueListId: 100001000L, remoteIssueId: "1000022", issueId: 100002L, bugtrackerId: 100001L)
		expected(issue: issue2, issueListId: 100001011L, remoteIssueId: "1000033", issueId: 100003L, bugtrackerId: 100001L)
		expected(issue: issue3, issueListId: 100002010L, remoteIssueId: "1000066", issueId: 100006L, bugtrackerId: 100001L)

	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should count issues for execution and execution steps"(){
		given:
		List<Long> execIds = [10000101L, 10000400L, 10000201L, 10000100L]
		List<Long> execStepIds = [100001010L, 100001011L, 100002010L, 100001000L]

		when:
		def result = issueDao.countIssuesfromExecutionAndExecutionSteps(execIds, execStepIds)

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

		when:
		def result = issueDao.findSortedIssuesFromIssuesLists(issueListIds, sorter(firstItemIndex: 1, pageSize: 7), bugTrackerId)

		then:
		result.size() == 3

		def issue1 = result[0]
		def issue2 = result[1]
		def issue3 = result[2]

		expected(issue: issue1, issueListId: 100001000L, remoteIssueId: "1000022", issueId: 100002L)
		expected(issue: issue2, issueListId: 100001011L, remoteIssueId: "1000033", issueId: 100003L)
		expected(issue: issue3, issueListId: 100002010L, remoteIssueId: "1000066", issueId: 100006L)

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

		when:
		def result = issueDao.countIssuesfromIssueList(issueListIds, bugTrackerId)

		then:
		result == 4
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return issues for iteration"(){
		given :
		def iterationId = 100001L

		when:
		def result = issueDao.findAllForIteration(iterationId)

		then :
		result.size() == 3;
		result*.id.containsAll([100001L, 100002L, 100003L]);
	}

	@DataSet("HibernateIssueDaoIT.test suite.xml")
	def "should return issues for test suite"(){
		given :
		def testSuiteId = 1000030L

		when:
		def result = issueDao.findAllForTestSuite(testSuiteId)

		then :
		result.size() == 3;
		result*.id.containsAll([100001L, 100002L, 100003L]);
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution as issue detector"(){
		given :
		def issueId = 100007L

		when:
		def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 10000400L
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "should return execution step as issue detector"(){
		given :
		def issueId = 100005L

		when:
		def result = issueDao.findIssueDetectorByIssue(issueId)

		then :
		result != null
		result.issueListId == 100002010L
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "[#6062] should return all execution - ish pairs for a campaign"() {
		given:
		def camp = sessionFactory.currentSession.load(Campaign, 100001L)

		when:
		def result = issueDao.findAllExecutionIssuePairsByCampaign(camp, sorter(firstItemIndex: 0, pageSize: 5))

		then:
		result.size() == 5
		result*.left.id as Set == [10000100L, 10000100L, 10000101L, 10000200L, 10000201L] as Set
		result*.right.id as Set == 100001..100005 as Set
	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "[#6062] should count issues for a campaign"() {
		given:
		def camp = sessionFactory.currentSession.load(Campaign, 100001L)

		expect:
		issueDao.countByCampaign(camp) == 7

	}

	@DataSet("HibernateIssueDaoIT.xml")
	def "[#6062] should return all execution - ish pairs for an execution"() {
		given:
		def exec = sessionFactory.currentSession.load(Execution, 10000100L)

		when:
		def result = issueDao.findAllExecutionIssuePairsByExecution(exec, sorter(firstItemIndex: 0, pageSize: 5))

		then:
		result.size() == 2
		result*.left.id as Set == [10000100L, 10000100L] as Set
		result*.right.id as Set == 100001..100002 as Set
}

	@DataSet("HibernateIssueDaoIT.xml")
	def "[#6062] should count issues for an execution"() {
		given:
		def exec = sessionFactory.currentSession.load(Execution, 10000100L)

		expect:
		issueDao.countByExecution(exec) == 2

	}
}
