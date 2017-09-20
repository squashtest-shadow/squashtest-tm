/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.dto.json.JsTreeNode;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.rest.RestLibrary;
import org.squashtest.tm.web.internal.model.rest.RestProject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/test-case-workspace")
public class TestCaseWorkspaceController extends WorkspaceController<TestCaseLibraryNode> {

	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;

	@Inject
	@Named("squashtest.tm.service.TestCasesWorkspaceService")
	private WorkspaceService<TestCaseLibrary> workspaceService;

	@Inject
	private TestCaseWorkspaceDisplayService testCaseWorkspaceDisplayService;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@Override
	protected WorkspaceService<TestCaseLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "test-case-workspace.html";
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	@Override
	protected WorkspaceType getWorkspaceType() {
		return null;
	}

	@Override
	protected void populateModel(Model model, Locale locale) {
		//Degenerated code
		//Client side needs the editable libraries in a different shape. the sad part is that libraries are already in model with all needed information
		//No time to find and refactor HTML and JS that use that, and maybe it can't be done
		//So i just reshape data without refetching in database like it was done previously, witch is a pain with just Objects :-(
		Collection<JsTreeNode> jsTreeNodes = (Collection<JsTreeNode>) model.asMap().get("rootModel");//NOSONAR it's should be safe, we just created that in WorkspaceController
		List<RestLibrary> libraries = jsTreeNodes.stream()
			.filter(jsTreeNode -> {
				Object editable = jsTreeNode.getAttr().get("editable");
				return Objects.nonNull(editable) && Objects.equals(editable.toString(), "true");
			})
			.sorted(Comparator.comparing(JsTreeNode::getTitle))
			.map(jsTreeNode -> {
				RestLibrary restLibrary = new RestLibrary();
				restLibrary.setId(Long.parseLong(jsTreeNode.getAttr().get("resId").toString()));
				RestProject restProject = new RestProject();
				restProject.setId(Long.parseLong(jsTreeNode.getAttr().get("resId").toString()));
				restProject.setName(jsTreeNode.getTitle());
				restLibrary.setProject(restProject);
				return restLibrary;
			}).collect(toList());
		model.addAttribute("editableLibraries", libraries);
	}

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#driveNodeBuilderProvider()
	 */
	@Override
	protected Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return testCaseWorkspaceDisplayService;
	}

	@Override
	protected String[] getNodeParentsInWorkspace(Long elementId) {
		List<String> parents = testCaseLibraryNavigationService.getParentNodesAsStringList(elementId);
		return parents.toArray(new String[parents.size()]);
	}

	@Override
	protected String getTreeElementIdInWorkspace(Long elementId) {
		return "TestCase-" + elementId;
	}

}
