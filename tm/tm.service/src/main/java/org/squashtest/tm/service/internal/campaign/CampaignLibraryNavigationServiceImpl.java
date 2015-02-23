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
package org.squashtest.tm.service.internal.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.CampaignLibraryDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.SecurityCheckableObject;

@Service("squashtest.tm.service.CampaignLibraryNavigationService")
@Transactional
public class CampaignLibraryNavigationServiceImpl extends
AbstractLibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> implements
CampaignLibraryNavigationService {

	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";

	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private CampaignFolderDao campaignFolderDao;

	@Inject
	@Qualifier("squashtest.tm.repository.CampaignLibraryNodeDao")
	private LibraryNodeDao<CampaignLibraryNode> campaignLibraryNodeDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private TestSuiteDao suiteDao;

	@Inject
	private IterationModificationService iterationModificationService;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private Provider<SimpleCampaignExportCSVModelImpl> simpleCampaignExportCSVModelProvider;

	@Inject
	private Provider<CampaignExportCSVModelImpl> standardCampaignExportCSVModelProvider;

	@Inject
	private Provider<CampaignExportCSVFullModelImpl> fullCampaignExportCSVModelProvider;

	@Inject
	@Qualifier("squashtest.tm.service.CampaignLibrarySelectionStrategy")
	private LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> libraryStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignFolderStrategy")
	private Provider<PasteStrategy<CampaignFolder, CampaignLibraryNode>> pasteToCampaignFolderStrategyProvider;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignLibraryStrategy")
	private Provider<PasteStrategy<CampaignLibrary, CampaignLibraryNode>> pasteToCampaignLibraryStrategyProvider;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToCampaignStrategy")
	private Provider<PasteStrategy<Campaign, Iteration>> pasteToCampaignStrategyProvider;

	@Inject
	private MilestoneMembershipManager milestoneManager;

	@Override
	protected NodeDeletionHandler<CampaignLibraryNode, CampaignFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	protected PasteStrategy<CampaignFolder, CampaignLibraryNode> getPasteToFolderStrategy() {
		return pasteToCampaignFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<CampaignLibrary, CampaignLibraryNode> getPasteToLibraryStrategy() {
		return pasteToCampaignLibraryStrategyProvider.get();
	}

	@Override
	@PreAuthorize("(hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE')) "
			+ OR_HAS_ROLE_ADMIN)
	public List<Iteration> copyIterationsToCampaign(long campaignId, Long[] iterationsIds) {
		PasteStrategy<Campaign, Iteration> pasteStrategy = pasteToCampaignStrategyProvider.get();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(campaignId, Arrays.asList(iterationsIds));
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') " + OR_HAS_ROLE_ADMIN)
	public Campaign findCampaign(long reqId) {
		return campaignDao.findById(reqId);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan) {
		Campaign campaign = campaignDao.findById(campaignId);

		if (!campaign.isContentNameAvailable(iteration.getName())) {
			throw new DuplicateNameException(iteration.getName(), iteration.getName());
		}
		return iterationModificationService.addIterationToCampaign(iteration, campaignId, copyTestPlan);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan,
			Map<Long, RawValue> customFieldValues) {
		int iterIndex = addIterationToCampaign(iteration, campaignId, copyTestPlan);
		initCustomFieldValues(iteration, customFieldValues);
		return iterIndex;
	}

	@Override
	protected final CampaignLibraryDao getLibraryDao() {
		return campaignLibraryDao;
	}

	@Override
	protected final CampaignFolderDao getFolderDao() {
		return campaignFolderDao;
	}

	@Override
	protected final LibraryNodeDao<CampaignLibraryNode> getLibraryNodeDao() {
		return campaignLibraryNodeDao;
	}

	/*
	 * refer to the comment in
	 * org.squashtest.csp.tm.internal.service.TestCaseModificationServiceImpl#findVerifiedRequirementsByTestCaseId
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.CampaignLibraryNavigationService#findIterationsByCampaignId(long)
	 */
	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return iterationModificationService.findIterationsByCampaignId(campaignId);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.campaign.CampaignLibrary', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignLibrary(long libraryId, Campaign newCampaign) {
		CampaignLibrary library = campaignLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			library.addContent(newCampaign);
			campaignDao.persist(newCampaign);
			createCustomFieldValues(newCampaign);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.campaign.CampaignLibrary', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignLibrary(long libraryId, Campaign campaign, Map<Long, RawValue> customFieldValues, List<Long> milestoneIds) {
		addCampaignToCampaignLibrary(libraryId, campaign);
		initCustomFieldValues(campaign, customFieldValues);
		milestoneManager.bindCampaignToMilestones(campaign.getId(), milestoneIds);
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignFolder(long folderId, Campaign newCampaign) {
		CampaignFolder folder = campaignFolderDao.findById(folderId);
		if (!folder.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			folder.addContent(newCampaign);
			campaignDao.persist(newCampaign);
			createCustomFieldValues(newCampaign);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignFolder(long folderId, Campaign campaign, Map<Long, RawValue> customFieldValues, List<Long> milestoneIds) {

		addCampaignToCampaignFolder(folderId, campaign);
		initCustomFieldValues(campaign, customFieldValues);
		milestoneManager.bindCampaignToMilestones(campaign.getId(), milestoneIds);

	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	public Iteration findIteration(long iterationId) {
		return iterationDao.findById(iterationId);

	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	public List<TestSuite> findIterationContent(long iterationId) {
		return suiteDao.findAllByIterationId(iterationId);
	}

	@Override
	public String getPathAsString(long entityId) {
		// get
		CampaignLibraryNode node = getLibraryNodeDao().findById(entityId);

		// check
		checkPermission(new SecurityCheckableObject(node, "READ"));

		// proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);

		return "/" + node.getProject().getName() + "/" + formatPath(names);

	}

	private String formatPath(List<String> names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/").append(name);
		}
		return builder.toString();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	public List<CampaignLibrary> findLinkableCampaignLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();

		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : campaignLibraryDao
				.findAll();
	}

	@Override
	public List<SuppressionPreviewReport> simulateIterationDeletion(List<Long> targetIds) {
		return deletionHandler.simulateIterationDeletion(targetIds);
	}

	@Override
	public OperationReport deleteIterations(List<Long> targetIds) {
		return deletionHandler.deleteIterations(targetIds);
	}

	@Override
	public List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds) {
		return deletionHandler.simulateSuiteDeletion(targetIds);
	}

	@Override
	public OperationReport deleteSuites(List<Long> targetIds) {
		return deletionHandler.deleteSuites(targetIds);
	}


	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' ,'EXPORT') or hasRole('ROLE_ADMIN')")
	public CampaignExportCSVModel exportCampaignToCSV(Long campaignId, String exportType) {

		Campaign campaign = campaignDao.findById(campaignId);

		WritableCampaignCSVModel model;

		if ("L".equals(exportType)){
			model = simpleCampaignExportCSVModelProvider.get();
		}
		else if ("F".equals(exportType)){
			model = fullCampaignExportCSVModelProvider.get();
		}
		else{
			model = standardCampaignExportCSVModelProvider.get();
		}

		model.setCampaign(campaign);
		model.init();

		return model;
	}

	@Override
	//Only functions for campaigns and campaign folders
	//TODO make it work for iteration and test suites
	public List<String> getParentNodesAsStringList(Long nodeId) {
		List<Long> ids = campaignLibraryNodeDao.getParentsIds(nodeId);

		CampaignLibraryNode node = campaignLibraryNodeDao.findById(nodeId);

		Long librabryId = node.getLibrary().getId();

		List<String> parents = new ArrayList<String>();

		parents.add("#CampaignLibrary-"+librabryId);

		if(ids.size() > 1){
			for(int i=0; i<ids.size()-1; i++){
				long currentId = ids.get(i);
				CampaignLibraryNode currentNode = campaignLibraryNodeDao.findById(currentId);
				parents.add(currentNode.getClass().getSimpleName()+"-"+String.valueOf(currentId));
			}
		}

		return parents;
	}


}
