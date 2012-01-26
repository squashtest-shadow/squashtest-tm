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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.VerifiedRequirementException;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.service.VerifiedRequirementsManagerService;
import org.squashtest.csp.tm.web.internal.helper.VerifiedRequirementActionSummaryBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

/**
 * Controller for verified requirements management page.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
public class VerifiedRequirementsManagerController {
	private static final String REQUIREMENTS_IDS = "requirementsIds[]";

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@ServiceReference
	public void setVerifiedRequirementsManagerService(
			VerifiedRequirementsManagerService verifiedRequirementsManagerService) {
		this.verifiedRequirementsManagerService = verifiedRequirementsManagerService;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements-manager", method = RequestMethod.GET)
	public String showManager(@PathVariable long testCaseId, Model model) {
		TestCase testCase = verifiedRequirementsManagerService.findTestCase(testCaseId);
		List<RequirementLibrary> linkableLibraries = verifiedRequirementsManagerService
				.findLinkableRequirementLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		model.addAttribute("testCase", testCase);
		model.addAttribute("linkableLibrariesModel", linkableLibrariesModel);

		return "page/test-cases/show-verified-requirements-manager";
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<RequirementLibrary> linkableLibraries) {
		DriveNodeBuilder builder = driveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (RequirementLibrary library : linkableLibraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public @ResponseBody
	Map<String, Object> addVerifiedRequirementsToTestCase(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
			@PathVariable long testCaseId) {
		Collection<VerifiedRequirementException> rejections = verifiedRequirementsManagerService
				.addVerifiedRequirementsToTestCase(requirementsIds, testCaseId);

		return buildSummary(rejections);

	}

	private Map<String, Object> buildSummary(Collection<VerifiedRequirementException> rejections) {
		return VerifiedRequirementActionSummaryBuilder.buildAddActionSummary(rejections);
	}

	@RequestMapping(value = "/test-cases/{testCaseId}/non-verified-requirements", method = RequestMethod.POST, params = REQUIREMENTS_IDS)
	public @ResponseBody
	void removeVerifiedRequirementsFromTestCase(@RequestParam(REQUIREMENTS_IDS) List<Long> requirementsIds,
			@PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementsFromTestCase(requirementsIds, testCaseId);

	}

	@RequestMapping(value = "/test-cases/{testCaseId}/verified-requirements/{requirementId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifiedRequirementFromTestCase(@PathVariable long requirementId, @PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementFromTestCase(requirementId, testCaseId);

	}
}
