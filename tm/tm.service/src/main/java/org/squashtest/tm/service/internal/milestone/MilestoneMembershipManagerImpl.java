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
package org.squashtest.tm.service.internal.milestone;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;

@Service("squashtest.tm.service.MilestoneMembershipManager")
public class MilestoneMembershipManagerImpl implements MilestoneMembershipManager {

	private static final String READ_TC = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')";
	private static final String WRITE_TC = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE')";

	private static final String READ_REQVERSION = "hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'READ')";
	private static final String WRITE_REQVERSION = "hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'WRITE')";

	private static final String READ_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'READ')";
	private static final String WRITE_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'WRITE')";

	private static final String READ_ITERATION = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration' , 'READ')";

	private static final String READ_TESTSUITE = "hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite' , 'READ')";

	private static final String ROLE_ADMIN = " or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private IterationDao iterDao;

	@Inject
	private TestSuiteDao tsDao;

	@Inject
	private MilestoneDao milestoneDao;

	@Override
	@PreAuthorize(WRITE_TC + ROLE_ADMIN)
	public void bindTestCaseToMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		Collection<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);

		for (Milestone m : milestones) {
			tc.bindMilestone(m);
		}
	}

	@Override
	@PreAuthorize(WRITE_TC + ROLE_ADMIN)
	public void unbindTestCaseFromMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		for (Long milestoneId : milestoneIds) {
			tc.unbindMilestone(milestoneId);
		}
	}

	@Override
	@PreAuthorize(WRITE_REQVERSION + ROLE_ADMIN)
	public void bindRequirementVersionToMilestones(long versionId, Collection<Long> milestoneIds) {
		RequirementVersion version = requirementVersionDao.findById(versionId);
		Collection<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);

		for (Milestone m : milestones) {
			if (!m.isOneVersionAlreadyBound(version)) {
				version.bindMilestone(m);
			}
		}

	}

	@Override
	@PreAuthorize(WRITE_REQVERSION + ROLE_ADMIN)
	public void unbindRequirementVersionFromMilestones(long versionId, Collection<Long> milestoneIds) {
		RequirementVersion version = requirementVersionDao.findById(versionId);
		for (Long milestoneId : milestoneIds) {
			version.unbindMilestone(milestoneId);
		}
	}

	@Override
	@PreAuthorize(WRITE_CAMPAIGN + ROLE_ADMIN)
	public void bindCampaignToMilestones(long campaignId, Collection<Long> milestoneIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		Collection<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);

		for (Milestone m : milestones) {
			campaign.bindMilestone(m);
		}
	}

	@Override
	@PreAuthorize(WRITE_CAMPAIGN + ROLE_ADMIN)
	public void unbindCampaignFromMilestones(long campaignId, Collection<Long> milestoneIds) {
		Campaign campaign = campaignDao.findById(campaignId);
		for (Long milestoneId : milestoneIds) {
			campaign.unbindMilestone(milestoneId);
		}
	}

	@Override
	@PreAuthorize(READ_TC + ROLE_ADMIN)
	public Collection<Milestone> findAllMilestonesForTestCase(long testCaseId) {
		return milestoneDao.findAllMilestonesForTestCase(testCaseId);
	}

	@Override
	@PreAuthorize(READ_TC + ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToTestCase(long testCaseId) {
		return milestoneDao.findAssociableMilestonesForTestCase(testCaseId);
	}

	@Override
	public Collection<Milestone> findAllMilestonesForUser(long userId) {
		return milestoneDao.findAssociableMilestonesForUser(userId);
	}

	@PreAuthorize(READ_REQVERSION + ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToRequirementVersion(long versionId) {
		return milestoneDao.findAssociableMilestonesForRequirementVersion(versionId);
	}

	@Override
	@PreAuthorize(READ_REQVERSION + ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForRequirementVersion(long versionId) {
		return milestoneDao.findMilestonesForRequirementVersion(versionId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN + ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToCampaign(long campaignId) {
		return milestoneDao.findAssociableMilestonesForCampaign(campaignId);
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN + ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForCampaign(long campaignId) {
		return milestoneDao.findMilestonesForCampaign(campaignId);
	}

	@Override
	@PreAuthorize(READ_ITERATION + ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForIteration(long iterationId) {
		return milestoneDao.findMilestonesForIteration(iterationId);
	}

	@Override
	@PreAuthorize(READ_TESTSUITE + ROLE_ADMIN)
	public Collection<Milestone> findMilestonesForTestSuite(long testSuiteId) {
		return milestoneDao.findMilestonesForTestSuite(testSuiteId);
	}

}
