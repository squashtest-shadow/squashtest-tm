/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.bugtracker;

import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.bugtracker.RequirementVersionIssueOwnership;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

import javax.inject.Inject;
import java.util.*;

@Component
class RequirementVersionIssueFinder extends TestCaseIssueFinder {
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private IssueDao issueDao;
	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;

	static final String INFO = "info";
	static final String ALPHABETICAL_ORDER = "alphabetical-order";
	static final String CUSTOM_ORDER = "custom-order";

	public PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> findSorted(long entityId, String displayMode, PagingAndSorting sorter) {
		RequirementVersion currentRequirementVersion = requirementVersionManagerService.findById(entityId);

		List<RequirementVersion> allRequirementVersions = new ArrayList<>();
		List<RequirementVersionIssueOwnership<RemoteIssueDecorator>> requirementVersionIssueOwnerships = new ArrayList<>();
		long nbIssues = 0L;
		int order = 0;

		if (displayMode.equals(INFO)) {
			allRequirementVersions.add(currentRequirementVersion);
		} else {
			Requirement currentRequirement = currentRequirementVersion.getRequirement();
			Map<Long, Requirement> targetRequirementMaps = getAllRequirements(currentRequirement);
			if (displayMode.equals(CUSTOM_ORDER)) {
				List<Long> unsortedRequirementIds = new ArrayList(targetRequirementMaps.keySet());
				List<Long> sortedRequirementIds = requirementDao.sortRequirementByNodeRelationship(unsortedRequirementIds);
				sortedRequirementIds.forEach(id -> {
					allRequirementVersions.add(targetRequirementMaps.get(id).getCurrentVersion());
				});
			}
			if (displayMode.equals(ALPHABETICAL_ORDER)) {
				targetRequirementMaps.forEach((id, req) -> {
					allRequirementVersions.add(req.getCurrentVersion());
				});
				allRequirementVersions.sort(Comparator.comparing(RequirementVersion::getFullName));
			}
		}
		for (RequirementVersion rv : allRequirementVersions) {
			List<Pair<Execution, Issue>> pairs = findExecutionIssuePairs(rv, sorter);
			List<IssueOwnership<RemoteIssueDecorator>> issueOwnerships = findRemoteIssues(pairs);
			for (IssueOwnership<RemoteIssueDecorator> io : issueOwnerships) {
				requirementVersionIssueOwnerships.add(new RequirementVersionIssueOwnership<>(io.getIssue(), io.getOwner(), rv, String.valueOf(order)));
			}
			nbIssues += countIssues(rv);
			order += 1;
		}
		return new PagingBackedPagedCollectionHolder<>(sorter, nbIssues, requirementVersionIssueOwnerships);
	}

	private Map<Long, Requirement> getAllRequirements(Requirement currentRequirement) {
		Map<Long, Requirement> allRequirements = new HashMap<>();
		allRequirements.put(currentRequirement.getId(), currentRequirement);
		if (currentRequirement.hasContent()) {
			for (Requirement childrenRequirement : currentRequirement.getContent()) {
				allRequirements.putAll(getAllRequirements(childrenRequirement));
			}
		}
		return allRequirements;
	}

	private List<Pair<Execution, Issue>> findExecutionIssuePairs(RequirementVersion requirementVersion, PagingAndSorting sorter) {
		return issueDao.findAllExecutionIssuePairsByRequirementVersion(requirementVersion, sorter);
	}

	private long countIssues(RequirementVersion requirementVersion) {
		return issueDao.countByRequirementVersion(requirementVersion);
	}

}
