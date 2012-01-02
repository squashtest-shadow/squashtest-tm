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



import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.service.VerifyingTestCaseManagerServiceImpl;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;

import spock.lang.Specification;

class VerifiedTestCasesManagerServiceImplTest extends Specification {

	VerifyingTestCaseManagerService service = new VerifyingTestCaseManagerServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	RequirementDao requirementDao = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy = Mock()
	LibraryNodeDao<TestCaseLibraryNode> nodeDao = Mock();

	def setup() {
		service.testCaseDao = testCaseDao
		service.testCaseLibraryDao = testCaseLibraryDao
		service.requirementDao = requirementDao
		service.projectFilterModificationService = projectFilterModificationService
		service.libraryStrategy = libraryStrategy
		service.testCaseLibraryNodeDao = nodeDao;
	}

	def "should find requirement by id"() {
		given:
		Requirement requirement = Mock()
		requirementDao.findById(10L) >> requirement

		when:
		def res = service.findRequirement(10L)

		then:
		res == requirement
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
		Requirement requirement = new Requirement()
		requirementDao.findById(10) >> requirement

		and:
		TestCase req5 = new TestCase()
		req5.id >> 5
		TestCase req15 = new TestCase()
		req5.id >> 15
		nodeDao.findAllById([5, 15]) >> [req5, req15]

		when:
		service.addVerifyingTestCasesToRequirement([5, 15], 10)

		then:
		requirement.getVerifyingTestCase().containsAll([req5, req15])
		[req5, req15].containsAll(requirement.getVerifyingTestCase())
	}

	def "should remove requirements from test case's verified requirements"() {
		given: "some requirements"
		TestCase req5 = new TestCase()
		req5.id >> 5
		TestCase req15 = new TestCase()
		req15.id >> 15
		testCaseDao.findAllByIdList([15]) >> [req15]

		and: " a test case which verifies these requirements"
		Requirement requirement = new Requirement()
		requirement.addVerifyingTestCase req5
		requirement.addVerifyingTestCase req15
		requirementDao.findById(10) >> requirement

		when:
		service.removeVerifyingTestCasesFromRequirement([15], 10)

		then:
		requirement.getVerifyingTestCase().containsAll([req5])
		[req5].containsAll(requirement.getVerifyingTestCase())
		print req5
	}

	def "should remove single requirement from test case's verified requirements"() {
		given: "a requirement"
		TestCase tq = new TestCase()
		tq.id >> 5
		testCaseDao.findById(5) >> tq

		and: " a test case which verifies this requirements"
		Requirement requirement = new Requirement()
		requirement.id >> 10
		requirement.addVerifyingTestCase tq
		requirementDao.findById(10) >> requirement

		when:
		service.removeVerifyingTestCaseFromRequirement(10, 5)

		then:
		requirement.getVerifyingTestCase().size() == 0
	}
}
