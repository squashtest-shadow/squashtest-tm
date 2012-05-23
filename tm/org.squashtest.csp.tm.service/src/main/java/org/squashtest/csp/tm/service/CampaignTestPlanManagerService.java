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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * 
 * @author Agnes Durand
 */
public interface CampaignTestPlanManagerService {

	/**
	 * Find a campaign using its id
	 * 
	 * @param campaignId
	 */
	Campaign findCampaign(long campaignId);

	/**
	 * Returns a collection of {@link TestCaseLibrary}, the test cases of which may be added to the campaign
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	/**
	 * Adds a list of test cases to a campaign.
	 * 
	 * @param testCaseIds
	 * @param campaignId
	 */
	void addTestCasesToCampaignTestPlan(List<Long> testCaseIds, long campaignId);

	/**
	 * Get Users with Execute Access for a campaign and his test plans.
	 * 
	 * @param testCaseId
	 * @param campaignId
	 */
	List<User> findAssignableUserForTestPlan(long campaignId);

	/**
	 * Assign a user to the given test plan items
	 * 
	 * @param itemsIds
	 *            the test plan items to which assign a user
	 * @param campaignId
	 *            the campaign which test plan contains the items.
	 * @param userId
	 *            the assigned user
	 */
	void assignUserToTestPlanItem(long itemId, long campaignId, long userId);

	/**
	 * Assign a user to the given test plan items
	 * 
	 * @param itemsIds
	 *            the test plan items to which assign a user
	 * @param campaignId
	 *            the campaign which test plan contains the items.
	 * @param userId
	 *            the assigned user
	 */
	void assignUserToTestPlanItems(List<Long> itemsIds, long campaignId, long userId);

	/**
	 * 
	 * @param campaignId
	 *            the campaign which test plan we are about to modify.
	 * @param targetIndex
	 *            the index of the test plan to which we want to move the items
	 * @param itemIds
	 *            the ids of the items we want to move.
	 */
	void moveTestPlanItems(long campaignId, int targetIndex, List<Long> itemIds);

	/**
	 * @param campaignId
	 *            id of the campaign which test plan we will remove an item from
	 * @param itemId
	 *            id of the test plan item we want to remove
	 */
	void removeTestPlanItem(long campaignId, long itemId);

	/**
	 * @param campaignId
	 *            id of the campaign which test plan we will remove items from
	 * @param itemId
	 */
	void removeTestPlanItems(long campaignId, List<Long> itemIds);

	/**
	 * @param itemId
	 * @return
	 */
	CampaignTestPlanItem findById(long itemId);
}
