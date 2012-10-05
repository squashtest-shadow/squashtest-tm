/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

public interface CampaignLibraryNavigationService extends
		LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode>, CampaignLibraryFinderService {

	void addCampaignToCampaignLibrary(long libraryId, Campaign campaign);

	void addCampaignToCampaignFolder(long folderId, Campaign campaign);

	Campaign findCampaign(long campaignId);

	/**
	 * Adds a new iteration to a campaign. Returns the index of the new iteration.
	 * 
	 * @param iteration
	 * @param campaignId
	 * @return
	 */
	int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan);

	List<Iteration> findIterationsByCampaignId(long campaignId);

	int copyIterationToCampaign(long campaignId, long iterationId);

	List<Iteration> copyIterationsToCampaign(long campaignId, Long[] iterationsIds);

	int moveIterationToNewCampaign(long newCampaignId, long oldCampaignId, long iterationId);

	Iteration findIteration(long iterationId);

	List<TestSuite> findIterationContent(long iterationId);

	List<CampaignLibrary> findLinkableCampaignLibraries();

	/**
	 * that method should investigate the consequences of the deletion request of iterations, and return a report about
	 * what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds);

	/**
	 * that method should delete the iterations. It still takes care of non deletable iterations so the implementation
	 * should filter out the ids who can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @return
	 */
	List<Long> deleteIterations(List<Long> targetIds);

	List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds);

	/**
	 * that method should delete test suites, and remove its references in iteration and iteration test plan item
	 * 
	 * @param testSuites
	 * @return
	 */
	List<Long> deleteSuites(List<Long> suiteIds);

}
