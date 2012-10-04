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

import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.tm.internal.repository.IterationDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateIssueDaoIT extends DbunitDaoSpecification {
	@Inject IssueDao issueDao

	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should return sorted issues from execs/exec-steps"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]
		CollectionSorting sorter = new CollectionSorting() {

					@Override
					public int getFirstItemIndex() {
						return 0
					}

					@Override
					public String getSortingOrder() {
						return "asc"
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 2
					}
				}
		when: def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds,sorter)

		then:
		result.size() <= 2
		result == [[100L, "11", 1L], [1000L, "22", 1L]]
	}

	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should return sorted issues from execs/exec-steps2"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]
		CollectionSorting sorter = new CollectionSorting() {

					@Override
					public int getFirstItemIndex() {
						return 1
					}

					@Override
					public String getSortingOrder() {
						return "asc"
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 7
					}
				}
		when: def result = issueDao.findSortedIssuesFromExecutionAndExecutionSteps(execIds, execStepIds,sorter)

		then:
		result.size() <= 7
		result == [
			[1000L, "22", 1L],
			[1011L, "33", 1L],
			[2010L, "66", 1L]
		]
	}

	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should count issues for execution and execution steps"(){
		given:
		List<Long> execIds = [101L, 400L, 201L, 100L]
		List<Long> execStepIds = [1010L, 1011L, 2010L, 1000L]

		when: def result = issueDao.countIssuesfromExecutionAndExecutionSteps(execIds, execStepIds)

		then:
		result == 4
	}
	
	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should return sorted issues from issue list ids"(){
		given:
		List<Long> issueListIds = [101L, 400L, 201L, 100L, 1011L, 2010L, 1000L]
		def bugTrackerId = 1L
		CollectionSorting sorter = new CollectionSorting() {

					@Override
					public int getFirstItemIndex() {
						return 1
					}

					@Override
					public String getSortingOrder() {
						return "asc"
					}

					@Override
					public String getSortedAttribute() {
						return "Issue.id"
					}

					@Override
					public int getPageSize() {
						return 7
					}
				}
		when: def result = issueDao.findSortedIssuesFromIssuesLists (issueListIds, sorter, bugTrackerId)

		then:
		result.size() <= 7
		result == [
			[1000L, "22"],
			[1011L, "33"],
			[2010L, "66"]
		]
	}
	
	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should count issues for issue list ids and bugtracker id"(){
		given:
		List<Long> issueListIds = [101L, 400L, 201L, 100L, 1010L, 1011L, 2010L, 1000L]
		def bugTrackerId = 1L

		when: def result = issueDao.countIssuesfromIssueList(issueListIds, bugTrackerId)

		then:
		result == 4
	}
	
	
}