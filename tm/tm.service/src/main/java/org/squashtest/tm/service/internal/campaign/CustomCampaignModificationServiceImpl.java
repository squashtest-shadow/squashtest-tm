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
package org.squashtest.tm.service.internal.campaign;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.service.campaign.CustomCampaignModificationService;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.library.NodeManagementService;
import org.squashtest.tm.service.internal.repository.CampaignDao;

@Service("CustomCampaignModificationService")
@Transactional
public class CustomCampaignModificationServiceImpl implements CustomCampaignModificationService {

	@Inject
	private CampaignDao campaignDao;

	@Inject
	@Named("squashtest.tm.service.internal.CampaignManagementService")
	private NodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> campaignManagementService;

	public CustomCampaignModificationServiceImpl() {
		super();
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' ,'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void rename(long campaignId, String newName) {
		campaignManagementService.renameNode(campaignId, newName);
	}

	@Override
	public TestPlanStatistics findCampaignStatistics(long campaignId) {
		return campaignDao.findCampaignStatistics(campaignId);
	}
	
	
}
