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
package org.squashtest.csp.tm.internal.service

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy
import org.squashtest.tm.service.internal.project.ProjectFilterModificationServiceImpl
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.testcase.TestCaseImportanceManagerServiceImpl
import org.squashtest.tm.service.internal.testcase.VerifyingTestCaseManagerServiceImpl
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService

import spock.lang.Specification


class VerifiedTestCasesManagerServiceImplTest extends Specification {

	VerifyingTestCaseManagerService service = new VerifyingTestCaseManagerServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	RequirementVersionDao requirementVersionDao = Mock()
	LibraryNodeDao testCaseLibraryNodeDao = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy = Mock()
	LibraryNodeDao<TestCaseLibraryNode> nodeDao = Mock()
	TestCaseImportanceManagerServiceImpl testCaseImportanceServiceImpl = Mock()
	RequirementVersionCoverageDao requirementVersionCoverageDao = Mock()

	def setup() {
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.testCaseLibraryDao = testCaseLibraryDao
		service.requirementVersionDao = requirementVersionDao
		service.projectFilterModificationService = projectFilterModificationService
		service.libraryStrategy = libraryStrategy
		service.testCaseLibraryNodeDao = testCaseLibraryNodeDao
		service.testCaseImportanceManagerService = testCaseImportanceServiceImpl
		service.requirementVersionCoverageDao = requirementVersionCoverageDao
	}

	def "should find libraries of linkable test Case"() {
		given:
		TestCaseLibrary lib = Mock()
		ProjectFilter pf = new ProjectFilter();
		pf.setActivated(false)
		projectFilterModificationService.findProjectFilterByUserLogin() >> pf
		testCaseLibraryDao.findAll() >> [lib]

		when:
		def res =
		service.findLinkableTestCaseLibraries()

		then:
		res == [lib]
	}

	def "should add TestCase to test case's verified requirements"() {
		given:
		RequirementVersion requirementVersion = new RequirementVersion()
		requirementVersionDao.findById(10) >> requirementVersion

		and:
		TestCase tc5 = new TestCase()
		tc5.id >> 5
		TestCase tc15 = new TestCase()
		tc5.id >> 15
		testCaseLibraryNodeDao.findAllByIds([5, 15]) >> [tc5, tc15]

		when:
		def rejected = service.addVerifyingTestCasesToRequirementVersion([5, 15], 10)

		then:
		requirementVersion.verifyingTestCases.containsExactly([tc5, tc15])
		rejected == []
	}

	def "should not add TestCase to test case's verified requirements"() {
		given:
		RequirementVersion requirementVersion = new RequirementVersion()
		requirementVersionDao.findById(10) >> requirementVersion

		and:
		TestCase tc5 = new TestCase()
		tc5.id >> 5
		testCaseLibraryNodeDao.findAllByIds([5]) >> [tc5]

		and:
		def requirement = new Requirement(requirementVersion)
		requirement.increaseVersion()
		def verifiedVersion = requirement.currentVersion
		tc5.addVerifiedRequirementVersion verifiedVersion

		when:
		def rejected = service.addVerifyingTestCasesToRequirementVersion([5], 10)

		then:
		requirementVersion.verifyingTestCases.containsExactly([])
		rejected*.verifyingTestCase == [tc5]
	}

	def "should remove requirements from test case's verified requirements"() {
		given: "some requirements"
		TestCase tc5 = new TestCase()
		tc5.id >> 5L
		TestCase tc15 = new TestCase()
		tc15.id >> 15L
		testCaseDao.findAllByIds([15L]) >> [tc15]
		and: " a test case which verifies these requirements"
		RequirementVersion rv = new RequirementVersion()
		RequirementVersionCoverage rvc5 = rv.addVerifyingTestCase tc5
		RequirementVersionCoverage rvc15 = rv.addVerifyingTestCase tc15
		requirementVersionDao.findById(10L) >> rv
		requirementVersionCoverageDao.findAllByRequirementVersionAndTestCases([15L], 10L)>>[rvc15]
		when:
		service.removeVerifyingTestCasesFromRequirementVersion([15], 10)

		then:
		rv.verifyingTestCases.containsExactly([tc5])
	}

	def "should remove single requirement from test case's verified requirements"() {
		given: "a requirement"
		TestCase tq = new TestCase()
		tq.id >> 5L
		testCaseDao.findById(5L) >> tq

		and: " a test case which verifies this requirements"
		RequirementVersion rv = new RequirementVersion()
		rv.id >> 10L
		RequirementVersionCoverage rvc = rv.addVerifyingTestCase tq
		requirementVersionDao.findById(10L) >> rv
		requirementVersionCoverageDao.findByRequirementVersionAndTestCase(10L, 5L)>>rvc
		when:
		service.removeVerifyingTestCaseFromRequirementVersion(5L, 10L)

		then:
		rv.verifyingTestCases.size() == 0
		tq.verifiedRequirementVersions.size() == 0
	}
	def "should not be able to unverify an obsolete requirement"() {
		given:
		TestCase tc = new TestCase()
		tc.id >> 5L
		testCaseDao.findById(5L) >> tc
		and:
		RequirementVersion req = new RequirementVersion(status: RequirementStatus.OBSOLETE)
		req.id >> 10L
		requirementVersionDao.findById(10L) >> req
		RequirementVersionCoverage rvc = new RequirementVersionCoverage();
		rvc.setVerifiedRequirementVersion(req)
		rvc.setVerifyingTestCase(tc)
		use (ReflectionCategory) {
			RequirementVersion.set field: "requirementVersionCoverages", of: req, to: [rvc]as Set
			TestCase.set field: "requirementVersionCoverages", of: tc, to: [rvc]as Set
		}
		requirementVersionCoverageDao.findByRequirementVersionAndTestCase(10L, 5L)>>rvc

		when:
		service.removeVerifyingTestCaseFromRequirementVersion(5L, 10L)

		then:
		thrown(RequirementVersionNotLinkableException)
	}
}
