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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.CampaignFolderDao;
import org.squashtest.csp.tm.internal.repository.CampaignLibraryDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Service("squashtest.tm.service.CampaignLibraryNavigationService")
@Transactional
public class CampaignLibraryNavigationServiceImpl extends
		AbstractLibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> implements
		CampaignLibraryNavigationService {
	
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";
	
	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private IterationTestPlanManager iterationTestPlanManager;

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
	@Qualifier("squashtest.tm.service.CampaignLibrarySelectionStrategy")
	private LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> libraryStrategy;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Override
	protected NodeDeletionHandler<CampaignLibraryNode, CampaignFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	@PreAuthorize("(hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'CREATE')"
			+ "and hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ'))"
			+ OR_HAS_ROLE_ADMIN)
	public int copyIterationToCampaign(long campaignId, long iterationId) {
		Iteration originalIteration = iterationDao.findById(iterationId);
		Campaign campaign = campaignDao.findById(campaignId);
		copyIterationToCampaign(campaign, originalIteration);
		return 0;
	}

	private void copyIterationToCampaign(Campaign campaign, Iteration iterationSource) {
		Iteration iterationCopy = createCopyIteration(iterationSource);
		if (!campaign.isContentNameAvailable(iterationCopy.getName())) {
			renameIterationCopy(iterationCopy, campaign);
		}
		campaign.addIteration(iterationCopy);
	}

	private void renameIterationCopy(Iteration newIteration, Campaign campaign) {
		List<String> copiesNames = campaignDao
				.findNamesInCampaignStartingWith(campaign.getId(), newIteration.getName());
		int newCopy = generateUniqueCopyNumber(copiesNames, newIteration.getName());
		String newName = newIteration.getName() + COPY_TOKEN + newCopy;
		newIteration.setName(newName);
	}

	@Override
	@PreAuthorize("(hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'CREATE'))"
			+ " or hasRole('ROLE_ADMIN')")
	public List<Iteration> copyIterationsToCampaign(long campaignId, Long[] iterationsIds) {
		// create persisted copies
		List<Iteration> newIterations = createCopiesOfIterations(campaignId, iterationsIds);
		// link to campaign
		linkIterationsToCampaign(campaignId, newIterations);

		return newIterations;
	}

	private void linkIterationsToCampaign(long campaignId, List<Iteration> newIterations) {
		Campaign campaign = campaignDao.findById(campaignId);
		for (Iteration iteration : newIterations) {
			campaign.addIteration(iteration);
		}
	}

	private List<Iteration> createCopiesOfIterations(long campaignId, Long[] iterationsIds) {
		List<Iteration> newIterations = new ArrayList<Iteration>();
		List<String> namesAtDestination = campaignDao.findAllNamesInCampaign(campaignId);
		for (Long iterationId : iterationsIds) {
			Iteration newIteration = createCopyIteration(iterationId);
			renameIfHomonymeInDestination(newIteration, namesAtDestination);
			namesAtDestination.add(newIteration.getName());
			newIterations.add(newIteration);
		}
		return newIterations;
	}

	private void renameIfHomonymeInDestination(Iteration newIteration, List<String> namesAtDestination) {
		if (namesAtDestination.contains(newIteration.getName())) {
			List<String> copiesNames = findNamesStartingWith(namesAtDestination, newIteration.getName());
			int newCopyNumber = generateUniqueCopyNumber(copiesNames, newIteration.getName());
			String newName = newIteration.getName() + COPY_TOKEN + newCopyNumber;
			newIteration.setName(newName);
		}
	}

	private List<String> findNamesStartingWith(List<String> namesAtDestination, String string) {
		List<String> copiedNames = new ArrayList<String>();
		for (String name : namesAtDestination) {
			if (name.startsWith(string)) {
				copiedNames.add(name);
			}
		}
		return copiedNames;
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') "+OR_HAS_ROLE_ADMIN)
	public Campaign findCampaign(long reqId) {
		return campaignDao.findById(reqId);
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'CREATE') "+OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, long campaignId) {
		Campaign campaign = campaignDao.findById(campaignId);

		if (!campaign.isContentNameAvailable(iteration.getName())) {
			throw new DuplicateNameException(iteration.getName(), iteration.getName());
		}
		return iterationModificationService.addIterationToCampaign(iteration, campaignId);
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
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' , 'READ') "+OR_HAS_ROLE_ADMIN)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return iterationModificationService.findIterationsByCampaignId(campaignId);
	}

	/**
	 * this is done here because content of campaigns is to be persisted first
	 */
	@Override
	protected CampaignLibraryNode createPastableCopy(CampaignLibraryNode original) {
		if (original instanceof Campaign) {
			Campaign clone = (Campaign) original.createPastableCopy();
			campaignDao.persist(clone);
			for (Iteration iterationSource : ((Campaign) original).getIterations()) {
				copyIterationToCampaign(clone, iterationSource);
			}
			return clone;
		} else {
			CampaignFolder sourceFolder = (CampaignFolder) original;
			CampaignFolder newFolder = sourceFolder.createPastableCopy();
			for (CampaignLibraryNode node : sourceFolder.getContent()) {
				CampaignLibraryNode newNode = createPastableCopy(node);
				newFolder.addContent(newNode);
			}
			return newFolder;
		}

	}

	/*
	 * //TODO : investigate why is public and find why createCopyIteration is not exposed in the interface too.
	 * 
	 * mpagnon says : my guess is that the method is public so that we can check authorizations through @PreAuthorize
	 * for each iteration id because this couldn't be done with a list of ids in copyIterationsToCampaign. I suppose we
	 * need to remove the public attribute and add a check step to the method copyIterationToCampaign
	 */
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "+OR_HAS_ROLE_ADMIN)
	public Iteration createCopyIteration(long iterationId) {
		Iteration originalIteration = iterationModificationService.findById(iterationId);
		return createCopyIteration(originalIteration);
	}

	private Iteration createCopyIteration(Iteration originalIteration) {
		Iteration iterationCopy = originalIteration.createCopy();
		iterationDao.persistIterationAndTestPlan(iterationCopy);
		copyIterationTestSuites(originalIteration, iterationCopy);
		return iterationCopy;
	}

	private void copyIterationTestSuites(Iteration originalIteration, Iteration iterationCopy) {
		Map<TestSuite, List<Integer>> testSuitesPastableCopiesMap = originalIteration.createTestSuitesPastableCopy();
		for (Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry : testSuitesPastableCopiesMap.entrySet()) {
			TestSuite testSuiteCopy = testSuitePastableCopyEntry.getKey();
			iterationTestPlanManager.addTestSuite(iterationCopy, testSuiteCopy);
			bindTestPlanOfCopiedTestSuite(iterationCopy, testSuitePastableCopyEntry, testSuiteCopy);
		}
	}

	private void bindTestPlanOfCopiedTestSuite(Iteration iterationCopy,
			Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry, TestSuite testSuiteCopy) {
		List<Integer> testSuiteTpiIndexesInIterationList = testSuitePastableCopyEntry.getValue();
		List<IterationTestPlanItem> testPlanItemsToBind = new ArrayList<IterationTestPlanItem>();
		List<IterationTestPlanItem> iterationTestPlanCopy = iterationCopy.getTestPlans();
		for (Integer testSuiteTpiIndexInIterationList : testSuiteTpiIndexesInIterationList) {
			IterationTestPlanItem testPlanItemToBind = iterationTestPlanCopy.get(testSuiteTpiIndexInIterationList);
			testPlanItemsToBind.add(testPlanItemToBind);
		}
		testSuiteCopy.bindTestPlanItems(testPlanItemsToBind);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.csp.tm.domain.campaign.CampaignLibrary', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignLibrary(long libraryId, Campaign newCampaign) {
		CampaignLibrary library = campaignLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			library.addRootContent(newCampaign);
			campaignDao.persist(newCampaign);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.csp.tm.domain.campaign.CampaignFolder', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void addCampaignToCampaignFolder(long folderId, Campaign newCampaign) {
		CampaignFolder folder = campaignFolderDao.findById(folderId);
		if (!folder.isContentNameAvailable(newCampaign.getName())) {
			throw new DuplicateNameException(newCampaign.getName(), newCampaign.getName());
		} else {
			folder.addContent(newCampaign);
			campaignDao.persist(newCampaign);
		}

	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') or hasRole('ROLE_ADMIN')")
	public Iteration findIteration(long iterationId) {
		return iterationDao.findById(iterationId);

	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestSuite> findIterationContent(long iterationId) {
		return suiteDao.findAllByIterationId(iterationId);
	}
	
	@Override
	public String getPathAsString(long entityId) {
		//get
		CampaignLibraryNode node = getLibraryNodeDao().findById(entityId);
		
		//check
		checkPermission(new SecurityCheckableObject(node, "READ"));
		
		//proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);
		
		return "/"+node.getProject().getName()+"/"+formatPath(names);
		
	}
	
	private String formatPath(List<String> names){
		StringBuilder builder = new StringBuilder();
		for (String name : names){
			builder.append("/").append(name);
		}
		return builder.toString();		
	}

	/*
	 * //TODO : investigate why that method must always return 0, or why should it return something anyway.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.CampaignLibraryNavigationService#moveIterationToNewCampaign(long, long, long)
	 */
	@Override
	@PreAuthorize("(hasPermission(#newCampaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' , 'CREATE') "
			+ "and hasPermission(#oldCampaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign' , 'DELETE') "
			+ "and hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration' , 'READ' ) ) "
			+ OR_HAS_ROLE_ADMIN)
	public int moveIterationToNewCampaign(long newCampaignId, long oldCampaignId, long iterationId) {

		Campaign destination = campaignDao.findById(newCampaignId);
		Campaign source = campaignDao.findById(oldCampaignId);
		Iteration iteration = iterationDao.findById(iterationId);

		source.removeIteration(iteration);
		campaignDao.flush();
		destination.addIteration(iteration);

		return 0;
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') "+OR_HAS_ROLE_ADMIN)
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
	public List<Long> deleteIterations(List<Long> targetIds) {
		return deletionHandler.deleteIterations(targetIds);
	}

	@Override
	public List<SuppressionPreviewReport> simulateSuiteDeletion(List<Long> targetIds) {
		return deletionHandler.simulateSuiteDeletion(targetIds);
	}

	@Override
	public List<Long> deleteSuites(List<Long> targetIds) {
		return deletionHandler.deleteSuites(targetIds);
	}

}
