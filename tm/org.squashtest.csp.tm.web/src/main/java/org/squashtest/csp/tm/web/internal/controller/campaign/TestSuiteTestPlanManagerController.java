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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;

/**
 *
 * @author R.A
 */
@Controller
public class TestSuiteTestPlanManagerController {

	private static final String FALSE = "false";
	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";
	private static final String TESTPLANS_IDS_REQUEST_PARAM = "testPlanIds[]";
	
	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;
	private IterationTestPlanManagerService iterationTestPlanManagerService;
	
	
	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class,
			TestCase.class, Project.class, TestSuite.class).initMapping(9)
			.mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(IterationTestPlanItem.class, 5, "executionStatus", ExecutionStatus.class)
			.mapAttribute(IterationTestPlanItem.class, 6, "lastExecutedBy", String.class)
			.mapAttribute(IterationTestPlanItem.class, 7, "lastExecutedOn", Date.class);

	@Inject
	private MessageSource messageSource;
	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	@ServiceReference
	public void setIterationTestPlanManagerService(IterationTestPlanManagerService iterationTestPlanManagerService) {
		this.iterationTestPlanManagerService = iterationTestPlanManagerService;
	}
	
	@ServiceReference
	public void setTestSuiteTestPlanManagerService(TestSuiteTestPlanManagerService testSuiteTestPlanManagerService) {
		this.testSuiteTestPlanManagerService = testSuiteTestPlanManagerService;
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long id, @PathVariable long iterationId) {

		Iteration iteration = iterationTestPlanManagerService.findIteration(iterationId);
		TestSuite testSuite = testSuiteTestPlanManagerService.findTestSuite(id);
		
		List<TestCaseLibrary> linkableLibraries = iterationTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		ModelAndView mav = new ModelAndView("page/iterations/show-iteration-test-plan-manager");
		mav.addObject("iteration", iteration);
		mav.addObject("testSuite", testSuite);
		mav.addObject("baseURL", "/test-suites/" + id + "/" + iterationId );
		mav.addObject("useIterationTable", false);
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
	
	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-case/{testPlanId}/assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long testPlanId, @PathVariable long id, 
			@PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItem(testPlanId, iterationId, userId);
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/batch-assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(TESTPLANS_IDS_REQUEST_PARAM) List<Long> testPlanIds, @PathVariable long id, 
			@PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, iterationId, userId);
	}
	
	@RequestMapping(value = "/test-suites/{id}/{iterationId}/assignable-user", method = RequestMethod.GET)
	public
	ModelAndView getAssignUserForIterationTestPlanItem(@RequestParam("testPlanId") long testPlanId, @PathVariable long id, 
			@PathVariable long iterationId,final Locale locale) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(testPlanId);
		List<User> usersList =  iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		IterationTestPlanItem itp = iterationTestPlanManagerService.findTestPlanItem(iterationId, testPlanId);

		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");

		mav.addObject("usersList", usersList);
		mav.addObject("selectIdentitier", "usersList"+testPlanId);
		mav.addObject("selectClass", "userLogin");
		mav.addObject("dataAssignUrl", "/test-suites/"+id+"/"+iterationId+"/test-case/"+testPlanId+"/assign-user");

		if (itp.getUser() != null){
			mav.addObject("testCaseAssignedLogin", itp.getUser().getLogin());
		}else{
			mav.addObject("testCaseAssignedLogin", null);
		}

		return mav;
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/batch-assignable-user", method = RequestMethod.GET)
	public
	ModelAndView getAssignUserForIterationTestPlanItems(@PathVariable long id, @PathVariable long iterationId, final Locale locale) {

		List<User> userList =  iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");
		mav.addObject("usersList", userList);
		mav.addObject("selectIdentitier", "comboUsersList");
		mav.addObject("testCaseAssignedLogin", null);
		mav.addObject("selectClass", "comboLogin");
		return mav;
	}
	
	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToIteration(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long id, @PathVariable long iterationId) {
		testSuiteTestPlanManagerService.addTestCasesToIterationAndTestSuite(testCasesIds, id);
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan/remove/delete/{testPlanId}", method = RequestMethod.DELETE)
	public @ResponseBody
	String removeTestCaseFromTestSuiteAndIteration(@PathVariable("testPlanId") long testPlanId, @PathVariable long id) {
		// check if a test plan was already executed and therefore not removed from the iteration
		List<Long> testPlanIds = new ArrayList<Long>();
		testPlanIds.add(testPlanId);
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(testPlanIds, id);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-plan/remove/detach/{testPlanId}", method = RequestMethod.DELETE)
	public @ResponseBody
	String detachTestCaseFromTestSuite(@PathVariable("testPlanId") long testPlanId, @PathVariable long id) {
		List<Long> testPlanIds = new ArrayList<Long>();
		testPlanIds.add(testPlanId);
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlanIds, id);
		return FALSE;
	}
	
	@RequestMapping(value = "/test-suites/{id}/{iterationId}/non-belonging-test-cases/remove/delete", method = RequestMethod.POST, params = TESTPLANS_IDS_REQUEST_PARAM)
	public @ResponseBody
	String removeTestCasesFromTestSuiteAndIteration(@RequestParam(TESTPLANS_IDS_REQUEST_PARAM) List<Long> testPlansIds, 
			@PathVariable long id) {
		// check if a test plan was already executed and therefore not removed from the iteration
		Boolean response = testSuiteTestPlanManagerService.detachTestPlanFromTestSuiteAndRemoveFromIteration(testPlansIds, id);
		return response.toString();
	}

	@RequestMapping(value = "/test-suites/{id}/{iterationId}/non-belonging-test-cases/remove/detach", method = RequestMethod.POST, params = TESTPLANS_IDS_REQUEST_PARAM)
	public @ResponseBody
	String detachTestCasesFromTestSuite(@RequestParam(TESTPLANS_IDS_REQUEST_PARAM) List<Long> testPlansIds,
			@PathVariable long id) {
		testSuiteTestPlanManagerService.detachTestPlanFromTestSuite(testPlansIds, id);
		return FALSE;
	}
	
	@RequestMapping(value = "/test-suites/{id}/{iterationId}/test-cases/table", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable Long id, final DataTableDrawParameters params,
			final Locale locale) {

		Paging paging = new DataTableMapperPagingAndSortingAdapter(params, testPlanMapper);
		
		PagedCollectionHolder<List<IterationTestPlanItem>> holder = testSuiteTestPlanManagerService.findTestPlan(
				id, paging);
		
		return new DataTableModelHelper<IterationTestPlanItem>() {
			@Override
			public Object[] buildItemData(IterationTestPlanItem item) {

				String projectName;
				String testCaseName;
				String testCaseExecutionMode;
				String testCaseId;

				if (item.isTestCaseDeleted()) {
					projectName = formatNoData(locale);
					testCaseName = formatDeleted(locale);
					testCaseExecutionMode = formatNoData(locale);
					testCaseId = "";
				} else {
					projectName = item.getReferencedTestCase().getProject().getName();
					testCaseName = item.getReferencedTestCase().getName();
					testCaseExecutionMode = formatExecutionMode(item.getReferencedTestCase().getExecutionMode(), locale);
					testCaseId = item.getReferencedTestCase().getId().toString();
				}
				
				return new Object[] { item.getId(), 
						getCurrentIndex(), 
						projectName, 
						testCaseName,
						testCaseExecutionMode, 
						testCaseId, 
						item.isTestCaseDeleted(), 
						" "
				};
			}
		}.buildDataModel(holder, params.getsEcho());

	}
	
/* ***************** data formatter *************************** */

	private String formatNoData(Locale locale) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.getMessage("squashtm.itemdeleted", null, locale);
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.getMessage(mode.getI18nKey(), null, locale);
	}
}
