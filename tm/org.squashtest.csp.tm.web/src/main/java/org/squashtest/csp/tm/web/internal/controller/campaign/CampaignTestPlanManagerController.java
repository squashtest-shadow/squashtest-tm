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

	private static final String ITEMS_IDS_REQUEST_PARAM = "itemsIds[]";

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;

	private CampaignTestPlanManagerService testPlanManager;

	@ServiceReference
	public void setTestPlanManager(CampaignTestPlanManagerService campaignTestPlanManagerService) {
		this.testPlanManager = campaignTestPlanManagerService;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long campaignId) {

		Campaign campaign = testPlanManager.findCampaign(campaignId);
		List<TestCaseLibrary> linkableLibraries = testPlanManager.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries);

		ModelAndView mav = new ModelAndView("page/campaigns/show-campaign-test-plan-manager");
		mav.addObject("campaign", campaign);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST, params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToCampaign(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long campaignId) {
		testPlanManager.addTestCasesToCampaignTestPlan(testCasesIds, campaignId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST, params = {
			"action=remove", ITEMS_IDS_REQUEST_PARAM })
	public @ResponseBody
	void removeItemsFromTestPlan(@PathVariable long campaignId,
			@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds) {
		testPlanManager.removeTestPlanItems(campaignId, itemsIds);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeItemFromTestPlan(@PathVariable long campaignId, @PathVariable long itemId) {
		testPlanManager.removeTestPlanItem(campaignId, itemId);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemId}/assigned-user", method = RequestMethod.POST, params = "userId")
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long itemId, @PathVariable long campaignId,
			@RequestParam long userId) {
		testPlanManager.assignUserToTestPlanItem(itemId, campaignId, userId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/assignable-user", method = RequestMethod.GET)
	public ModelAndView getAssignUserForCampaignTestPlanItem(@RequestParam long itemId, @PathVariable long campaignId) {
		List<User> usersList = testPlanManager.findAssignableUserForTestPlan(campaignId);
		CampaignTestPlanItem itp = testPlanManager.findById(itemId);

		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");

		mav.addObject("usersList", usersList);
		mav.addObject("selectIdentitier", "usersList");
		mav.addObject("selectClass", "userLogin");
		mav.addObject("dataAssignUrl", "/campaigns/" + campaignId + "/test-plan/" + itp.getId() + "/assigned-user");

		if (itp != null && itp.getUser() != null) {
			mav.addObject("testCaseAssignedLogin", itp.getUser().getLogin());
		} else {
			mav.addObject("testCaseAssignedLogin", null);
		}

		return mav;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/batch-assignable-user", method = RequestMethod.GET)
	public ModelAndView getAssignUserForCampaignTestPlanItems(@PathVariable long campaignId) {
		List<User> userList = testPlanManager.findAssignableUserForTestPlan(campaignId);
		ModelAndView mav = new ModelAndView("fragment/generics/test-plan-combo-box");
		mav.addObject("usersList", userList);
		mav.addObject("selectIdentitier", "comboUsersList");
		mav.addObject("testCaseAssignedLogin", null);
		mav.addObject("selectClass", "comboLogin");
		return mav;
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST, params = {
			"action=assign-user", ITEMS_IDS_REQUEST_PARAM, "userId" })
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds,
			@PathVariable long campaignId, @RequestParam long userId) {
		testPlanManager.assignUserToTestPlanItems(itemsIds, campaignId, userId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/index/{targetIndex}", method = RequestMethod.POST, params = {
			"action=move", ITEMS_IDS_REQUEST_PARAM })
	@ResponseBody
	public void moveTestPlanItems(@PathVariable long campaignId, @PathVariable int targetIndex,
			@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds) {
		testPlanManager.moveTestPlanItems(campaignId, targetIndex, itemsIds);
	}
}
