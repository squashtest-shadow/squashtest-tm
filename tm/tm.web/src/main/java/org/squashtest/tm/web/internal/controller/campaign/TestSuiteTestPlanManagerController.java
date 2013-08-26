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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.IndexBasedMapper;

/**
 * 
 * @author R.A
 */
@Controller
public class TestSuiteTestPlanManagerController {

	private static final String FALSE = "false";
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";
	private static final String TESTPLANS_IDS_REQUEST_PARAM = "testPlanIds[]";

	@Inject
	private TestSuiteModificationService service;
	private IterationFinder iterationFinder;
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	private final DatatableMapper testPlanMapper = new IndexBasedMapper(11)
			.mapAttribute(2, "name", Project.class)
			.mapAttribute(3, "reference", TestCase.class)
			.mapAttribute(4, "name", TestCase.class)
			.mapAttribute(5, "importance", TestCase.class)
			.mapAttribute(6, "executionMode", TestCase.class)
			.mapAttribute(7, "executionStatus", IterationTestPlanItem.class)
			.mapAttribute(8, "lastExecutedBy", IterationTestPlanItem.class)
			.mapAttribute(9, "lastExecutedOn", IterationTestPlanItem.class);

	@Inject
	private InternationalizationHelper messageSource;
	
	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder; 

	@ServiceReference
	public void setIterationFinder(IterationFinder iterationFinder) {
		this.iterationFinder = iterationFinder;
	}

	@ServiceReference
	public void setIterationTestPlanManagerService(IterationTestPlanManagerService iterationTestPlanManagerService) {
		this.iterationTestPlanManagerService = iterationTestPlanManagerService;
	}

	@ServiceReference
	public void setTestSuiteTestPlanManagerService(TestSuiteTestPlanManagerService testSuiteTestPlanManagerService) {
		this.testSuiteTestPlanManagerService = testSuiteTestPlanManagerService;
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long id, @PathVariable long iterationId, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		Iteration iteration = iterationFinder.findById(iterationId);
		TestSuite testSuite = testSuiteTestPlanManagerService.findTestSuite(id);

		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/iterations/show-iteration-test-plan-manager");
		mav.addObject("iteration", iteration);
		mav.addObject("testSuite", testSuite);
		mav.addObject("baseURL", "/test-suites/" + id + "/" + iterationId);
		mav.addObject("useIterationTable", false);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates =  JsTreeHelper.mapIdsByType(openedNodes);
		
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-case/{testPlanId}/assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long testPlanId, @PathVariable long id,
			@PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItem(testPlanId, userId);
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/batch-assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(TESTPLANS_IDS_REQUEST_PARAM) List<Long> testPlanIds,
			@PathVariable long id, @PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, userId);
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/assignable-user", method = RequestMethod.GET)
	public ModelAndView getAssignUserForIterationTestPlanItem(
			@RequestParam("testPlanId") long iterationTestPlanItemIdId, @PathVariable long id,
			@PathVariable long iterationId, final Locale locale) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(iterationTestPlanItemIdId);
		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		IterationTestPlanItem itp = iterationTestPlanManagerService.findTestPlanItem(iterationTestPlanItemIdId);

		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");

		mav.addObject("usersList", usersList);
		mav.addObject("selectIdentitier", "usersList" + iterationTestPlanItemIdId);
		mav.addObject("selectClass", "userLogin");
		mav.addObject("dataAssignUrl", "/test-suites/" + id + "/" + iterationId + "/test-case/"
				+ iterationTestPlanItemIdId + "/assign-user");

		if (itp.getUser() != null) {
			mav.addObject("testCaseAssignedLogin", itp.getUser().getLogin());
		} else {
			mav.addObject("testCaseAssignedLogin", null);
		}

		return mav;
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/batch-assignable-user", method = RequestMethod.GET)
	public ModelAndView getAssignUserForIterationTestPlanItems(@PathVariable long id, @PathVariable long iterationId,
			final Locale locale) {

		List<User> userList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");
		mav.addObject("usersList", userList);
		mav.addObject("selectIdentitier", "comboUsersList");
		mav.addObject("testCaseAssignedLogin", null);
		mav.addObject("selectClass", "comboLogin");
		return mav;
	}

