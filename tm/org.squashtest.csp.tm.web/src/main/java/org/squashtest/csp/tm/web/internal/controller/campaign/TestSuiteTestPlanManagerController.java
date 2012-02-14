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
import java.util.List;
import java.util.Locale;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;

/**
 *
 * @author R.A
 */
@Controller
public class TestSuiteTestPlanManagerController {

	private static final String TESTPLANS_IDS_REQUEST_PARAM = "testPlanIds[]";

//	private TestSuiteTestPlanManagerService testSuiteTestPlanManagerService;
	private IterationTestPlanManagerService iterationTestPlanManagerService;

//	@Inject
//	private MessageSource messageSource;

	@ServiceReference
	public void setCampaignTestPlanManagerService(IterationTestPlanManagerService iterationTestPlanManagerService) {
		this.iterationTestPlanManagerService = iterationTestPlanManagerService;
	}

//	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class, 
//			TestCase.class,	Project.class, TestSuite.class).initMapping(9)
//			.mapAttribute(Project.class, 2, "name", String.class)
//			.mapAttribute(TestCase.class, 3, "name", String.class)
//			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class)
//			.mapAttribute(IterationTestPlanItem.class, 5, "executionStatus", ExecutionStatus.class)
//			.mapAttribute(IterationTestPlanItem.class, 6, "lastExecutedBy", String.class)
//			.mapAttribute(IterationTestPlanItem.class, 7, "lastExecutedOn", Date.class);

	@RequestMapping(value = "/testSuite/{id}/{iterationId}/test-case/{testPlanId}/assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long testPlanId, @PathVariable long id, 
			@PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItem(testPlanId, iterationId, userId);
	}

	@RequestMapping(value = "/testSuite/{id}/{iterationId}/batch-assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(TESTPLANS_IDS_REQUEST_PARAM) List<Long> testPlanIds, @PathVariable long id, 
			@PathVariable long iterationId, @RequestParam long userId) {
		iterationTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, iterationId, userId);
	}
	
	@RequestMapping(value = "/testSuite/{id}/{iterationId}/assignable-user", method = RequestMethod.GET)
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
		mav.addObject("dataAssignUrl", "/testSuite/"+id+"/"+iterationId+"/test-case/"+testPlanId+"/assign-user");

		if (itp.getUser() != null){
			mav.addObject("testCaseAssignedLogin", itp.getUser().getLogin());
		}else{
			mav.addObject("testCaseAssignedLogin", null);
		}

		return mav;
	}

	@RequestMapping(value = "/testSuite/{id}/{iterationId}/batch-assignable-user", method = RequestMethod.GET)
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

//	@RequestMapping(value = "/testSuite/{id}/toto", params = "sEcho")
//	public @ResponseBody
//	DataTableModel getIterationTableModel(@PathVariable Long id, final DataTableDrawParameters params,
//			final Locale locale) {
//		
//		Paging paging = new DataTableMapperPagingAndSortingAdapter(params, testPlanMapper);
//		
//		PagedCollectionHolder<List<IterationTestPlanItem>> holder = testSuiteTestPlanManagerService.findTestPlan(id, paging);
//
//		return new DataTableModelHelper<IterationTestPlanItem>() {
//			@Override
//			public Object[] buildItemData(IterationTestPlanItem item) {
//
//				String projectName;
//				String testCaseName;
//				String testCaseExecutionMode;
//				String testCaseId;
//
//				if (item.isTestCaseDeleted()){
//					projectName=formatNoData(locale);
//					testCaseName=formatDeleted(locale);
//					testCaseExecutionMode=formatNoData(locale);
//					testCaseId="";
//				}
//				else{
//					projectName=item.getReferencedTestCase().getProject().getName();
//					testCaseName=item.getReferencedTestCase().getName();
//					testCaseExecutionMode = formatExecutionMode(item.getReferencedTestCase().getExecutionMode(), locale);
//					testCaseId=item.getReferencedTestCase().getId().toString();
//				}			
//				
//				return new Object[]{
//						item.getId(),
//						getCurrentIndex(),
//						projectName,
//						testCaseName,
//						testCaseExecutionMode,
//						testCaseId,
//						item.isTestCaseDeleted(),
//						" "
//
//				};
//
//			}
//		}.buildDataModel(holder, params.getsEcho());
//
//	}



//	private String formatNoData(Locale locale) {
//		return messageSource.getMessage("squashtm.nodata", null, locale);
//	}
//
//
//	private String formatDeleted(Locale locale){
//		return messageSource.getMessage("squashtm.itemdeleted", null, locale);
//	}
//
//	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
//		return messageSource.getMessage(mode.getI18nKey(), null, locale);
//	}
}
