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
import java.util.Map;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.library.LibraryNavigationService;

public interface CampaignLibraryNavigationService extends
LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode>, CampaignLibraryFinderService {

	/**
	 * Adds a Campaign to the root of the library. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param campaign
	 */
	void addCampaignToCampaignLibrary(long libraryId, Campaign campaign);

	/**
	 * Adds a Campaign to the root of the Library, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 * 
	 * @param libraryId
	 * @param campaign
	 * @param customFieldValues
	 */
	void addCampaignToCampaignLibrary(long libraryId, Campaign campaign, Map<Long, RawValue> customFieldValues, Long milestoneId);



	/**
	 * Adds a campaign to a folder. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param campaign
	 */
	void addCampaignToCampaignFolder(long folderId, Campaign campaign);

	/**
	 * Adds a campaign to a folder, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 * 
	 * @param libraryId
	 * @param campaign
	 * @param customFieldValues
	 */
	void addCampaignToCampaignFolder(long folderId, Campaign campaign, Map<Long, RawValue> customFieldValues, Long milestoneId);

	void moveIterationsWithinCampaign(long destinationId, Long[] nodeIds, int position);

	/**
	 * @deprecated use {@linkplain CampaignFinder#findById(long)} instead
	 * @param campaignId
	 * @return
	 */
	@Deprecated
	Campaign findCampaign(long campaignId);

	/**
	 * Adds a new iteration to a campaign. Returns the index of the new iteration.
	 * 
	 * @param iteration
	 * @param campaignId
	 * @return
	 */
	int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan);


	/**
	 * Adds a new iteration to a campaign. Returns the index of the new iteration. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 * 
	 * 
	 * @param iteration
	 * @param campaignId
	 * @param customFieldValues
	 * @return
	 */
	int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan, Map<Long, RawValue> customFieldValues);


	List<Iteration> findIterationsByCampaignId(long campaignId);

	List<Iteration> copyIterationsToCampaign(long campaignId, Long[] iterationsIds);
	/**
	 * @deprecated use {@linkplain IterationFinder#findById(long)} instead
	 * @param iterationId
	 * @return
	 */
	@Deprecated
	Iteration findIteration(long iterationId);

	//FIXME move to TestSuiteFinder
	List<TestSuite> findIterationContent(long iterationId);

	/**
	 * @param
	 * @return
	 */
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
	OperationReport deleteIterations(List<Long> targetIds);
	/**
	 * that method should investigate the consequences of the deletion request of tes suites, and return a report about
	 * what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds);

	/**
	 * that method should delete test suites, and remove its references in iteration and iteration test plan item
	 * 
	 * @param testSuites
	 * @return
	 */
	OperationReport deleteSuites(List<Long> suiteIds);

	/**
	 * given a campaign Id, returns a model. It's made of rows and cell, and have a row header, check the relevant methods.
	 * Note that the actual model will differ according to the export type : "L" (light), "S" (standard), "F" (full).
	 * 
	 * @param campaignId
	 * @return
	 */
	CampaignExportCSVModel exportCampaignToCSV(Long campaignId, String exportType);

	List<String> getParentNodesAsStringList(Long elementId);

}
