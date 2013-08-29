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
import org.squashtest.tm.domain.testcase.Dataset;
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
	private static final String TESTPLANS_IDS_REQUEST_PARAM = "testPlanIds[]";

	@Inject
	private TestSuiteModificationService service;
	private IterationFinder iterationFinder;
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
											.map		 ("entity-index", 	"index(IterationTestPlanItem)")		// index is a special case which means : no sorting.
											.mapAttribute("project-name",	"name", 			Project.class)
											.mapAttribute("reference", 		"reference", 		TestCase.class)
											.mapAttribute("tc-name", 		"name", 			TestCase.class)
											.mapAttribute("importance",		"importance", 		TestCase.class)
											.mapAttribute("dataset",		"name", 			Dataset.class)
											.mapAttribute("status",			"executionStatus", 	IterationTestPlanItem.class)
											.mapAttribute("assignee-login", "login", 			User.class)
											.mapAttribute("last-exec-on",	"lastExecutedOn",	IterationTestPlanItem.class)
											.mapAttribute("exec-mode", 		"automatedTest", 	TestCase.class);

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

	@RequestMapping(value = "/test-suites/{id}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long id, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {


		TestSuite testSuite = testSuiteTestPlanManagerService.findTestSuite(id);

		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/iterations/show-iteration-test-plan-manager");
		mav.addObject("testSuite", testSuite);
		mav.addObject("baseURL", "/test-suites/" + id );
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
	
	
	@RequestMapping(value = "/test-suites/{suiteId}/test-plan", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable long suiteId, final DataTableDrawParameters params, final Locale locale) {

		Paging paging = new DataTableSorting(params, testPlanMapper);

		PagedCollectionHolder<List<IterationTestPlanItem>> holder = service.findTestSuiteTestPlan(suiteId, paging);

		return new TestSuiteTestPlanTableModelHelper(messageSource, locale).buildDataModel(holder,
				params.getsEcho());

	}


	@RequestMapping(value = "/test-suites/{id}/assignable-users", method = RequestMethod.GET)
	@ResponseBody
	public List<TestPlanAssignableUser> getAssignUserForTestSuite(@PathVariable("id") long id, final Locale locale) {
		
		TestSuite testSuite = service.findById(id);
		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(testSuite.getIteration().getId());
		
		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<TestPlanAssignableUser>();

		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel));

		for (User user : usersList) {
			jsonUsers.add(new TestPlanAssignableUser(user));
		}

		return jsonUsers;
	}


	@RequestMapping(value = "/test-suites/{id}/test-plan/{testPlanIds}", method = RequestMethod.POST, params = {"assignee"})
	public @ResponseBody
	long assignUserToCampaignTestPlanItem(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable("id") long id, 
			@RequestParam("assignee") long assignee) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, assignee);
		return assignee;
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

	@RequestMapping(value = "/test-suites/{id}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long id, @PathVariable long iterationId) {
		testSuiteTestPlanManagerService.addTestCasesToIterationAndTestSuite(testCasesIds, id);
	}

	@RequestMapping(value = "/test-suites/{id}/test-plan/{testPlanIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestCaseFromTestSuiteAndIteration(@PathVariable("testPlanIds") List<Long> testPlanIds,
			@PathVariable("id") long id) {
		// check if a test plan was already executed and therefore not removed from the iteration
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(
				testPlanIds, id);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{id}/test-plan/{testPlanIds}", method = RequestMethod.DELETE, params={"detach=true"})
	public @ResponseBody
	String detachTestCaseFromTestSuite(@PathVariable("testPlanIds") List<Long> testPlanIds, @PathVariable long id) {
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlanIds, id);
		return FALSE;
	}


	@RequestMapping(value = "/test-suites/{suiteIds}/test-plan", method = RequestMethod.POST, params = { "itemIds[]"})
	public @ResponseBody
	Map<String, List<Long>> bindTestPlan(@RequestParam("itemIds") List<Long> itpIds,
			@PathVariable("suiteIds") List<Long> suitesIds) {
		service.bindTestPlanToMultipleSuites(suitesIds, itpIds);
		Map<String, List<Long>> result = new HashMap<String, List<Long>>();
		result.put("ids", suitesIds);
		return result;
	}

	private String formatUnassigned(Locale locale) {
		return messageSource.internationalize("label.Unassigned", locale);
	}
	

}
