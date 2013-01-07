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
package org.squashtest.tm.web.internal.controller.testcase;

import javax.inject.Provider;

import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.service.WorkspaceService;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseWorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;

import spock.lang.Specification;



class TestCasesWorkspaceControllerTest extends Specification {
	TestCaseWorkspaceController controller = new TestCaseWorkspaceController()
	WorkspaceService service = Mock()
	DriveNodeBuilder driveNodeBuilder = Mock()
	Provider provider = Mock()

	def setup() {
		controller.workspaceService = service
		provider.get() >> driveNodeBuilder
		use(ReflectionCategory) {
			WorkspaceController.set field: 'nodeBuilderProvider', of: controller, to: provider
		}
	}

	def "show should return workspace view with tree root model"() {
		given:
		TestCaseLibrary library = Mock()
		Project project = Mock()
		library.project >> project
		service.findAllLibraries() >> [library]

		when:
		ModelAndView view = controller.showWorkspace()

		then:
		view.viewName == "page/test-case-workspace"
		view.model["rootModel"] != null
	}
}
