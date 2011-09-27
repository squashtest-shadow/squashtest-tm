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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

/**
 * Controller for verified requirements management page.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
public class VerifyingTestCaseManagerController {
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject private Provider<DriveNodeBuilder> driveNodeBuilder;

	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@ServiceReference
	public void setVerifyingTestCaseManagerService(
			VerifyingTestCaseManagerService verifyingTestCaseManagerService) {
		this.verifyingTestCaseManagerService = verifyingTestCaseManagerService;
	}

	@RequestMapping(value = "/requirements/{requirementId}/verifying-test-cases-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long requirementId) {
		Requirement requirement = verifyingTestCaseManagerService.findRequirement(requirementId);
		List<TestCaseLibrary> linkableLibraries = verifyingTestCaseManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		ModelAndView mav = new ModelAndView("page/requirements/show-verifying-testcase-manager");
		mav.addObject("requirement", requirement);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		DriveNodeBuilder builder = driveNodeBuilder.get();

		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (TestCaseLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	@RequestMapping(value = "/requirements/{requirementId}/verifying-test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addVerifyingTestCasesToRequirement(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long requirementId) {
		verifyingTestCaseManagerService.addVerifyingTestCasesToRequirement(testCasesIds, requirementId);
	}

	@RequestMapping(value = "/requirements/{requirementId}/non-verifying-test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void removeVerifyingTestCasesFromRequirement(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long requirementId) {
		verifyingTestCaseManagerService.removeVerifyingTestCasesFromRequirement(testCasesIds, requirementId);
	}

	@RequestMapping(value = "/requirements/{requirementId}/verifying-test-cases/{testCaseId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifyingTestCaseFromRequirement(@PathVariable long testCaseId, @PathVariable long requirementId) {
		verifyingTestCaseManagerService.removeVerifyingTestCaseFromRequirement(requirementId, testCaseId);
	}
}
