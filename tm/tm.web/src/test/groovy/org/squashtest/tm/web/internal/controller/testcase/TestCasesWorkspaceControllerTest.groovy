/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import org.springframework.ui.Model
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.library.WorkspaceService
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder

import javax.inject.Provider

class TestCasesWorkspaceControllerTest extends NodeBuildingSpecification {
    TestCaseWorkspaceController controller = new TestCaseWorkspaceController()
    WorkspaceService service = Mock()
    DriveNodeBuilder driveNodeBuilder = new DriveNodeBuilder(permissionEvaluator(), Mock(Provider))

    Provider provider = Mock()

    def setup() {
        controller.workspaceService = service
        provider.get() >> driveNodeBuilder
        use(ReflectionCategory) {
            TestCaseWorkspaceController.set field: 'driveNodeBuilderProvider', of: controller, to: provider
        }
    }

    def "show should return workspace view with tree root model"() {
        given:
        TestCaseLibrary library = Mock()
        library.getClassSimpleName() >> "TestCaseLibrary"
        Project project = Mock()
        library.project >> project
        service.findAllLibraries() >> [library]
        def model = Mock(Model)

        when:
        String view = controller.showWorkspace(model, Locale.getDefault(), [] as String[], "" as String)

        then:
        view == "page/test-case-workspace"
        1 * model.addAttribute("rootModel", _)
    }
}
