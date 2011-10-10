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
package org.squashtest.csp.tm.internal.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.service.CampaignModificationService;

@Service("squashtest.tm.service.CampaignModificationService")
@Transactional
public class CampaignModificationServiceImpl implements CampaignModificationService {

	@Inject
	private CampaignDao campaignDao;

	@Inject
	@Named("squashtest.tm.service.internal.CampaignManagementService")
	private NodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> campaignManagementService;

	public CampaignModificationServiceImpl() {
		super();
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
			"or hasRole('ROLE_ADMIN')")
	public void updateDescription(long campaignId, String newDescription) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setDescription(newDescription);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	public Campaign findById(long campaignId) {
		return campaignDao.findById(campaignId);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
			"or hasRole('ROLE_ADMIN')")
	public void rename(long campaignId, String newName) {
		campaignManagementService.renameNode(campaignId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
			"or hasRole('ROLE_ADMIN')")
	public void setScheduledStartDate(long campaignId, Date scheduledStart) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setScheduledStartDate(scheduledStart);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
			"or hasRole('ROLE_ADMIN')")
	public void setScheduledEndDate(long campaignId, Date scheduledEnd) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setScheduledEndDate(scheduledEnd);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
		"or hasRole('ROLE_ADMIN')")
	public void setActualStartDate(long campaignId, Date actualStart) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setActualStartDate(actualStart);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
		"or hasRole('ROLE_ADMIN')")
	public void setActualEndDate(long campaignId, Date actualEnd) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setActualEndDate(actualEnd);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
			"or hasRole('ROLE_ADMIN')")
	public void setActualStartAuto(long campaignId, boolean isAuto) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setActualStartAuto(isAuto);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'WRITE') " +
		"or hasRole('ROLE_ADMIN')")
	public void setActualEndAuto(long campaignId, boolean isAuto) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.setActualEndAuto(isAuto);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' ,'READ') " +
			"or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<CampaignTestPlanItem>> findTestPlanByCampaignId(
			long campaignId, CollectionSorting filter){
		List<CampaignTestPlanItem> tcs = campaignDao.findAllTestPlanByIdFiltered(campaignId, filter);
		long count = campaignDao.countTestPlanById(campaignId);
		return new FilteredCollectionHolder<List<CampaignTestPlanItem>>(count, tcs);
	}
}
