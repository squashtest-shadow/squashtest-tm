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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
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
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JeditableComboHelper;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableColumnFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
import org.squashtest.tm.web.internal.model.json.JsonIterationTestPlanItem;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * 
 * @author R.A
 */
@Controller
public class IterationTestPlanManagerController {

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private IterationFinder iterationFinder;

	@Inject
	private Provider<JsonTestCaseBuilder> jsonTestCaseBuilder;

	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
	.map("entity-index", "index(IterationTestPlanItem)")
	// index is a special case which means : no sorting.
	.mapAttribute("project-name", "name", Project.class).mapAttribute("reference", "reference", TestCase.class)
	.mapAttribute("tc-name", "name", TestCase.class).mapAttribute("importance", "importance", TestCase.class)
	.mapAttribute("dataset.selected.name", "name", Dataset.class)
	.mapAttribute("status", "executionStatus", IterationTestPlanItem.class)
	.mapAttribute("assignee-login", "login", User.class)
	.mapAttribute("last-exec-on", "lastExecutedOn", IterationTestPlanItem.class)
	.mapAttribute("exec-mode", "automatedTest", TestCase.class).map("suite", "suitenames");

	@RequestMapping(value = "/iterations/{iterationId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long iterationId,
			@CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		Iteration iteration = iterationFinder.findById(iterationId);
		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/iterations/show-iteration-test-plan-manager");
		mav.addObject("iteration", iteration);
		mav.addObject("baseURL", "/iterations/" + iterationId);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable long iterationId, final DataTableDrawParameters params,
			final Locale locale) {

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, testPlanMapper);

		ColumnFiltering filter = new DataTableColumnFiltering(params);

		PagedCollectionHolder<List<IndexedIterationTestPlanItem>> holder = iterationTestPlanManagerService
				.findAssignedTestPlan(iterationId, paging, filter);

		return new TestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long iterationId) {
		iterationTestPlanManagerService.addTestCasesToIteration(testCasesIds, iterationId);
	}

	/**
	 * Fetches and returns a list of json test cases from an iteration id
	 * 
	 * @param iterationId
	 *            : the id of an {@link Iteration}
	 * @return the list of {@link JsonTestCase} representing the iteration's planned test-cases
	 * 
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-cases", method = RequestMethod.GET, headers = AcceptHeaders.CONTENT_JSON)
	public @ResponseBody
	List<JsonTestCase> getJsonTestCases(@PathVariable long iterationId, Locale locale) {
		List<TestCase> testCases = iterationFinder.findPlannedTestCases(iterationId);
		return jsonTestCaseBuilder.get().locale(locale).entities(testCases).toJson();
	}

	/***
	 * Method called when you drag a test case and change its position in the selected iteration
	 * 
	 * @param testPlanId
	 *            : the iteration owning the moving test plan items
	 * 
	 * @param itemIds
	 *            the ids of the items we are trying to move
	 * 
	 * @param newIndex
	 *            the new position of the first of them
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{itemIds}/position/{newIndex}", method = RequestMethod.POST)
	@ResponseBody
	public void moveTestPlanItems(@PathVariable("iterationId") long iterationId,
			@PathVariable("newIndex") int newIndex, @PathVariable("itemIds") List<Long> itemIds) {
		iterationTestPlanManagerService.changeTestPlanPosition(iterationId, newIndex, itemIds);

	}

	/**
	 * Will reorder the test plan according to the current sorting instructions.
	 * 
	 * @param iterationId
	 * @return
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/order", method = RequestMethod.POST)
	@ResponseBody
	public void reorderTestPlan(@PathVariable("iterationId") long iterationId, DataTableDrawParameters parameters) {

		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		iterationTestPlanManagerService.reorderTestPlan(iterationId, sorting);
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanItemsIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestPlanItemsFromIteration(@PathVariable("testPlanItemsIds") List<Long> testPlanItemsIds,
			@PathVariable long iterationId) {
		// check if a test plan item was already executed and therefore not removed
		Boolean response = iterationTestPlanManagerService.removeTestPlansFromIteration(testPlanItemsIds, iterationId);
		return response.toString();
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(openedNodes);

		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/iterations/{iterationId}/assignable-users", method = RequestMethod.GET)
	public @ResponseBody
	List<TestPlanAssignableUser> getAssignUserForIterationTestPlanItem(@PathVariable long iterationId,
			final Locale locale) {

		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);

		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<TestPlanAssignableUser>();

		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel));

		for (User user : usersList) {
			jsonUsers.add(new TestPlanAssignableUser(user));
		}

		return jsonUsers;
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = { "assignee" })
	public @ResponseBody
	Long assignUserToIterationTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = { "status" })
	public @ResponseBody
	JsonIterationTestPlanItem editStatusOfIterationTestPlanItems(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@RequestParam("status") String status) {
		List<IterationTestPlanItem> itpis = iterationTestPlanManagerService.forceExecutionStatus(testPlanIds, status);
		return createJsonITPI(itpis.get(0));

	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = { "dataset" })
	public @ResponseBody
	Long setDataset(@PathVariable("testPlanId") long testPlanId, @RequestParam("dataset") Long datasetId) {
		iterationTestPlanManagerService.changeDataset(testPlanId, JeditableComboHelper.coerceIntoEntityId(datasetId));
		return datasetId;
	}

	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}

	private JsonIterationTestPlanItem createJsonITPI(IterationTestPlanItem item) {
		String name = (item.isTestCaseDeleted()) ? null : item.getReferencedTestCase().getName();
		return new JsonIterationTestPlanItem(item.getId(), item.getExecutionStatus(), name, item.getLastExecutedOn(),
				item.getLastExecutedBy(), item.getUser(), item.isTestCaseDeleted(), item.isAutomated());
	}

}
