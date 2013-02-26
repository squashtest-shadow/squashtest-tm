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
package org.squashtest.tm.domain.requirement

import static RequirementStatus.*

import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class RequirementVersionTest extends Specification {
	RequirementVersion requirementVersion = new RequirementVersion(name:"req", description:"this is a req");

	def setup() {
		CollectionAssertions.declareContainsExactly()
	}

	@Unroll("should allow modification of property '#property' for status WORK_IN_PROGRESS")
	def "should allow modification for status WORK_IN_PROGRESS"(){
		given :
		requirementVersion.setStatus(WORK_IN_PROGRESS)

		when :
		requirementVersion[property] = valueToSet

		then :
		notThrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}

	@Unroll("should allow modification of property '#property' for status UNDER_REVIEW")
	def "should allow modification for status UNDER_REVIEW"(){

		given :
		requirementVersion.setStatus(UNDER_REVIEW)

		when :
		requirementVersion[property] = valueToSet

		then :
		notThrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}


	@Unroll("should not allow modification of property '#property' for status APPROVED")
	def "should not allow modification for status APPROVED"(){

		given :
		requirementVersion.setStatus(UNDER_REVIEW) //needed because of the workflow
		requirementVersion.setStatus(APPROVED)

		when :
		requirementVersion[property] = valueToSet

		then :
		thrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR

	}


	@Unroll("should not allow modification of property '#property' for status OBSOLETE")
	def "should not allow modification for status OBSOLETE"(){

		given :
		requirementVersion.setStatus(OBSOLETE)

		when :
		requirementVersion[property] = valueToSet

		then :
		thrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}

	@Unroll("should allow verification of a test case for #status ")
	def "non obsolete requirements should allow verification of a test case"() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersion requirementVersion = prepareRequirement(status)
		when :
		requirementVersion.addVerifyingTestCase(tc)
		then :
		notThrown(IllegalRequirementModificationException)

		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}

	@Unroll("should allow removal of a test case for #status ")
	def "non obsolete requirements should allow removal of a test case "() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersion requirementVersion = prepareRequirement(status, tc)
		when :
		requirementVersion.removeVerifyingTestCase(tc)
		then :
		notThrown(IllegalRequirementModificationException)

		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}

	def "obsolete requirements should not allow verification of a test case"() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersion requirementVersion = prepareRequirement(OBSOLETE)

		when :
		requirementVersion.addVerifyingTestCase(tc)

		then :
		thrown(RequirementVersionNotLinkableException)
	}

