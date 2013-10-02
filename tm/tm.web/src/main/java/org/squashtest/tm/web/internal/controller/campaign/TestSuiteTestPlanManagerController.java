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
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
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
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
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

	private static final String FALSE = "false";
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";
	private static final String TEST_SUITE = "testSuite";

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
			.mapAttribute("project-name", "name", Project.class).mapAttribute("reference", "reference", TestCase.class)
			.mapAttribute("tc-name", "name", TestCase.class).mapAttribute("importance", "importance", TestCase.class)
			.mapAttribute("dataset", "name", Dataset.class)
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
	public ModelAndView showManager(@PathVariable("suiteId") long suiteId,
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
	DataTableModel getTestPlanModel(@PathVariable("suiteId") long suiteId, final DataTableDrawParameters params,
			final Locale locale) {

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testPlanMapper);
		PagedCollectionHolder<List<IndexedIterationTestPlanItem>> holder = testSuiteTestPlanManagerService
				.findAssignedTestPlan(suiteId, paging);

		return new TestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/test-suites/{suiteId}/assignable-users", method = RequestMethod.GET)
	@ResponseBody
	public List<TestPlanAssignableUser> getAssignUserForTestSuite(@PathVariable("suiteId") long suiteId,
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
	long assignUserToCampaignTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@PathVariable("suiteId") long suiteId, @RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void changeTestPlanIndex(@PathVariable("suiteId") long suiteId, @PathVariable("newIndex") int newIndex,
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
	public void reorderTestPlan(@PathVariable("suiteId") long suiteId, DataTableDrawParameters parameters) {

		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		testSuiteTestPlanManagerService.reorderTestPlan(suiteId, sorting);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable("suiteId") long suiteId) {
		testSuiteTestPlanManagerService.addTestCasesToIterationAndTestSuite(testCasesIds, suiteId);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestCaseFromTestSuiteAndIteration(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@PathVariable("suiteId") long suiteId) {
		// check if a test plan was already executed and therefore not removed from the iteration
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(
				testPlanIds, suiteId);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE, params = { "detach=true" })
	public @ResponseBody
	String detachTestCaseFromTestSuite(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@PathVariable("suiteId") long suiteId) {
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlanIds, suiteId);
		return FALSE;
	}

	@RequestMapping(value = "/test-suites/{suiteIds}/test-plan", method = RequestMethod.POST, params = { "itemIds[]" })
	public @ResponseBody
	Map<String, List<Long>> bindTestPlan(@RequestParam("itemIds[]") List<Long> itpIds,
			@PathVariable("suiteIds") List<Long> suitesIds) {
		LOGGER.debug("bind test plan items to test suites");
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(suitesIds, itpIds);
		Map<String, List<Long>> result = new HashMap<String, List<Long>>();
		result.put("ids", suitesIds);
		return result;
	}

	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { "itemIds[]",
			"boundSuiteIds[]", "unboundSuiteIds[]" })
	public @ResponseBody
	void changeboundTestPlan(@RequestParam("itemIds[]") List<Long> itpIds,
			@RequestParam("boundSuiteIds[]") List<Long> boundTestSuitesIds,
			@RequestParam("unboundSuiteIds[]") List<Long> unboundTestSuiteIds) {
		LOGGER.debug("bind test plan items to test suites");
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(boundTestSuitesIds, itpIds);
		testSuiteTestPlanManagerService.unbindTestPlanToMultipleSuites(unboundTestSuiteIds, itpIds);
	}
	
	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { "itemIds[]", "unboundSuiteIds[]" })
	public @ResponseBody
	void unbindTestPlans(@RequestParam("itemIds[]") List<Long> itpIds,
			@RequestParam("unboundSuiteIds[]") List<Long> unboundTestSuiteIds) {
		LOGGER.debug("bind test plan items to test suites");
		testSuiteTestPlanManagerService.unbindTestPlanToMultipleSuites(unboundTestSuiteIds, itpIds);
	}
	
	@RequestMapping(value = "/test-suites/test-plan", method = RequestMethod.POST, params = { "itemIds[]",
			"boundSuiteIds[]" })
	public @ResponseBody
	void bindTestPlans(@RequestParam("itemIds[]") List<Long> itpIds,
			@RequestParam("boundSuiteIds[]") List<Long> boundTestSuitesIds) {
		LOGGER.debug("bind test plan items to test suites");
		testSuiteTestPlanManagerService.bindTestPlanToMultipleSuites(boundTestSuitesIds, itpIds);
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = { "status" })
	public @ResponseBody
	String setTestPlanItemStatus(@PathVariable("testPlanId") long testPlanId, @RequestParam("status") String status) {
		LOGGER.debug("change status test plan item #{} to {}", testPlanId, status);
		iterationTestPlanManagerService.assignExecutionStatusToTestPlanItem(testPlanId, status);
		return status;
	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{itemId}/executions", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable("suiteId") long suiteId,
			@PathVariable("itemId") long itemId) {
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
	String addManualExecution(@PathVariable("suiteId") long suiteId, @PathVariable("itemId") long itemId) {
		LOGGER.debug("add manual execution to item #{}", itemId);
		TestSuite testSuite = service.findById(suiteId);
		Long iterationId = testSuite.getIteration().getId();

		service.addExecution(itemId);
		List<Execution> executionList = iterationFinder.findExecutionsByTestPlan(iterationId, itemId);

		return executionList.get(executionList.size() - 1).getId().toString();

	}

	@RequestMapping(value = "/test-suites/{suiteId}/test-plan/{testPlanId}/executions/new", method = RequestMethod.POST, params = { "mode=auto" })
	public @ResponseBody
	AutomatedSuiteOverview addAutoExecution(@PathVariable("suiteId") long suiteId, @PathVariable("itemId") long itemId,
			Locale locale) {
		LOGGER.debug("add automated execution to item #{}", itemId);
		List<Long> testPlanIds = new ArrayList<Long>(1);
		testPlanIds.add(itemId);

		AutomatedSuite suite = service.createAndStartAutomatedSuite(itemId, testPlanIds);

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);

	}

	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}

}
