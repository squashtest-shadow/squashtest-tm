/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.ExportTestStepData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseFormModel.TestCaseFormModelValidator;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Controller
@RequestMapping("/test-case-browser")
public class TestCaseLibraryNavigationController extends
		LibraryNavigationController<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> {
	public static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNavigationController.class);
	
	@Inject
	private Provider<TestCaseLibraryTreeNodeBuilder> testCaseLibraryTreeNodeBuilder;

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;
	
	private static final String JASPER_EXPORT_FILE = "/WEB-INF/reports/test-case-export.jasper";

	

	@Override
	protected LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> getLibraryNavigationService() {
		return testCaseLibraryNavigationService;
	}

	

	@Override
	protected String getShowLibraryViewName() {
		return "page/test-case-libraries/show-test-case-library";
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(TestCaseLibraryNode node) {
		return testCaseLibraryTreeNodeBuilder.get().setNode(node).build();
	}
	

	@InitBinder("add-test-case")
	public void addTestCaseBinder(WebDataBinder binder){
		TestCaseFormModelValidator validator = new TestCaseFormModelValidator();
		validator.setMessageSource(getMessageSource());
		binder.setValidator(validator);
	}
	
	

	@RequestMapping(value = "/drives/{libraryId}/content/new-test-case", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewTestCaseToLibraryRootContent(@PathVariable long libraryId,
			@Valid @ModelAttribute("add-test-case") TestCaseFormModel testCaseModel){
		
		TestCase testCase = testCaseModel.getTestCase();
		
		Map<Long, String> customFieldValues = testCaseModel.getCustomFields();

		testCaseLibraryNavigationService.addTestCaseToLibrary(libraryId, testCase, customFieldValues);

		return createTreeNodeFromLibraryNode(testCase);
	}

	
	@RequestMapping(value = "/folders/{folderId}/content/new-test-case", method = RequestMethod.POST)
	public @ResponseBody JsTreeNode addNewTestCaseToFolder(@PathVariable long folderId,
			@Valid @ModelAttribute("add-test-case") TestCaseFormModel testCaseModel){
		
		TestCase testCase = testCaseModel.getTestCase();
		
		Map<Long, String> customFieldValues = testCaseModel.getCustomFields();
		
		testCaseLibraryNavigationService.addTestCaseToFolder(folderId, testCase, customFieldValues);

		return createTreeNodeFromLibraryNode(testCase);
	}
	



	@RequestMapping(value = "/import/upload", method = RequestMethod.POST, params = "upload-ticket", produces="text/html")
	public ModelAndView importArchive(@RequestParam("archive") MultipartFile archive,
			@RequestParam("projectId") Long projectId, @RequestParam("zipEncoding") String zipEncoding)
			throws IOException {

		InputStream stream = archive.getInputStream();

		ImportSummary summary =  testCaseLibraryNavigationService.importExcelTestCase(stream, projectId, zipEncoding);
		ModelAndView mav =  new ModelAndView("fragment/import/import-summary");
		mav.addObject("summary", summary);
		mav.addObject("workspace", "test-case");
		return mav;

	}

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody
	List<JsTreeNode> getLinkablesRootModel() {
		List<TestCaseLibrary> linkableLibraries = testCaseLibraryNavigationService.findLinkableTestCaseLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}
	
	@RequestMapping(value = "/export-folder", method = RequestMethod.GET)
	public @ResponseBody
	void exportTestCases(@RequestParam("tab[]") List<Long> ids, @RequestParam("name") String filename, @RequestParam("format") String format,
			HttpServletResponse response, Locale locale) {
		List<ExportTestCaseData> dataSource = testCaseLibraryNavigationService
				.findTestCasesToExportFromNodes(ids);
		escapePrerequisiteAndSteps(dataSource);
		printExport(dataSource, filename,JASPER_EXPORT_FILE, response, locale, format);

	}

	private void escapePrerequisiteAndSteps(List<ExportTestCaseData> dataSource) {
		for (ExportTestCaseData data : dataSource) {
			//escape prerequisite
			String htmlPrerequisite = data.getPrerequisite();
			String prerequisite = HTMLCleanupUtils.htmlToText(htmlPrerequisite);
			data.setPrerequisite(prerequisite);
			
			//escape first step
			String htmlFirstAction = data.getFirstAction();
			String firstAction = HTMLCleanupUtils.htmlToText(htmlFirstAction);
			data.setFirstAction(firstAction);
			
			String htmlFirstResult = data.getFirstExpectedResult();
			String firstResult = HTMLCleanupUtils.htmlToText(htmlFirstResult);
			data.setFirstExpectedResult(firstResult);
			
			//escape other steps
			for(ExportTestStepData step : data.getSteps()){
				String htmlAction = step.getAction();
				String action = HTMLCleanupUtils.htmlToText(htmlAction);
				step.setAction(action);
				
				String htmlExpectedResult = step.getExpectedResult();
				String expectedResult = HTMLCleanupUtils.htmlToText(htmlExpectedResult);
				step.setExpectedResult(expectedResult);
			}
			
		}
	}

	@RequestMapping(value = "/export-library", method = RequestMethod.GET)
	public @ResponseBody
	void exportLibrary(@RequestParam("tab[]") List<Long> libraryIds, @RequestParam("name") String filename, @RequestParam("format") String format,
			HttpServletResponse response, Locale locale) {

		List<ExportTestCaseData> dataSource = testCaseLibraryNavigationService.findTestCasesToExportFromLibrary(libraryIds);
		
		escapePrerequisiteAndSteps(dataSource);
		printExport(dataSource, filename,JASPER_EXPORT_FILE, response, locale, format);

	}

	
	// ****************************** statistics section *******************************
	
	@RequestMapping (value = "/statistics", method = RequestMethod.GET, produces="application/json", params = {"libraries", "nodes"})
	public @ResponseBody TestCaseStatisticsBundle getStatisticsAsJson(@RequestParam(value="libraries", defaultValue="") Collection<Long> libraryIds, 
																	  @RequestParam(value="nodes", defaultValue="") Collection<Long> nodeIds){
		
		return testCaseLibraryNavigationService.getStatisticsForSelection(libraryIds, nodeIds);
	}
	
	@RequestMapping (value = "/dashboard", method = RequestMethod.GET, produces="text/html", params = {"libraries", "nodes"})
	public String getDashboard(Model model, @RequestParam("libraries") Collection<Long> libraryIds, 
											@RequestParam("nodes") Collection<Long> nodeIds){
		
		TestCaseStatisticsBundle stats = testCaseLibraryNavigationService.getStatisticsForSelection(libraryIds, nodeIds);
		
		model.addAttribute("statistics", stats);
		
		return "fragment/test-cases/test-case-dashboard";
	}
	

}
