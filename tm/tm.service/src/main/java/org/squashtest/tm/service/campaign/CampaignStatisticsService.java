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

import java.util.List;

import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignProgressionStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.IterationTestInventoryStatistics;

public interface CampaignStatisticsService {

	
	/**
	 * Given a campaign id, gathers and returns how many tests and at which status are planned in this campaign.
	 * Only tests part of an iteration count. Those statistics are grouped and sorted by Iteration.
	 * 
	 * @param campaignId
	 * @return
	 */
	List<IterationTestInventoryStatistics> gatherIterationTestInventoryStatistics(long campaignId);

	/**
	 * Given a milestone id (and so campaign ids), gathers and returns how many tests and at which status are planned in
	 * this campaign. Only tests part of an iteration count. Those statistics are grouped and sorted by Iteration.
	 * 
	 * @param milestoneId
	 * @return
	 */
	List<IterationTestInventoryStatistics> gatherIterationTestInventoryStatisticsByMilestone(long milestoneId);

	/**
	 * <p>Given a campaignId, gathers and return the theoterical and actual cumulative test count by iterations.
	 * The theoterical cumulative test count by iterations means how many tests should have been executed per day on 
	 * the basis of the scheduled start and end of an iteration. The actual cumulative test count means how many tests
	 * have been executed so far, each days, during the same period.</p>
	 * 
	 * <p>This assumes that the scheduled start and end dates of each iterations are square : they must all be defined, 
	 * and must not overlap. In case of errors appropriate messages will be filled instead and data won't be returned.</p>
	 *  
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignProgressionStatistics gatherCampaignProgressionStatistics(long campaignId);
	
	/**
	 * Given a campaign id, gathers all of the above in one package. 
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId);

	/**
	 * Given a campaign id, gathers all of the above in one package for a milestone.
	 * 
	 * @param milestoneId
	 * @return
	 */
	CampaignStatisticsBundle gatherCampaignStatisticsBundleByMilestone(long milestoneId);

	/**
	 * Given a campaign id, gathers and returns the number of test cases grouped by execution status.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignTestCaseStatusStatistics gatherCampaignTestCaseStatusStatistics(long campaignId);

	/**
	 * Given a milestone id (and so find some campaign id), gathers and returns the number of test cases grouped by
	 * execution status.
	 * 
	 * @param milestoneId
	 * @return
	 */
	CampaignTestCaseStatusStatistics gatherCampaignTestCaseStatusStatisticsByMilestone(long milestoneId);

	/**
	 * Given a campaign id, gathers and returns the number of non-executed test cases grouped by weight.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignNonExecutedTestCaseImportanceStatistics gatherCampaignNonExecutedTestCaseImportanceStatistics(long campaignId);

	/**
	 * Given a campaign id, gathers and returns the number of non-executed test cases grouped by weight.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignNonExecutedTestCaseImportanceStatistics gatherCampaignNonExecutedTestCaseImportanceStatisticsByMilestone(
			long milestoneId);

	/**
	 * Given a campaign id, gathers and returns the number of passed and failed test cases grouped by weight.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignTestCaseSuccessRateStatistics gatherCampaignTestCaseSuccessRateStatistics(long campaignId);

	/**
	 * Given a milestoneId (and so campaign id), gathers and returns the number of passed and failed test cases grouped
	 * by weight.
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignTestCaseSuccessRateStatistics gatherCampaignTestCaseSuccessRateStatisticsByMilestone(long milestoneId);

}
