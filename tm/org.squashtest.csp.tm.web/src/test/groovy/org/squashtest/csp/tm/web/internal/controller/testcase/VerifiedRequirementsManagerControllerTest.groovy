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
package org.squashtest.csp.tm.web.internal.controller.testcase

import javax.inject.Provider

import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.csp.core.service.security.PermissionEvaluationService
import org.squashtest.csp.tm.domain.NoVerifiableRequirementVersionException
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.service.RequirementLibraryFinderService;
import org.squashtest.csp.tm.service.TestCaseModificationService;
import org.squashtest.csp.tm.service.VerifiedRequirementsManagerService
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder
import spock.lang.Specification


class VerifiedRequirementsManagerControllerTest extends Specification{
	VerifiedRequirementsManagerController controller = new VerifiedRequirementsManagerController()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	Provider driveNodeBuilder = Mock()
	TestCaseModificationService testCaseFinder = Mock()
	RequirementLibraryFinderService requirementLibraryFinder = Mock()

	def setup() {
		controller.verifiedRequirementsManagerService = verifiedRequirementsManagerService
		controller.driveNodeBuilder = driveNodeBuilder
		controller.testCaseFinder = testCaseFinder
		controller.requirementLibraryFinder = requirementLibraryFinder
		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService))
	}

	def "should show manager page"() {
		given:
		requirementLibraryFinder.findLinkableRequirementLibraries() >> []

		when:
		def res = controller.showManager(20L, Mock(Model))

		then:
		res == "page/test-cases/show-verified-requirements-manager"
	}

	def "should populate manager page with test case and requirement libraries model"() {
		given:
		TestCase testCase = Mock()
		testCaseFinder.findById(20L) >> testCase

		and:
		RequirementLibrary lib = Mock()
		lib.getClassSimpleName() >> "RequirementLibrary"
		Project project = Mock()
		lib.project >> project
		requirementLibraryFinder.findLinkableRequirementLibraries() >> [lib]

		and:
		def model = new ExtendedModelMap()

		when:
		def res = controller.showManager(20L, model)

		then:
		model['testCase'] == testCase
		model['linkableLibrariesModel'] != null
	}

	def "should add requirements to verified requirements of test case"() {
		when:
		controller.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.addVerifiedRequirementsToTestCase([5, 15], 10) >> []
	}

	def "should remove requirements to verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementVersionsFromTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementVersionsFromTestCase([5, 15], 10)
	}

	def "should remove single requirement from verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementVersionFromTestCase(20, 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementVersionFromTestCase(20, 10)
	}

	def "should return rapport of requirements which could not be added"() {
		given:
		Requirement req = Mock()
		NoVerifiableRequirementVersionException ex = new NoVerifiableRequirementVersionException(req)
		verifiedRequirementsManagerService.addVerifiedRequirementsToTestCase([5, 15], 10) >> [ex]

		when:
		def res = controller.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		res.noVerifiableVersionRejections
	}
}
