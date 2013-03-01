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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.hibernate.Query
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.csp.tools.unittest.assertions.ListAssertions
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateRequirementVersionDaoIT extends DbunitDaoSpecification {
	@Inject RequirementVersionDao versionDao
	@Inject RequirementDeletionDao deletionDao

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		ListAssertions.declareIdsEqual()
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by id"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortOrder >> SortOrder.ASCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.id } == [10L, 20L, 30L]
	}
	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by desc name"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.name"
		sorting.sortOrder >> SortOrder.DESCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.name } == ["vingt", "30", "10"]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find paged list of requirements verified by list of test cases"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 1
		sorting.pageSize >> 1
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortOrder >> SortOrder.ASCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.id } == [20L]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should count requirements verified by list of test cases"() {
		when:
		def count = versionDao.countVerifiedByTestCases([100L, 200L])

		then:
		count == 3
	}

	@DataSet("HibernateRequirementVersionDaoIT.should find all requirements versions by id.xml")
	def "should find all requirements versions by id"() {
		when:
		def res = versionDao.findAllByIds([10L, 20L])

		then:
		res.containsExactlyIds([10L, 20L])
	}

	@DataSet("HibernateTestCaseDaoIT.should find requirement versions directly verified by a test case sorted by name.xml")
	def "should find requirement versions directly verified by a test case sorted by name"() {
		given:
		PagingAndSorting pas = Mock()
		pas.firstItemIndex >> 0
		pas.pageSize >> 10
		pas.sortedAttribute >> "RequirementVersion.name"
		pas.sortOrder >> SortOrder.ASCENDING

		when:
		def res = versionDao.findAllVerifiedByTestCase(100, pas)

		then:
		res.idsEqual([20L, 10L])
	}

	@DataSet("HibernateTestCaseDaoIT.should find requirement versions directly verified by a test case sorted by name.xml")
	def "should count requirements verified by a test case"() {
		when:
		def count = versionDao.countVerifiedByTestCase(100)

		then:
		count == 2
	}

	@DataSet("HibernateTestCaseDaoIT.should count versions of requirement.xml")
	def "should count versions of requirement"() {
		expect:
		versionDao.countByRequirement(1) == 2
	}

	@DataSet("HibernateTestCaseDaoIT.should count versions of requirement.xml")
	def "should find versions of requirement"() {
		given:
		PagingAndSorting paging = Mock()
		paging.firstItemIndex >> 1
		paging.pageSize >> 1
		paging.sortedAttribute >> "RequirementVersion.versionNumber"
		paging.sortOrder >> SortOrder.ASCENDING

		when:
		def res = versionDao.findAllByRequirement(1, paging)

		then:
		res*.id == [20L]
	}
}
