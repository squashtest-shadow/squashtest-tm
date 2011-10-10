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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.service.CampaignTestPlanManagerService;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

/**
 * @author Agnes Durand
 */
@Controller
public class CampaignTestPlanManagerController {

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	private CampaignTestPlanManagerService campaignTestPlanManagerService;

	@ServiceReference
	public void setCampaignTestPlanManagerService(CampaignTestPlanManagerService campaignTestPlanManagerService) {
		this.campaignTestPlanManagerService = campaignTestPlanManagerService;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/campaign-test-plan-manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long campaignId) {

		Campaign campaign = campaignTestPlanManagerService.findCampaign(campaignId);
		List<TestCaseLibrary> linkableLibraries = campaignTestPlanManagerService.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		ModelAndView mav = new ModelAndView("page/campaigns/show-campaign-test-plan-manager");
		mav.addObject("campaign", campaign);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToCampaign(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long campaignId) {
		campaignTestPlanManagerService.addTestCasesToCampaignTestPlan(testCasesIds, campaignId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/non-belonging-test-cases", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void removeTestCasesFromCampaign(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testPlanIds,
			@PathVariable long campaignId) {
		campaignTestPlanManagerService.removeTestCasesFromCampaign(testPlanIds, campaignId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-cases/{testCaseId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeTestCaseFromCampaign(@PathVariable long testCaseId, @PathVariable long campaignId) {
		campaignTestPlanManagerService.removeTestCaseFromCampaign(testCaseId, campaignId);
	}
	
	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}
	
	@RequestMapping(value = "/campaigns/{campaignId}/test-cases/{testCaseId}/assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long testCaseId, @PathVariable long campaignId, @RequestParam long userId) {
		campaignTestPlanManagerService.assignUserToTestPlanItem(testCaseId, campaignId, userId);
	}
	
	@RequestMapping(value = "/campaigns/{campaignId}/assignable-user", method = RequestMethod.GET)
	public 
	ModelAndView getAssignUserForCampaignTestPlanItem(@RequestParam long testCaseId, @PathVariable long campaignId) {
		List<User> usersList =  campaignTestPlanManagerService.findAssignableUserForTestPlan(campaignId);
		CampaignTestPlanItem itp = campaignTestPlanManagerService.findTestPlanItemByTestCaseId(campaignId, testCaseId);
		
		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");
		
		mav.addObject("usersList", usersList);
		mav.addObject("selectIdentitier", "usersList");
		mav.addObject("selectClass", "userLogin");	
		mav.addObject("dataAssignUrl", "/campaigns/"+campaignId+"/test-cases/"+testCaseId+"/assign-user");
		
		if (itp != null && itp.getUser() != null){
			mav.addObject("testCaseAssignedLogin", itp.getUser().getLogin());
		}else{
			mav.addObject("testCaseAssignedLogin", null);
		}
		
		return mav;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/batch-assignable-user", method = RequestMethod.GET)
	public 
	ModelAndView getAssignUserForCampaignTestPlanItems(@PathVariable long campaignId) {
		List<User> userList =  campaignTestPlanManagerService.findAssignableUserForTestPlan(campaignId);
		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");
		mav.addObject("usersList", userList);
		mav.addObject("selectIdentitier", "comboUsersList");
		mav.addObject("testCaseAssignedLogin", null);
		mav.addObject("selectClass", "comboLogin");
		return mav;
	}

	
	@RequestMapping(value = "/campaigns/{campaignId}/batch-assign-user", method = RequestMethod.POST)
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testPlanIds, @PathVariable long campaignId, @RequestParam long userId) {
		campaignTestPlanManagerService.assignUserToTestPlanItems(testPlanIds, campaignId, userId);
	}
}
