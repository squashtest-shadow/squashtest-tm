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

import java.util.Date;
import java.util.List;

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

public interface CampaignModificationService {

	Campaign findById(long campaignId);

	void updateDescription(long campaignId, String newDescription);

	void rename(long campaignId, String newName);

	void setScheduledStartDate(long campaignId, Date scheduledStart);

	void setScheduledEndDate(long campaignId, Date scheduledEnd);

	void setActualStartDate(long campaignId, Date actualStart);

	void setActualEndDate(long campaignId, Date actualEnd);

	void setActualStartAuto(long campaignId, boolean isAuto);

	void setActualEndAuto(long campaignId, boolean isAuto);

	FilteredCollectionHolder<List<CampaignTestPlanItem>> findTestPlanByCampaignId(
			long campaignId, CollectionSorting filter);

	/**** Todo : CampaignIterations, User and Attachment lists ***/

}
