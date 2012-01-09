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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface CampaignDao extends EntityDao<Campaign> {

	Campaign findByIdWithInitializedIterations(long campaignId);

	List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(long campaignId, CollectionSorting filter);

	long countTestPlanById(long campaignId);

	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);

	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	List<String> findNamesInCampaignStartingWith(long campaignId,
			String nameStart);
	
	List<String> findAllNamesInCampaign(long campaignId);
	
	List<Campaign> findAllCampaign();

	/**
	 * Finds all {@link CampaignLibraryNode} which name contains the given token.
	 * 
	 * @param tokenInName
	 * @param groupByProject
	 * @return
	 */
	List<CampaignLibraryNode> findAllByNameContaining(String tokenInName, boolean groupByProject);
	
	List<Campaign> findAllById(List<Long> campaignIds);

}
