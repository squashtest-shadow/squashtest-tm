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

package org.squashtest.csp.tm.web.internal.controller.testcase;

import javax.inject.Provider;

import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.service.VerifiedRequirementsManagerService;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import spock.lang.Specification;


class VerifiedRequirementsManagerControllerTest extends Specification{
	VerifiedRequirementsManagerController controller = new VerifiedRequirementsManagerController()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	Provider driveNodeBuilder = Mock()

	def setup() {
		controller.verifiedRequirementsManagerService = verifiedRequirementsManagerService
		controller.driveNodeBuilder = driveNodeBuilder
		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService))
	}

	def "should show manager page"() {
		given:
		verifiedRequirementsManagerService.findLinkableRequirementLibraries() >> []

		when:
		def res = controller.showManager(20L)

		then:
		res.viewName == "page/test-cases/show-verified-requirements-manager"
	}

	def "should populate manager page with test case and requirement libraries model"() {
		given:
		TestCase testCase = Mock()
		verifiedRequirementsManagerService.findTestCase(20L) >> testCase

		and:
		RequirementLibrary lib = Mock()
		lib.getClassSimpleName() >> "RequirementLibrary"
		Project project = Mock()
		lib.project >> project
		verifiedRequirementsManagerService.findLinkableRequirementLibraries() >> [lib]

		when:
		def res = controller.showManager(20L)

		then:
		res.model['testCase'] == testCase
		res.model['linkableLibrariesModel'] != null
	}

	def "should add requirements to verified requirements of test case"() {
		when:
		controller.addVerifiedRequirementsToTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.addVerifiedRequirementsToTestCase([5, 15], 10)
	}

	def "should remove requirements to verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementsFromTestCase([5, 15], 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementsFromTestCase([5, 15], 10)
	}

	def "should remove single requirement from verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementFromTestCase(20, 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementFromTestCase(20, 10)
	}
}
