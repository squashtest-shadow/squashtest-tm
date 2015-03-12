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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;

public interface MilestoneDao extends EntityDao<Milestone> {
	public interface HolderConsumer {
		void consume(MilestoneHolder holder);
	}

	long countMilestones();

	void checkLabelAvailability(String label);

	Collection<Milestone> findAssociableMilestonesForTestCase(long testCaseId);

	Collection<Milestone> findAllMilestonesForTestCase(long testCaseId);

	Collection<Milestone> findAssociableMilestonesForUser(long UserId);

	void bindMilestoneToProjectTestCases(long projectId, long milestoneId);

	void bindMilestoneToProjectRequirementVersions(long projectId, long milestoneId);

	void bindMilestoneToProjectCampaigns(long projectId, long milestoneId);

	Collection<Milestone> findAssociableMilestonesForRequirementVersion(long versionId);

	Collection<Milestone> findMilestonesForRequirementVersion(long versionId);

	Collection<Milestone> findAssociableMilestonesForCampaign(long campaignId);

	Collection<Milestone> findMilestonesForCampaign(long campaignId);

	Collection<Milestone> findMilestonesForIteration(long iterationId);

	Collection<Milestone> findMilestonesForTestSuite(long suiteId);

	void synchronizeCampaigns(long source, long target, List<Long> projectIds);

	void synchronizeRequirementVersions(long source, long target, List<Long> projectIds);

	void synchronizeTestCases(long source, long target, List<Long> projectIds);

	void  performBatchUpdate(HolderConsumer consumer);
}
