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
import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy
import org.squashtest.tm.service.internal.project.ProjectFilterModificationServiceImpl;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.testcase.TestCaseImportanceManagerServiceImpl;
import org.squashtest.tm.service.internal.testcase.VerifyingTestCaseManagerServiceImpl;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

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

	def setup() {
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.testCaseLibraryDao = testCaseLibraryDao
		service.requirementVersionDao = requirementVersionDao
		service.projectFilterModificationService = projectFilterModificationService
		service.libraryStrategy = libraryStrategy
		service.testCaseLibraryNodeDao = testCaseLibraryNodeDao
		service.testCaseImportanceManagerService = testCaseImportanceServiceImpl
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
		tc5.id >> 5
		TestCase tc15 = new TestCase()
		tc15.id >> 15
		testCaseDao.findAllByIds([15]) >> [tc15]

		and: " a test case which verifies these requirements"
		RequirementVersion rv = new RequirementVersion()
		rv.addVerifyingTestCase tc5
		rv.addVerifyingTestCase tc15
		requirementVersionDao.findById(10) >> rv

		when:
		service.removeVerifyingTestCasesFromRequirementVersion([15], 10)

		then:
		rv.verifyingTestCases.containsExactly([tc5])
	}

	def "should remove single requirement from test case's verified requirements"() {
		given: "a requirement"
		TestCase tq = new TestCase()
		tq.id >> 5
		testCaseDao.findById(5) >> tq

		and: " a test case which verifies this requirements"
		RequirementVersion rv = new RequirementVersion()
		rv.id >> 10
		rv.addVerifyingTestCase tq
		requirementVersionDao.findById(10) >> rv

		when:
		service.removeVerifyingTestCaseFromRequirementVersion(5, 10)

		then:
		rv.verifyingTestCases.size() == 0
	}
}
