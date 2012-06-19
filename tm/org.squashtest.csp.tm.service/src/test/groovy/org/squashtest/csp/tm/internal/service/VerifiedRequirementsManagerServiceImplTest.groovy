/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service

import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.csp.tm.domain.requirement.RequirementStatus
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.domain.resource.Resource
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao
import org.squashtest.csp.tm.internal.repository.RequirementDao
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao
import org.squashtest.csp.tm.service.TestCaseModificationService
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory

import spock.lang.Specification

class VerifiedRequirementsManagerServiceImplTest extends Specification {
	VerifiedRequirementsManagerServiceImpl service = new VerifiedRequirementsManagerServiceImpl()
	TestCaseDao testCaseDao = Mock()
	RequirementLibraryDao requirementLibraryDao = Mock()
	RequirementVersionDao requirementVersionDao = Mock()
	LibraryNodeDao<RequirementLibraryNode> nodeDao = Mock()
	TestCaseImportanceManagerServiceImpl testCaseImportanceManagerService = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy = Mock()

	def setup() {
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.requirementVersionDao = requirementVersionDao
		service.requirementLibraryNodeDao = nodeDao
		service.testCaseImportanceManagerService = testCaseImportanceManagerService
	}


	def "should add requirements to test case's verified requirements"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		RequirementVersion rv5 = new RequirementVersion()
		RequirementVersion rv15 = new RequirementVersion()


		use (ReflectionCategory) {
			Resource.set field: "id", of: rv5, to: 5L
			Resource.set field: "id", of: rv15, to: 15L
		}

		requirementVersionDao.findAllByIdList([5, 15]) >> [rv5, rv15]

		and:
		Requirement req5 = new Requirement(rv5)
		Requirement req15 = new Requirement(rv15)
		nodeDao.findAllByIdList([5, 15]) >> [req5, req15]

		when:
		service.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		testCase.verifiedRequirementVersions.containsExactly([rv5, rv15])
	}

	def "should not add requirements with no verifiable version to test case's verified requirements"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		RequirementVersion rv5 = new RequirementVersion()
		RequirementVersion rv15 = new RequirementVersion()


		use (ReflectionCategory) {
			Resource.set field: "id", of: rv5, to: 5L
			RequirementVersion.set field: "status", of: rv5, to: RequirementStatus.OBSOLETE
			Resource.set field: "id", of: rv15, to: 15L
		}

		requirementVersionDao.findAllByIdList([5, 15]) >> [rv5, rv15]

		and:
		Requirement req5 = new Requirement(rv5)
		Requirement req15 = new Requirement(rv15)
		nodeDao.findAllByIdList([5, 15]) >> [req5, req15]

		when:
		service.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		testCase.verifiedRequirementVersions.containsExactly([rv15])
	}

	def "should remove requirements from test case's verified requirements"() {
		given: "some requirements"
		RequirementVersion req5 = new RequirementVersion()
		new Requirement(req5)
		RequirementVersion req15 = new RequirementVersion()
		new Requirement(req15)

		use (ReflectionCategory) {
			Resource.set field: "id", of: req5, to: 5L
			Resource.set field: "id", of: req15, to: 15L
		}
		requirementVersionDao.findAllByIdList([15]) >> [req15]

		and: "a test case which verifies these requirements"
		TestCase testCase = new TestCase()
		testCase.addVerifiedRequirementVersion req5
		testCase.addVerifiedRequirementVersion req15
		testCaseDao.findById(10) >> testCase

		when:
		service.removeVerifiedRequirementVersionsFromTestCase([15], 10)

		then:
		testCase.verifiedRequirementVersions.containsExactly([req5])
	}

	def "should remove single requirement from test case's verified requirements"() {
		given: "a requirement"
		RequirementVersion req = Mock()
		req.id >> 5
		requirementVersionDao.findById(5) >> req

		and: " a test case which verifies this requirements"
		TestCase testCase = new TestCase()
		testCase.addVerifiedRequirementVersion req
		testCaseDao.findById(10) >> testCase

		when:
		service.removeVerifiedRequirementVersionFromTestCase(5, 10)

		then:
		testCase.verifiedRequirementVersions.size() == 0
	}

	def "should return the first 2 verified requirements"() {
		given:
		PagingAndSorting filter = Mock()
		filter.getFirstItemIndex() >> 0
		filter.getPageSize() >> 2

		and:
		requirementVersionDao.findAllVerifiedByTestCase(10, filter) >> [
			Mock(Requirement),
			Mock(Requirement)
		]

		when:
		def res = service.findAllDirectlyVerifiedRequirementsByTestCaseId(10, filter)

		then:
		res.pagedItems.size() == 2
	}

	def "should tell that unfiltered result size is 5"() {
		given:
		PagingAndSorting filter = Mock()
		filter.getFirstItemIndex() >> 0
		filter.getPageSize() >> 2

		and:
		requirementVersionDao.countVerifiedByTestCase(10) >> 5

		when:
		PagedCollectionHolder res = service.findAllDirectlyVerifiedRequirementsByTestCaseId(10, filter)

		then:
		res.totalNumberOfItems == 5
	}
}
