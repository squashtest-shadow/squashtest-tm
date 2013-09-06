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
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.web.internal.controller.RequestParams;
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
 * @author Agnes Durand
 */
@Controller
public class CampaignTestPlanManagerController {

	private static final String ITEMS_IDS_REQUEST_PARAM = "itemIds[]";

	private static final String TESTCASES_IDS_REQUEST_PARAM = "testCasesIds[]";

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder; 

	@Inject
	private CampaignTestPlanManagerService testPlanManager;	

	@Inject
	private InternationalizationHelper messageSource;
	


	private final DatatableMapper<String> testPlanMapper = new NameBasedMapper()
			.map		 ("entity-index", 	"index(CampaignTestPlanItem)")
			.mapAttribute("project-name", 	"name", 			Project.class)
			.mapAttribute("reference", 		"reference", 		TestCase.class)
			.mapAttribute("tc-name", 		"name", 			TestCase.class)
			.mapAttribute("assigned-user", 	"login", 			User.class)
			.mapAttribute("importance",		"importance", 		TestCase.class)
			.mapAttribute("exec-mode", 		"automatedTest", 	TestCase.class);



	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/manager", method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long campaignId, @CookieValue(value = "jstree_open", required = false, defaultValue = "") String[] openedNodes) {

		Campaign campaign = testPlanManager.findCampaign(campaignId);
		List<TestCaseLibrary> linkableLibraries = testPlanManager.findLinkableTestCaseLibraries();

		List<JsTreeNode> linkableLibrariesModel = createLinkableLibrariesModel(linkableLibraries, openedNodes);

		ModelAndView mav = new ModelAndView("page/campaigns/show-campaign-test-plan-manager");
		mav.addObject("campaign", campaign);
		mav.addObject("linkableLibrariesModel", linkableLibrariesModel);
		return mav;
	}
	

	@RequestMapping(value = "campaigns/{campaignId}/test-plan/table", params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getTestCasesTableModel(@PathVariable("campaignId") long campaignId,
			final DataTableDrawParameters params, final Locale locale) {
		DataTableMultiSorting filter = new DataTableMultiSorting(params, testPlanMapper);

		PagedCollectionHolder<List<IndexedCampaignTestPlanItem>> holder = testPlanManager.findTestPlan(campaignId, filter);

		return new CampaignTestPlanTableModelHelper(messageSource, locale).buildDataModel(holder, 	params.getsEcho());
	}


	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST, 
			params = TESTCASES_IDS_REQUEST_PARAM)
	public @ResponseBody
	void addTestCasesToCampaign(@RequestParam(TESTCASES_IDS_REQUEST_PARAM) List<Long> testCasesIds,
			@PathVariable long campaignId) {
		testPlanManager.addTestCasesToCampaignTestPlan(testCasesIds, campaignId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan", method = RequestMethod.POST, 
			params = {"action=remove", ITEMS_IDS_REQUEST_PARAM })
	public @ResponseBody
	void removeItemsFromTestPlan(@PathVariable("campaignId") long campaignId,
			@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds) {
		testPlanManager.removeTestPlanItems(campaignId, itemsIds);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeItemFromTestPlan(@PathVariable long campaignId, @PathVariable long itemId) {
		testPlanManager.removeTestPlanItem(campaignId, itemId);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries, String[] openedNodes) {
		MultiMap expansionCandidates =  JsTreeHelper.mapIdsByType(openedNodes);
		
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.expand(expansionCandidates).setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/{itemId}/assign-user", method = RequestMethod.POST, params = "userId")
	public @ResponseBody
	void assignUserToCampaignTestPlanItem(@PathVariable long itemId, @PathVariable long campaignId,
			@RequestParam long userId) {
		testPlanManager.assignUserToTestPlanItem(itemId, campaignId, userId);
	}

	
	@RequestMapping(value = "/campaigns/{campaignId}/assignable-users", method = RequestMethod.GET)
	public @ResponseBody List<TestPlanAssignableUser> getAssignUserForCampaignTestPlanItem(
			@PathVariable("campaignId") long campaignId, final Locale locale) {
		
		List<User> usersList = testPlanManager.findAssignableUserForTestPlan(campaignId);
		
		String unassignedLabel = formatUnassigned(locale);
		List<TestPlanAssignableUser> jsonUsers = new LinkedList<TestPlanAssignableUser>();
		
		jsonUsers.add(new TestPlanAssignableUser(User.NO_USER_ID.toString(), unassignedLabel ));
		
		for (User user : usersList){
			jsonUsers.add(new TestPlanAssignableUser(user));
		}
		
		return jsonUsers;
	}
	

	@RequestMapping(value = "/campaigns/{campaignId}/batch-assign-user", method = RequestMethod.POST, params = {
			ITEMS_IDS_REQUEST_PARAM, "userId" })
	public @ResponseBody
	void assignUserToCampaignTestPlanItems(@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds,
			@PathVariable long campaignId, @RequestParam long userId) {
		testPlanManager.assignUserToTestPlanItems(itemsIds, campaignId, userId);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/move", method = RequestMethod.POST, 
			params = {ITEMS_IDS_REQUEST_PARAM, "newIndex" })
	@ResponseBody
	public void moveTestPlanItems(@PathVariable long campaignId, @RequestParam("newIndex") int newIndex,
			@RequestParam(ITEMS_IDS_REQUEST_PARAM) List<Long> itemsIds) {
		testPlanManager.moveTestPlanItems(campaignId, newIndex, itemsIds);
	}
	
	
	/**
	 * Will reorder the test plan according to the current sorting instructions.
	 * 
	 * @param iterationId
	 * @return
	 */
	@RequestMapping(value = "/campaigns/{campaignId}/test-plan/order", method = RequestMethod.POST)
	@ResponseBody
	public void reorderTestPlan(@PathVariable("campaignId") long campaignId, DataTableDrawParameters parameters){
		
		PagingAndMultiSorting sorting = new DataTableMultiSorting(parameters, testPlanMapper);
		testPlanManager.reorderTestPlan(campaignId, sorting);
	}
	
	private String formatUnassigned(Locale locale){
		return messageSource.getMessage("label.Unassigned", null, locale);
	}
	
}
