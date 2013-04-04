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
package org.squashtest.tm.web.internal.controller.testcase

import javax.inject.Provider

import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.NoVerifiableRequirementVersionException
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;

import spock.lang.Specification


class RequirementVersionCoverageControllerTest extends Specification{
	RequirementVersionCoverageController controller = new RequirementVersionCoverageController()
	VerifiedRequirementsManagerService verifiedRequirementsManagerService = Mock()
	def setup() {
		controller.verifiedRequirementsManagerService = verifiedRequirementsManagerService
	}
	def "should remove single requirement from verified requirements of test case"() {
		when:
		controller.removeVerifiedRequirementVersionFromTestCase(20, 10)

		then:
		1 * verifiedRequirementsManagerService.removeVerifiedRequirementVersionFromTestCase(20, 10)
	}
}
