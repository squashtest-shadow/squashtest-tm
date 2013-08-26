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
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.jquery.TestPlanAssignableUser;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

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


	@RequestMapping(value = "/iterations/{iterationId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long iterationId, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		Iteration iteration = iterationFinder.findById(iterationId);
		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/iterations/show-iteration-test-plan-manager");
		mav.addObject("iteration", iteration);
		mav.addObject("baseURL", "/iterations/" + iterationId);
		mav.addObject("useIterationTable", true);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long iterationId) {
		iterationTestPlanManagerService.addTestCasesToIteration(testCasesIds, iterationId);
	}

	@Inject
	private Provider<JsonTestCaseBuilder> jsonTestCaseBuilder;

	/**
	 * Fetches and returns a list of json test cases from an iteration id
	 * 
	 * @param iterationId
	 *            : the id of an {@link Iteration}
	 * @return the list of {@link JsonTestCase} representing the iteration's planned test-cases
	 * 
	 */
	@RequestMapping(value = "/iterations/{iterationId}/test-cases", method = RequestMethod.GET, headers = "Accept=application/json, text/javascript")
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


	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestCaseFromIteration(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable long iterationId) {
		// check if a test plan was already executed and therefore not removed
		Boolean response = iterationTestPlanManagerService.removeTestPlansFromIteration(testPlanIds, iterationId);
		return response.toString();
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates =  JsTreeHelper.mapIdsByType(openedNodes);
		
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
	
	
	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"assignee"})
	public @ResponseBody
	Long assignUserToCampaignTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable("iterationId") long iterationId,
			@RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
	}


	@RequestMapping(value = "/iterations/{iterationId}/test-plan/{testPlanId}", method = RequestMethod.POST, params = {"status"})
	public @ResponseBody
	String setTestPlanItemStatus(@PathVariable("testPlanId") long testPlanId, 
										  @PathVariable("iterationId") long iterationId,
										  @RequestParam("status") String status) {
		iterationTestPlanManagerService.assignExecutionStatusToTestPlanItem(testPlanId, status);
		return status;
	}



	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}


}