	@RequestMapping(value = "/test-suites/{id}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void changeTestPlanIndex(@PathVariable("id") long testSuiteId, @PathVariable("newIndex") int newIndex,
			@PathVariable("itemIds") List<Long> itemIds) {
		service.changeTestPlanPosition(testSuiteId, newIndex, itemIds);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("test-suite " + testSuiteId + ": moving " + itemIds.size() + " test plan items  to "
					+ newIndex);
		}
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long id, @PathVariable long iterationId) {
		testSuiteTestPlanManagerService.addTestCasesToIterationAndTestSuite(testCasesIds, id);
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan/{testPlanIds}/delete", method = RequestMethod.POST)
	public @ResponseBody
	String removeTestCaseFromTestSuiteAndIteration(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@PathVariable long id) {
		// check if a test plan was already executed and therefore not removed from the iteration
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(
				testPlanIds, id);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan/{testPlanIds}/detach", method = RequestMethod.POST)
	public @ResponseBody
	String detachTestCaseFromTestSuite(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable long id) {
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlanIds, id);
		return FALSE;
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-cases/table", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable Long id, final DataTableDrawParameters params, final Locale locale) {

		Paging paging = new DataTableSorting(params, testPlanMapper);

		PagedCollectionHolder<List<IterationTestPlanItem>> holder = testSuiteTestPlanManagerService.findTestPlan(id,
				paging);

		return new IterationTestPlanItemDataTableModelHelper(messageSource, locale).buildDataModel(holder,
				params.getsEcho());

	}

	@RequestMapping(value = "/test-suites/test-cases", method = RequestMethod.POST, params = { "test-cases[]",
			"test-suites[]" })
	public @ResponseBody
	Map<String, List<Long>> bindTestPlan(@RequestParam("test-cases[]") List<Long> itpIds,
			@RequestParam("test-suites[]") List<Long> suitesIds) {
		service.bindTestPlanToMultipleSuites(suitesIds, itpIds);
		Map<String, List<Long>> result = new HashMap<String, List<Long>>();
		result.put("ids", suitesIds);
		return result;
	}

	private static class IterationTestPlanItemDataTableModelHelper extends DataTableModelBuilder<IterationTestPlanItem> {

		private InternationalizationHelper messageSource;
		private Locale locale;

		private IterationTestPlanItemDataTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
			this.messageSource = messageSource;
			this.locale = locale;
		}

		@Override
		public Object[] buildItemData(IterationTestPlanItem item) {

			String projectName;
			String testCaseName;
			String importance;
			String testCaseId;
			String reference;

			// ugly copypasta from IterationThingieBuilder
			String datasetName;
			if (item.getReferencedDataset() == null) {
				datasetName = formatNoData(locale);
			} else {
				datasetName = item.getReferencedDataset().getName();
			}

			if (item.isTestCaseDeleted()) {
				projectName = formatNoData(locale);
				testCaseName = formatDeleted(locale);
				importance = formatNoData(locale);
				reference = formatNoData(locale);
				testCaseId = "";
			} else {
				projectName = item.getReferencedTestCase().getProject().getName();
				testCaseName = item.getReferencedTestCase().getName();
				reference = item.getReferencedTestCase().getReference();
				importance = formatImportance(item.getReferencedTestCase().getImportance(), locale);
				testCaseId = item.getReferencedTestCase().getId().toString();
			}

			return new Object[] { item.getId(), getCurrentIndex(), projectName, reference, testCaseName, importance,
					datasetName, testCaseId, item.isTestCaseDeleted(), " " };
		}

		private String formatNoData(Locale locale) {
			return this.messageSource.noData(locale);
		}

		private String formatDeleted(Locale locale) {
			return this.messageSource.itemDeleted(locale);
		}

		private String formatImportance(TestCaseImportance importance, Locale locale) {
			return this.messageSource.internationalize(importance, locale);
		}
	}

}
