/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.campaign;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;

@Transactional
public interface CustomCampaignModificationService {

	void rename(long campaignId, String newName);

	List<Iteration> findIterationsByCampaignId(long campaignId);

	/**
	 * 
	 * @param campaignId the id of the concerned campaign
	 * @return the computed {@link TestPlanStatistics} out of each test-plan-item of each campaign's iteration
	 */
	TestPlanStatistics findCampaignStatistics(long campaignId);

	CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId);



	/* ********************** milestones section ******************* */

	void bindMilestones(long campaignId, Collection<Long> milestoneIds);

	void unbindMilestones(long campaignId, Collection<Long> milestoneIds);

	Collection<Milestone> findAssociableMilestones(long campaignId);

	Collection<Milestone> findAllMilestones(long campaignId);
}