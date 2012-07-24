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

import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.domain.testcase.ActionTestStep
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting
import org.squashtest.csp.tm.internal.repository.RequirementDao
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.internal.repository.TestStepDao
import org.squashtest.csp.tm.service.CallStepManagerService
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;


import spock.lang.Specification

class CustomTestCaseModificationServiceImplTest extends Specification {
	CustomTestCaseModificationServiceImpl service = new CustomTestCaseModificationServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestStepDao testStepDao = Mock()
	RequirementVersionDao requirementVersionDao = Mock()
	GenericNodeManagementService testCaseManagementService = Mock()
	CallStepManagerService callStepManagerService = Mock()
	TestCaseNodeDeletionHandler deletionHandler = Mock()

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
		CollectionAssertions.declareContainsExactly()

		service.testCaseDao = testCaseDao
		service.testStepDao = testStepDao
		service.testCaseManagementService = testCaseManagementService
		service.requirementVersionDao = requirementVersionDao
		service.callStepManagerService = callStepManagerService
		service.deletionHandler = deletionHandler
	}

	def "should find test case and add a step"() {
		given:
		def testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		def step = new ActionTestStep()

		when:
		service.addActionTestStep(10, step)

		then:
		testCase.steps == [step]
		1 * testStepDao.persist(step)
	}

	def "should find test case and change its step index"() {
		given:
		TestCase testCase = new TestCase()
		testCaseDao.findById(10) >> testCase

		and:
		ActionTestStep step1 = Mock()
		step1.getId() >> 30
		testCase.steps[0] = step1

		ActionTestStep step2 = Mock()
		step2.getId() >> 5
		testCase.steps[1] = step2


		when:
		service.changeTestStepPosition(10, 5, 0)

		then:
		testCase.steps[0] == step2
	}

	def "should find test case and remove one of its steps"() {
		given:
		TestCase testCase = Mock()
		testCaseDao.findById(10) >> testCase

		and:
		ActionTestStep tstep = Mock()
		testStepDao.findById(20) >> tstep

		when:
		service.removeStepFromTestCase(10, 20)

		then:
		1 * deletionHandler.deleteStep(testCase, tstep)
	}


	def "should copy and insert a Test Step a a specific position"(){
		given:
		TestCase testCase = new TestCase()
		ActionTestStep step1 = new ActionTestStep("a","a")
		ActionTestStep step2 = new ActionTestStep("b", "b")

		and:
		testCase.addStep(step1)
		testCaseDao.findAndInit(0) >> testCase
		testStepDao.findById(0) >> step1
		testStepDao.findById(1) >> step2


		when:
		service.pasteCopiedTestStep(0, 0, 1)

		then:
		testCase.steps.get(1).action == (step2.action)
		testCase.steps.get(1).expectedResult == step2.expectedResult
	}


	def "should find directly verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with 1 verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		RequirementVersion directlyVerified = Mock()
		directlyVerified.id >> 100L

		testCase.getVerifiedRequirementVersions() >> [directlyVerified]


		and:
		requirementVersionDao.findAllVerifiedByTestCases({ [10L].containsAll(it) }, _) >> [directlyVerified]


		and : "the looked up test case calls no test case"
		callStepManagerService.getTestCaseCallTree(_) >> []


		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10L, sorting)

		then:
		verifieds.pagedItems.containsExactlyIds([100L])
		verifieds.pagedItems.collect { it.directVerification } == [true]
	}

	def "should find 1st level indirectly verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with no verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getVerifiedRequirementVersions() >> []


		and : "the looked up test case calls a test case"
		long callee = 20L
		callStepManagerService.getTestCaseCallTree(_) >> [callee]


		and: "the callee verifies a requiremnt"
		RequirementVersion verified = Mock()
		verified.id >> 100L
		requirementVersionDao.findAllVerifiedByTestCases({[10L, 20L].containsAll(it) }, _) >> [verified]



		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.pagedItems.containsExactlyIds([100L])
		verifieds.pagedItems.collect { it.directVerification } == [false]
	}



	def "should find 2nd level indirectly verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case with no verified requirement"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getVerifiedRequirementVersions() >> []



		and : "the looked up test case calls a test case that calls a test case (L2)"
		long firstLevelCallee = 20L
		long secondLevelCallee = 30L
		callStepManagerService.getTestCaseCallTree(_) >> [
			firstLevelCallee,
			secondLevelCallee
		]



		and: "the L2 callee verifies a requiremnt"
		RequirementVersion verified = Mock()
		verified.id >> 100L
		requirementVersionDao.findAllVerifiedByTestCases({[10L, 20L, 30L].containsAll(it) }, _) >> [verified]

		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.pagedItems.containsExactlyIds([100L])
		verifieds.pagedItems.collect { it.directVerification } == [false]
	}



	def "should count verified requiremnts in verified list"() {
		given: "sorting directives"
		PagingAndSorting sorting = Mock()

		and: "the looked up test case"
		TestCase testCase = Mock()
		testCaseDao.findById(10L) >> testCase

		testCase.getVerifiedRequirementVersions() >> []

		and: "the looked up test case calls no test case"
		callStepManagerService.getTestCaseCallTree(10L) >> []

		and:
		requirementVersionDao.findAllVerifiedByTestCases({ [10L].containsAll(it) }, _) >> []

		and:
		requirementVersionDao.countVerifiedByTestCases({ [10L].containsAll(it) }) >> 666
		when:
		PagedCollectionHolder verifieds = service.findAllVerifiedRequirementsByTestCaseId(10, sorting)

		then:
		verifieds.totalNumberOfItems == 666
	}
}
