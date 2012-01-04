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

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/test-case-browser")
public class TestCaseLibraryNavigationController extends
		LibraryNavigationController<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> {
	public static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNavigationController.class);

	@Inject
	private Provider<TestCaseLibraryTreeNodeBuilder> testCaseLibraryTreeNodeBuilder;

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;

	@ServiceReference
	public void setTestCaseLibraryNavigationService(TestCaseLibraryNavigationService testCaseLibraryNavigationService) {
		this.testCaseLibraryNavigationService = testCaseLibraryNavigationService;
	}

	@Override
	protected LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> getLibraryNavigationService() {
		return testCaseLibraryNavigationService;
	}

	@Override
	protected String getEditFolderViewName() {
		return "fragment/generics/edit-folder";
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(TestCaseLibraryNode node) {
		return testCaseLibraryTreeNodeBuilder.get().setNode(node).build();
	}

	@RequestMapping(value = "/drives/{libraryId}/content/new-test-case", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewTestCaseToLibraryRootContent(@PathVariable long libraryId,
			@Valid @ModelAttribute("add-test-case") TestCase testCase) {

		testCaseLibraryNavigationService.addTestCaseToLibrary(libraryId, testCase);

		LOGGER.debug("TEST CASE ADDED TO ROOT OF LIB " + libraryId + " " + testCase.getName() + " "
				+ testCase.getDescription());

		return createTreeNodeFromLibraryNode(testCase);
	}

	@RequestMapping(value = "/folders/{folderId}/content/new-test-case", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewTestCaseToFolder(@PathVariable long folderId,
			@Valid @ModelAttribute("add-test-case") TestCase testCase) {

		testCaseLibraryNavigationService.addTestCaseToFolder(folderId, testCase);

		return createTreeNodeFromLibraryNode(testCase);
	}

	@Deprecated
	@RequestMapping(method = RequestMethod.GET, params = "show-test-case")
	public final ModelAndView showLibraryWithOpenTestCase(@PathVariable long libraryId,
			@RequestParam("show-test-case") long testCaseId) {
		LOGGER.debug("showLibraryWithOpenTestCase");

		TestCaseLibrary library = testCaseLibraryNavigationService.findLibrary(libraryId);
		TestCase testCase = testCaseLibraryNavigationService.findTestCase(testCaseId);

		ModelAndView mav = new ModelAndView("page/test-case-libraries/show-test-case-in-library");
		mav.addObject("library", library);
		JsTreeNode rootModel = driveNodeBuilder.get().setModel(library).build();
		JsTreeNode testCaseModel = testCaseLibraryTreeNodeBuilder.get().setNode(testCase).build();
		rootModel.setChildren(Arrays.asList(testCaseModel));

		mav.addObject("rootModel", rootModel);
		mav.addObject("selectedNode", testCaseModel);

		return mav;
	}

	@Override
	protected String getShowLibraryViewName() {
		return "page/test-case-libraries/show-test-case-library";
	}
	
	
	@RequestMapping(value="/import/upload", method = RequestMethod.POST,  params = "upload-ticket")
	public @ResponseBody String importArchive(@RequestParam("archive") MultipartFile archive, 
			@RequestParam("projectId") Long projectId){
		return "{ \"status\" : \"ok\"}"; 
	}

}