//	def "obsolete requirements should not allow removal of a test case "() {
//		given :
//		def tc = new TestCase(name:"tc", description:"tc")
//		RequirementVersion requirementVersion = prepareRequirement(OBSOLETE, tc)
//
//		when :
//		requirementVersion.removeVerifyingTestCase(tc)
//
//		then :
//		thrown(RequirementVersionNotLinkableException)
//	}

	@Unroll("should allow status change when current status is #status")
	def "should allow status change"() {
		given :
		def req = prepareRequirement(status)
		when :
		req.setStatus(status)
		then :
		notThrown(IllegalRequirementModificationException)
		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}

	def "should not allow status change for OBSOLETE"(){
		given :
		def req = prepareRequirement(OBSOLETE)
		when :
		req.setStatus(OBSOLETE)
		then :
		thrown(IllegalRequirementModificationException)
	}


	@Unroll("the following workflow transition for #status are legal : #availableStatuses")
	def "check workflow legal"(){

		when :
		def arrayResult = [];
		for (tester in availableStatuses){
			def req = prepareRequirement(status)
			req.setStatus(tester)
			arrayResult << req.getStatus()
		}

		then :
		arrayResult  == availableStatuses

		where :
		status				|	availableStatuses
		WORK_IN_PROGRESS  	|	[
			OBSOLETE,
			WORK_IN_PROGRESS,
			UNDER_REVIEW
		]
		UNDER_REVIEW		|	[
			OBSOLETE,
			UNDER_REVIEW,
			APPROVED,
			WORK_IN_PROGRESS
		]
		APPROVED			|	[
			OBSOLETE,
			APPROVED,
			UNDER_REVIEW,
			WORK_IN_PROGRESS
		]
	}

	@Unroll("the following workflow transition for #status are not legal : #illegalStatuses")
	def "check workflow illegal"(){

		when :
		def arrayResult = [];
		for (tester in illegalStatuses){
			def req = prepareRequirement(status)
			try{
				req.setStatus(tester)
			}catch(IllegalRequirementModificationException){
				arrayResult << tester
			}
		}

		then :
		arrayResult  == illegalStatuses

		where :
		status				|	illegalStatuses
		WORK_IN_PROGRESS  	|	[APPROVED]
		UNDER_REVIEW		|	[]
		APPROVED			|	[]
		OBSOLETE			|	[
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED,
			OBSOLETE
		]
	}

	//that (naive) method builds requirements with initial status that could bypass the workflow.
	private RequirementVersion prepareRequirement(RequirementStatus status){
		def req = new RequirementVersion(name:"req", description:"this is a req");

		for (iterStatus in RequirementStatus.values()) {
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return req;
	}

	//same
	private RequirementVersion prepareRequirement(RequirementStatus status, TestCase testCase){
		def req = new RequirementVersion(name:"req", description:"this is a req");
		req.addVerifyingTestCase(testCase)

		for (iterStatus in RequirementStatus.values()){
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return req;
	}

	def "when verified by a test case, the test case should also veryfy the requirementVersion"() {
		given:
		TestCase tc = new TestCase()

		when:
		requirementVersion.addVerifyingTestCase tc

		then:
		tc.verifiedRequirementVersions.contains requirementVersion
	}

	def "requirement version should have an attachment list"() {
		expect:
		requirementVersion.attachmentList != null
	}

	def "should create a 'pastable' copy"() {
		given:
		RequirementVersion source = new RequirementVersion()
		source.name = "source name"
		source.description = "source description"
		source.reference = "source reference"
		source.versionNumber = 10
		use (ReflectionCategory) {
			// reflection on fields to override workflow
			RequirementVersion.set field: "status", of: source, to: RequirementStatus.APPROVED
			RequirementVersion.set field: "criticality", of: source, to: RequirementCriticality.MAJOR
		}

		Requirement   req = new Requirement(source);

		and:
		TestCase verifying = new TestCase()
		source.addVerifyingTestCase verifying

		and:
		Attachment attachment = new Attachment()
		source.attachmentList.addAttachment attachment

		when:
		RequirementVersion copy = source.createPastableCopy()

		then:
		copy.name == source.name
		copy.description == source.description
		copy.status == source.status
		copy.reference == source.reference
		copy.criticality == source.criticality
		copy.requirement == null
		copy.versionNumber == source.versionNumber

		copy.verifyingTestCases.containsExactly([verifying])
		verifying.verifiedRequirementVersions.containsExactly([source, copy])

		copy.attachmentList.allAttachments.size() == 1
		!copy.attachmentList.allAttachments.contains(attachment)
	}

	def "should create next version"() {
		given:
		RequirementVersion previousVersion = new RequirementVersion()
		previousVersion.name = "source name"
		previousVersion.description = "source description"
		previousVersion.reference = "source reference"
		previousVersion.versionNumber = 10
		use (ReflectionCategory) {
			// reflection on fields to override workflow
			RequirementVersion.set field: "status", of: previousVersion, to: RequirementStatus.APPROVED
			RequirementVersion.set field: "criticality", of: previousVersion, to: RequirementCriticality.MAJOR
		}

		Requirement   req = new Requirement(previousVersion)

		and:
		TestCase verifying = new TestCase()
		previousVersion.addVerifyingTestCase verifying

		and:
		Attachment attachment = new Attachment()
		previousVersion.attachmentList.addAttachment attachment

		when:
		RequirementVersion nextVersion = previousVersion.createNextVersion()

		then:
		nextVersion.name == previousVersion.name
		nextVersion.description == previousVersion.description
		nextVersion.status == RequirementStatus.WORK_IN_PROGRESS
		nextVersion.reference == previousVersion.reference
		nextVersion.criticality == previousVersion.criticality
		nextVersion.requirement == null
		nextVersion.versionNumber == previousVersion.versionNumber + 1

		nextVersion.verifyingTestCases.size() == 0

		nextVersion.attachmentList.allAttachments.size() == 1
		!nextVersion.attachmentList.allAttachments.contains(attachment)
	}

}
