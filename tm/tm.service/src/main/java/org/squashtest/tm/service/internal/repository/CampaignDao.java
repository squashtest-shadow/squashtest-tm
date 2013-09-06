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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;

public interface CampaignDao extends EntityDao<Campaign> {

	Campaign findByIdWithInitializedIterations(long campaignId);

	List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(long campaignId, PagingAndSorting filter);
	
	
	List<CampaignTestPlanItem> findTestPlan(long campaignId, PagingAndMultiSorting sorting);
	
	
	/**
	 * Returns the paged list of [index, CampaignTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 * 
	 * @param campaignId
	 * @param sorting
	 * @return
	 */
	List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting);
	
	/**
	 * Returns the paged list of [index, CampaignTestPlanItem] wrapped in an {@link IndexedIterationTestPlanItem}
	 * 
	 * @param campaignId
	 * @param sorting
	 * @return
	 */
	List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndSorting sorting);
	
	
	long countTestPlanById(long campaignId);

	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);

	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	List<String> findNamesInCampaignStartingWith(long campaignId, String nameStart);
	
	List<String> findAllNamesInCampaign(long campaignId);
	
	
	/**
	 * Finds all {@link CampaignLibraryNode} which name contains the given token.
	 * 
	 * @param tokenInName
	 * @param groupByProject
	 * @return
	 */
	List<CampaignLibraryNode> findAllByNameContaining(String tokenInName, boolean groupByProject);
	
	/**
	 * find all the campaign's iterations, and return all iteration's executions regardless of the campaign test-plan
	 * 
	 * @param campaignId
	 * @return list of executions of all iterations
	 */
	List<Execution> findAllExecutionsByCampaignId(Long campaignId);
	/**
	 * 
	 * @param campaignId the id of the concerned campaign
	 * @return the computed {@link TestPlanStatistics} out of each test-plan-item of each campaign's iteration
	 */
	TestPlanStatistics findCampaignStatistics(long campaignId);

	
	long countRunningOrDoneExecutions(long campaignId);
}
