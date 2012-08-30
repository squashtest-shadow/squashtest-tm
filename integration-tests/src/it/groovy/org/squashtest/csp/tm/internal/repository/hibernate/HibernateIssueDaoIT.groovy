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
	def "should return sorted issues"(){
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
		result == [["11",100L], ["22", 1000L]]
	}
	
	@DataSet("HibernateIssueDaoIT.should return sorted issues.xml")
	def "should return sorted issues 2"(){
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
		result == [ ["22", 1000L ], ["33", 1011L ], ["66", 2010L]]
	}
}	