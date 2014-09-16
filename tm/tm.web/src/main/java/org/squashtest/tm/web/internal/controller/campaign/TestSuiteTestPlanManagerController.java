/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableColumnFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
import org.squashtest.tm.web.internal.model.json.JsonIterationTestPlanItem;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * 
 * @author R.A
 * @authored bsiri
 */
@Controller
public class TestSuiteTestPlanManagerController {

	private static final String UNBOUND_SUITE_IDS = "unboundSuiteIds[]";
	private static final String BOUND_SUITE_IDS = "boundSuiteIds[]";
	private static final String BIND_TEST_PLAN_ITEMS_TO_TEST_SUITES = "bind test plan items to test suites";
	private static final String FALSE = "false";
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";
	private static final String TEST_SUITE = "testSuite";
	private static final String TEST_SUITE_ID = "suiteId";
	private static final String NAME = "name";
	private static final String REFERENCE = "reference";
	private static final String IMPORTANCE = "importance";
	private static final String ITEM_IDS = "itemIds[]";
	private static final String STATUS = "status";
	private static final String ITEM_ID = "itemId";
	private static final String TESTPLAN_IDS = "testPlanIds";

	@Inject
	private TestSuiteModificationService service;

	@Inject
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private IterationFinder iterationFinder;

	@Inject
	private PermissionEvaluationService permissionService;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
	.map("entity-index", "index(IterationTestPlanItem)")
	// index is a special case which means : no sorting.
	.mapAttribute("project-name", NAME, Project.class).mapAttribute(REFERENCE, REFERENCE, TestCase.class)
	.mapAttribute("tc-name", NAME, TestCase.class).mapAttribute(IMPORTANCE, IMPORTANCE, TestCase.class)
	.mapAttribute("dataset.selected.name", NAME, Dataset.class)
	.mapAttribute("status", "executionStatus", IterationTestPlanItem.class)
	.mapAttribute("assignee-login", "login", User.class)
	.mapAttribute("last-exec-on", "lastExecutedOn", IterationTestPlanItem.class)
	.mapAttribute("exec-mode", "automatedTest", TestCase.class);

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable(TEST_SUITE_ID) long suiteId,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		LOGGER.debug("show test suite test plan manager for test suite #{}", suiteId);
		TestSuite testSuite = testSuiteTestPlanManagerService.findTestSuite(suiteId);

		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/test-suites/show-test-suite-test-plan-manager");
		mav.addObject("testSuite", testSuite);
		mav.addObject("baseURL", "/test-suites/" + suiteId);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable(TEST_SUITE_ID) long suiteId, final DataTableDrawParameters params,
			final Locale locale) {

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testPlanMapper);

		ColumnFiltering filter = new DataTableColumnFiltering(params);

		PagedCollectionHolder<List<IndexedIterationTestPlanItem>> holder = testSuiteTestPlanManagerService
				.findAssignedTestPlan(suiteId, paging, filter);

		return new TestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/test-suites/{suiteId}/assignable-users", method = RequestMethod.GET)
	@ResponseBody
	public List<TestPlanAssignableUser> getAssignUserForTestSuite(@PathVariable(TEST_SUITE_ID) long suiteId,
			final Locale locale) {

		TestSuite testSuite = service.findById(suiteId);
		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(testSuite.getIteration()
				.getId());

		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<TestPlanAssignableUser>();

		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel));

		for (User user : usersList) {
			jsonUsers.add(new TestPlanAssignableUser(user));
		}

		return jsonUsers;
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = { "assignee" })
	public @ResponseBody
	long assignUserToCampaignTestPlanItem(@PathVariable(TESTPLAN_IDS) List<Long> testPlanIds,
			@PathVariable(TEST_SUITE_ID) long suiteId, @RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
	}


	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = {"dataset"})
	public @ResponseBody
	Long setDataset(@PathVariable("testPlanId") long testPlanId, @RequestParam("dataset") Long datasetId){
		iterationTestPlanManagerService.changeDataset(testPlanId, datasetId);
		return datasetId;
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void changeTestPlanIndex(@PathVariable(TEST_SUITE_ID) long suiteId, @PathVariable("newIndex") int newIndex,
			@PathVariable("itemIds") List<Long> itemIds) {
		testSuiteTestPlanManagerService.changeTestPlanPosition(suiteId, newIndex, itemIds);
	}

	/**
	 * Will reorder the test plan according to the current sorting instructions.
	 * 
	 * @param iterationId
	 * @return
	 */
	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/order", method = RequestMethod.POST)
	@ResponseBody
	public void reorderTestPlan(@PathVariable(TEST_SUITE_ID) long suiteId, DataTableDrawParameters parameters) {

		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		testSuiteTestPlanManagerService.reorderTestPlan(suiteId, sorting);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable(TEST_SUITE_ID) long suiteId) {
		testSuiteTestPlanManagerService.addTestCasesToIterationAndTestSuite(testCasesIds, suiteId);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestCaseFromTestSuiteAndIteration(@PathVariable(TESTPLAN_IDS) List<Long> testPlanIds,
			@PathVariable(TEST_SUITE_ID) long suiteId) {
		// check if a test plan was already executed and therefore not removed from the iteration
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(
				testPlanIds, suiteId);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE, params = { "detach=true" })
	public @ResponseBody
	String detachTestCaseFromTestSuite(@PathVariable(TESTPLAN_IDS) List<Long> testPlanIds,
			@PathVariable(TEST_SUITE_ID) long suiteId) {
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlanIds, suiteId);
		return FALSE;
	}

	@RequestMapping(value = "/test-suites/{suiteIds}/test-plan", method = RequestMethod.POST, params = { ITEM_IDS })
	public @ResponseBody
	Map<String, List<Long>> bindTestPlan(@RequestParam(ITEM_IDS) List<Long> itpIds,
			@PathVariable("suiteIds") List<Long> suitesIds) {
		LOGGER.debug(BIND_TEST_PLAN_ITEMS_TO_TEST_SUITES);
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(suitesIds, itpIds);
		Map<String, List<Long>> result = new HashMap<String, List<Long>>();
		result.put("ids", suitesIds);
		return result;
	}

	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { ITEM_IDS,
			BOUND_SUITE_IDS, UNBOUND_SUITE_IDS })
	public @ResponseBody
	void changeboundTestPlan(@RequestParam(ITEM_IDS) List<Long> itpIds,
			@RequestParam(BOUND_SUITE_IDS) List<Long> boundTestSuitesIds,
			@RequestParam(UNBOUND_SUITE_IDS) List<Long> unboundTestSuiteIds) {
		LOGGER.debug(BIND_TEST_PLAN_ITEMS_TO_TEST_SUITES);
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(boundTestSuitesIds, itpIds);
		testSuiteTestPlanManagerService.unbindTestPlanToMultipleSuites(unboundTestSuiteIds, itpIds);
	}

	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { ITEM_IDS, UNBOUND_SUITE_IDS })
	public @ResponseBody
	void unbindTestPlans(@RequestParam(ITEM_IDS) List<Long> itpIds,
			@RequestParam(UNBOUND_SUITE_IDS) List<Long> unboundTestSuiteIds) {
		LOGGER.debug(BIND_TEST_PLAN_ITEMS_TO_TEST_SUITES);
		testSuiteTestPlanManagerService.unbindTestPlanToMultipleSuites(unboundTestSuiteIds, itpIds);
	}

	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { ITEM_IDS,
			BOUND_SUITE_IDS })
	public @ResponseBody
	void bindTestPlans(@RequestParam(ITEM_IDS) List<Long> itpIds,
			@RequestParam(BOUND_SUITE_IDS) List<Long> boundTestSuitesIds) {
		LOGGER.debug(BIND_TEST_PLAN_ITEMS_TO_TEST_SUITES);
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(boundTestSuitesIds, itpIds);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = { STATUS })
	public @ResponseBody
	JsonIterationTestPlanItem setTestPlanItemStatus(@PathVariable("testPlanIds") List<Long> testPlanIds, @RequestParam(STATUS) String status) {
		LOGGER.debug("change status test plan items to {}", status);
		List<IterationTestPlanItem> itpis = iterationTestPlanManagerService.forceExecutionStatus(testPlanIds, status);
		return createJsonITPI(itpis.get(0));
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{itemId}/executions", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable(TEST_SUITE_ID) long suiteId,
			@PathVariable(ITEM_ID) long itemId) {
		LOGGER.debug("find model and view for executions of test plan item  #{}", itemId);
		TestSuite testSuite = service.findById(suiteId);
		Long iterationId = testSuite.getIteration().getId();
		List<Execution> executionList = iterationFinder.findExecutionsByTestPlan(iterationId, itemId);

		// get the iteraction to check access rights
		Iteration iter = iterationFinder.findById(iterationId);
		IterationTestPlanItem iterationTestPlanItem = iterationTestPlanManagerService.findTestPlanItem(itemId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);

		ModelAndView mav = new ModelAndView("fragment/test-suites/test-suite-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanItem", iterationTestPlanItem);
		mav.addObject("iterationId", iterationId);
		mav.addObject("executions", executionList);
		mav.addObject(TEST_SUITE, testSuite);

		return mav;

	}

	// ************* execution *****************************************

	// returns the ID of the newly created execution
	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{itemId}/executions/new", method = RequestMethod.POST, params = { "mode=manual" })
	public @ResponseBody
	String addManualExecution(@PathVariable(TEST_SUITE_ID) long suiteId, @PathVariable(ITEM_ID) long itemId) {
		LOGGER.debug("add manual execution to item #{}", itemId);
		Execution newExecution = service.addExecution(itemId);
		return newExecution.getId().toString();

	}


	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}


	private JsonIterationTestPlanItem createJsonITPI(IterationTestPlanItem item){
		String name = (item.isTestCaseDeleted()) ? null : item.getReferencedTestCase().getName();
		return new JsonIterationTestPlanItem(
				item.getId(),
				item.getExecutionStatus(),
				name,
				item.getLastExecutedOn(),
				item.getLastExecutedBy(),
				item.getUser(),
				item.isTestCaseDeleted(),
				item.isAutomated()
				);
	}

}
