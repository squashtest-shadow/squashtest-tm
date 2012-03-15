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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.domain.IdentifiersOrderComparator;
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
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.CampaignTestPlanManagerService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;

@Service("squashtest.tm.service.CampaignTestPlanManagerService")
@Transactional
public class CampaignTestPlanManagerServiceImpl implements CampaignTestPlanManagerService {
	/**
	 * Permission string for reading returned object.
	 */
	private static final String CAN_READ_RETURNED_OBJECT = "hasPermission(returnObject, 'READ') or hasRole('ROLE_ADMIN')";

	/**
	 * Permission string for writing campaigns based on campaignId param.
	 */
	private static final String CAN_WRITE_CAMPAIGN_BY_ID = "hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'WRITE') or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

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
	@Transactional(readOnly = true)
	@PostAuthorize(CAN_READ_RETURNED_OBJECT)
	public Campaign findCampaign(long campaignId) {
		return campaignDao.findById(campaignId);
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void addTestCasesToCampaignTestPlan(final List<Long> testCasesIds, long campaignId) {
		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIdList(testCasesIds);

		// now we resort them according to the order in which the testcaseids were given
		IdentifiersOrderComparator comparator = new IdentifiersOrderComparator(testCasesIds);
		Collections.sort(nodes, comparator);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		Campaign campaign = campaignDao.findById(campaignId);

		for (TestCase testCase : testCases) {
			CampaignTestPlanItem itp = new CampaignTestPlanItem(testCase);
			campaignTestPlanItemDao.persist(itp);
			campaign.addToTestPlan(itp);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findAssignableUserForTestPlan(long campaignId) {

		Campaign campaign = campaignDao.findById(campaignId);

		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();
		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(campaign);
		entityRefs.add(oid);

		List<String> loginList = aclService.findUsersWithWritePermission(entityRefs);
		List<User> usersList = userDao.findUsersByLoginList(loginList);
		return usersList;
	}

	/**
	 * @see CampaignTestPlanManagerService#assignUserToTestPlanItem(Long, long, Long)
	 * @param campaignId
	 *            not necessary but actually used for security check
	 */
	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void assignUserToTestPlanItem(long itemId, long campaignId, long userId) {
		User assignee = null;
		if (userId != 0) {
			assignee = userDao.findById(userId);
		}

		CampaignTestPlanItem item = campaignTestPlanItemDao.findById(itemId);
		item.setUser(assignee);
	}

	/**
	 * @see CampaignTestPlanManagerService#assignUserToTestPlanItem(Long, long, Long)
	 * @param campaignId
	 *            not necessary but actually used for security check
	 */
	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void assignUserToTestPlanItems(@NotNull List<Long> itemsIds, long campaignId, long userId) {
		User assignee = null;
		if (userId != 0) {
			assignee = userDao.findById(userId);
		}

		List<CampaignTestPlanItem> items = campaignTestPlanItemDao.findAllByIdList(itemsIds);

		for (CampaignTestPlanItem item : items) {
			item.setUser(assignee);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.CampaignTestPlanManagerService#moveTestPlanItems(long, int, java.util.List)
	 */
	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void moveTestPlanItems(long campaignId, int targetIndex, List<Long> itemIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.moveTestPlanItems(targetIndex, itemIds);
	}

	/**
	 * @see org.squashtest.csp.tm.service.CampaignTestPlanManagerService#removeTestPlanItem(long, long)
	 */
	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void removeTestPlanItem(long campaignId, long itemId) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.removeTestPlanItem(itemId);
	}

	/**
	 * @see org.squashtest.csp.tm.service.CampaignTestPlanManagerService#removeTestPlanItems(long, java.util.List)
	 */
	@Override
	@PreAuthorize(CAN_WRITE_CAMPAIGN_BY_ID)
	public void removeTestPlanItems(long campaignId, List<Long> itemIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		campaign.removeTestPlanItems(itemIds);
	}

	/**
	 * @see org.squashtest.csp.tm.service.CampaignTestPlanManagerService#findById(long)
	 */
	@Override
	@Transactional(readOnly = true)
	@PostAuthorize(CAN_READ_RETURNED_OBJECT)
	public CampaignTestPlanItem findById(long itemId) {
		return campaignTestPlanItemDao.findById(itemId);
	}

}
