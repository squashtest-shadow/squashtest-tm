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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.security.acls.model.ObjectAclService;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.CampaignTestPlanItemDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.CampaignTestPlanManagerService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;

@Service("squashtest.tm.service.CampaignTestPlanManagerService")
@Transactional
public class CampaignTestPlanManagerServiceImpl implements CampaignTestPlanManagerService {

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	private TestCaseFolderDao folderDao;
	
	@Inject
	private ObjectAclService aclService;

	@Inject
	private CampaignTestPlanItemDao campaignTestPlanItemDao;
	
	@Inject
	private UserDao userDao;
	
	private ObjectIdentityRetrievalStrategy objIdRetrievalStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;
	
	
	@ServiceReference
	public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {
		this.objIdRetrievalStrategy = objectIdentityRetrievalStrategy;
	}
	
	@Override
	@PostAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'READ') or hasRole('ROLE_ADMIN')")
	public Campaign findCampaign(long campaignId) {
		return campaignDao.findById(campaignId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	@Override
	@PostAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void addTestCasesToCampaignTestPlan(List<Long> testCasesIds, long campaignId) {

		List<TestCase> tcs = new ArrayList<TestCase>();

		// TODO (put three letters acronym here) !! Apparently, there are folders ids in the list which have been turned
		// into negative numbers.. these should have been sorted by controller and passed in separate arrays.
		for (Long tcId : testCasesIds) {
			if (tcId > 0) {
				TestCase testCase = testCaseDao.findById(tcId);
				tcs.add(testCase);
			} else {
				tcs.addAll(addFolderContent(Math.abs(tcId)));
			}
		}

		if (!tcs.isEmpty()) {
			addTestCasesToCampaignTestPlan(campaignId, tcs);
		}

	}

	private void addTestCasesToCampaignTestPlan(long campaignId, List<TestCase> testCases) {
		Campaign camp = campaignDao.findById(campaignId);
		for (TestCase testCase : testCases) {
			addTestCaseToCampaignTestPlan(camp, testCase);
		}
	}

	private void addTestCaseToCampaignTestPlan(Campaign campaign, TestCase testCase) {
		if (!campaign.testPlanContains(testCase)) {
			CampaignTestPlanItem itemTestPlan = new CampaignTestPlanItem(testCase);
			
			campaignTestPlanItemDao.persist(itemTestPlan);
			campaign.addToTestPlan(itemTestPlan);
		}
	}

	private List<TestCase> addFolderContent(long folderId) {
		List<TestCase> resultList = new ArrayList<TestCase>();
		List<Long> folderIds = new ArrayList<Long>();
		folderIds.add(0, 1L);
		int i = 0;

		List<TestCaseLibraryNode> folderContent = folderDao.findAllContentById(folderId);

		do {
			folderIds.remove(i);
			for (TestCaseLibraryNode node : folderContent) {
				if (node.getClassSimpleName() == "TestCaseFolder") {
					folderIds.add(node.getId());
				} else {
					resultList.add((TestCase) node);
				}
			}
			if (!folderIds.isEmpty()) {
				folderContent = folderDao.findAllContentById(folderIds.get(i));
			}
		} while (!folderIds.isEmpty());

		return resultList;
	}

	@Override
	@PostAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeTestCasesFromCampaign(List<Long> testCaseIds, long campaignId) {

		List<TestCase> tcs = testCaseDao.findAllByIdList(testCaseIds);

		if (!tcs.isEmpty()) {
			Campaign camp = campaignDao.findById(campaignId);

			for (TestCase testCase : tcs) {
				removeTestCaseFromCampaignTestPlan(camp, testCase);
			}
		}

	}

	private void removeTestCaseFromCampaignTestPlan(Campaign camp, TestCase testCase) {
		CampaignTestPlanItem itp = camp.findTestPlanItem(testCase);
		camp.removeTestPlanItem(itp);
		campaignTestPlanItemDao.remove(itp);
	}

	@Override
	@PostAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeTestCaseFromCampaign(Long testCaseId, long campaignId) {

		TestCase testCase = testCaseDao.findById(testCaseId);
		Campaign campaign = campaignDao.findById(campaignId);
		removeTestCaseFromCampaignTestPlan(campaign, testCase);
	}
	
	@Override
	public List<User> findAssignableUserForTestPlan(long campaignId) {
		
		Campaign campaign = campaignDao.findById(campaignId);
		
		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();
		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(campaign);
		entityRefs.add(oid);

		List<String> loginList = aclService.findUsersWithWritePermission(entityRefs);
		List<User> usersList = userDao.findUsersByLoginList(loginList);
		return usersList;
	}

	@Override
	public void assignUserToTestPlanItem(Long testCaseId, long campaignId, Long userId) {
		Campaign campaign = campaignDao.findById(campaignId);
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (userId == 0){
			CampaignTestPlanItem itp = campaign.findTestPlanItem(testCase);
			itp.setUser(null);
			return;
		}
		User user = userDao.findById(userId);
		CampaignTestPlanItem itp = campaign.findTestPlanItem(testCase);
		itp.setUser(user);	
	}

	@Override
	public void assignUserToTestPlanItems(List<Long> testCaseIds,
			long campaignId, Long userId) {
		Campaign campaign = campaignDao.findById(campaignId);
		if (userId == 0){
			for (Long testCaseId : testCaseIds) {
				TestCase testCase = testCaseDao.findById(testCaseId);
				CampaignTestPlanItem itp = campaign.findTestPlanItem(testCase);
				itp.setUser(null);
			}
			return;
		}
		User user = userDao.findById(userId);
		for (Long testCaseId : testCaseIds) {
			TestCase testCase = testCaseDao.findById(testCaseId);
			CampaignTestPlanItem itp = campaign.findTestPlanItem(testCase);
			itp.setUser(user);
		}
	}

	@Override
	public CampaignTestPlanItem findTestPlanItemByTestCaseId(long campaignId,
			long testCaseId) {
		Campaign campaign = campaignDao.findById(campaignId);
		TestCase testCase = testCaseDao.findById(testCaseId);
		CampaignTestPlanItem itp = campaign.findTestPlanItem(testCase);
		return itp;
	}

}
