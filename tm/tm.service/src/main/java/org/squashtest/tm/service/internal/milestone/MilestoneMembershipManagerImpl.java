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
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;

@Service
public class MilestoneMembershipManagerImpl implements MilestoneMembershipManager {

	private static final String WRITE_TC_OR_ROLE_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private MilestoneDao milestoneDao;

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void bindTestCaseToMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		Collection<Milestone> milestones = milestoneDao.findAllByIds(milestoneIds);

		for (Milestone m : milestones) {
			tc.bindMilestone(m);
		}
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void unbindTestCaseFromMilestones(long testCaseId, Collection<Long> milestoneIds) {
		TestCase tc = testCaseDao.findById(testCaseId);
		for (Long milestoneId : milestoneIds){
			tc.unbindMilestone(milestoneId);
		}
	}

	@Override
	public void bindRequirementVersionToMilestones(long requirementVersionId, Collection<Long> milestoneIds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindCampaignToMilestones(long campaignId, Collection<Long> milestoneIds) {
		// TODO Auto-generated method stub

	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestonesForTestCase(long testCaseId) {
		return milestoneDao.findAllMilestonesForTestCase(testCaseId);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestonesToTestCase(long testCaseId) {
		return milestoneDao.findAssociableMilestonesForTestCase(testCaseId);
	}
}
