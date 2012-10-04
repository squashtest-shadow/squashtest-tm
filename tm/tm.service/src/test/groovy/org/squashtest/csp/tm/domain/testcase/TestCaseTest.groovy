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
package org.squashtest.csp.tm.domain.testcase

import javax.swing.plaf.basic.BasicFileChooserUI.ApproveSelectionAction

import org.apache.commons.lang.NullArgumentException
import org.squashtest.csp.tm.domain.RequirementAlreadyVerifiedException
import org.squashtest.csp.tm.domain.RequirementVersionNotLinkableException
import org.squashtest.csp.tm.domain.UnknownEntityException
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.domain.requirement.RequirementStatus
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory

import spock.lang.Specification
import spock.lang.Unroll

class TestCaseTest extends Specification {
	TestCase testCase = new TestCase()

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}

	def "should add a step at the end of the list"() {
		given:
		testCase.steps << new ActionTestStep(action: "1")
		testCase.steps << new ActionTestStep(action: "2")

		and:
		def newStep = new ActionTestStep(action: "3")

		when:
		testCase.addStep(newStep)

		then:
		testCase.steps[2] == newStep
	}

	def "should not add a null step"() {
		when:
		testCase.addStep(null)

		then:
		thrown(NullArgumentException)
	}

	def "should move step from given index to a greater index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep( action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(1, 3)

		then:
		testCase.steps == [step0, step2, step3, step1]
	}

	def "should move step from given index to a lesser index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(2, 0)

		then:
		testCase.steps == [step2, step0, step1, step3]
	}

	def "should move a list of steps to a lesser index"(){

		given :
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")


		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3


		def tomove = [step2, step3]
		def position = 1
		def result = [step0, step2, step3, step1]


		when :

		testCase.moveSteps(position, tomove)

		then :
		testCase.steps.collect{ it.action } == result.collect{ it.action }
	}



	def "should move a list of steps to a last position"(){

		given :
		def step0 = new ActionTestStep( action:"0")
		def step1 = new ActionTestStep( action:"1")
		def step2 = new ActionTestStep( action:"2")
		def step3 = new ActionTestStep( action:"3")


		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3


		def tomove = [step0, step1]
		def position = 2
		def result = [step2, step3, step0, step1]

		when :
		testCase.moveSteps(position, tomove)

		then :
		testCase.steps.collect{ it.action } == result.collect{ it.action }
	}

	def "should add a verified requirement"() {
		given:
		RequirementVersion r = new RequirementVersion()

		when:
		testCase.addVerifiedRequirementVersion(r)

		then:
		testCase.verifiedRequirementVersions.contains r
	}

	def "should remove a verified requirement"() {
		given:
		RequirementVersion r = new RequirementVersion()
		use (ReflectionCategory) {
			TestCase.set field: "verifiedRequirementVersions", of: testCase, to: ([r]as Set)
		}

		when:
		testCase.removeVerifiedRequirementVersion(r)

		then:
		!testCase.verifiedRequirementVersions.contains(r)
	}

	def "should return position of step"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		TestStep step20 = Mock()
		step20.id >> 20
		testCase.steps << step20


		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		pos == 1
	}

	def "should throw exception when position of unknown step is asked"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		thrown(UnknownEntityException)
	}

	@Unroll("copy of test case should have the same '#propName' property")
	def "copy of a test case should have the same simple properties"() {
		given:
		TestCase source = new TestCase()
		source[propName] = propValue

		when:
		def copy = source.createPastableCopy()

		then:
		copy[propName] == source[propName]

		where:
		propName        | propValue
		"prerequisite"  | "foobarfoo"
		"name"          | "foo"
		"description"   | "bar"
		"executionMode" | TestCaseExecutionMode.AUTOMATED
		"importance"    | TestCaseImportance.HIGH
		"reference"     | "barfoo"
	}

	def "copy of a test case should have the same steps"() {
		given:
		TestCase source = new TestCase()
		ActionTestStep sourceStep = new ActionTestStep(action: "fingerpoke opponent", expectedResult: "win the belt")
		source.steps << sourceStep

		when:
		def copy = source.createPastableCopy()

		then:
		copy.steps.size() == 1
		copy.steps[0].action == sourceStep.action
		copy.steps[0].expectedResult == sourceStep.expectedResult
		!copy.steps[0].is(sourceStep)
	}

	def "copy of a test case should verify the same requirements"() {
		given:
		TestCase source = new TestCase()
		RequirementVersion req = new RequirementVersion(name: "")
		source.addVerifiedRequirementVersion req

		when:
		def copy = source.createPastableCopy()

		then:
		copy.verifiedRequirementVersions == source.verifiedRequirementVersions
	}

	def "when verifying a requirement, the requirement should also be verified by the test case"() {
		given:
		TestCase tc = new TestCase()

		and:
		RequirementVersion req = new RequirementVersion()

		when:
		tc.addVerifiedRequirementVersion req

		then:
		req.verifyingTestCases.contains tc
	}

	def "should not be able to verify an obsolete requirement"() {
		given:
		TestCase tc = new TestCase()

		and:
		RequirementVersion req = new RequirementVersion(status: RequirementStatus.OBSOLETE)

		when:
		tc.addVerifiedRequirementVersion req

		then:
		thrown(RequirementVersionNotLinkableException)
	}

	def "when unverifying a requirement, the requirement should also not be verified by the test case"() {
		given:
		TestCase tc = new TestCase()

		and:
		RequirementVersion req = new RequirementVersion()
		use (ReflectionCategory) {
			RequirementVersion.set field: "verifyingTestCases", of: req, to: [tc]as Set
			TestCase.set field: "verifiedRequirementVersions", of: tc, to: [req]as Set
		}

		when:
		tc.removeVerifiedRequirementVersion req

		then:
		!req.verifyingTestCases.contains(tc)
	}

	//	def "should not be able to unverify an obsolete requirement"() {
	//		given:
	//		TestCase tc = new TestCase()
	//
	//		and:
	//		RequirementVersion req = new RequirementVersion(status: RequirementStatus.OBSOLETE)
	//		use (ReflectionCategory) {
	//			RequirementVersion.set field: "verifyingTestCases", of: req, to: [tc]as Set
	//			TestCase.set field: "verifiedRequirementVersions", of: tc, to: [req]as Set
	//		}
	//
	//		when:
	//		tc.removeVerifiedRequirementVersion req
	//
	//		then:
	//		thrown(RequirementVersionNotLinkableException)
	//	}

	def "should not verify 2 versions of same requirement"() {
		given:
		Requirement req = new Requirement(new RequirementVersion())
		testCase.addVerifiedRequirementVersion(req.currentVersion)

		and:
		req.increaseVersion()

		when:
		testCase.addVerifiedRequirementVersion(req.currentVersion)

		then:
		thrown(RequirementAlreadyVerifiedException)
	}

	def "should verify the default verifiable version"() {
		given:
		Requirement req = Mock()
		RequirementVersion verifiableVersion = Mock()
		req.defaultVerifiableVersion >> verifiableVersion

		when:
		testCase.addVerifiedRequirement(req)

		then:
		testCase.verifiedRequirementVersions.containsExactly([verifiableVersion])
	}
}
