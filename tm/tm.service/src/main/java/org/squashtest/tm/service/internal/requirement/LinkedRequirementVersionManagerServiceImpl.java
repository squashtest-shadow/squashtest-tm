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
package org.squashtest.tm.service.internal.requirement;

import com.google.common.base.Optional;
import org.mockito.internal.matchers.Same;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.*;
import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;

import javax.inject.Inject;
import java.util.*;

@Service("squashtest.tm.service.LinkedRequirementVersionManagerService")
@Transactional
public class LinkedRequirementVersionManagerServiceImpl implements LinkedRequirementVersionManagerService {

	@Inject
	private RequirementVersionDao reqVersionDao;
	@Inject
	private RequirementVersionLinkDao reqVersionLinkDao;
	@Inject
	private RequirementVersionLinkTypeDao reqVersionLinkTypeDao;
	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;
	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@Override
	public PagedCollectionHolder<List<LinkedRequirementVersion>>
		findAllByRequirementVersion(long requirementVersionId, PagingAndSorting pagingAndSorting) {

		List<RequirementVersionLink> requirementVersionLinksList =
			reqVersionLinkDao.findAllByReqVersionId(requirementVersionId, pagingAndSorting);

		List<LinkedRequirementVersion> linkedReqVersionsList =
			new ArrayList<LinkedRequirementVersion>();

		for(RequirementVersionLink reqVerLink : requirementVersionLinksList) {
				linkedReqVersionsList.add(
					reqVerLink.getRelatedLinkedRequirementVerison());
		}

		return new PagingBackedPagedCollectionHolder<>(pagingAndSorting, requirementVersionLinksList.size(), linkedReqVersionsList);
	}

	@Override
	public void removeLinkedRequirementVersionsFromRequirementVersion(
		long requirementVersionId, List<Long> requirementVersionIdsToUnlink) {

		reqVersionLinkDao.deleteAllLinks(requirementVersionId, requirementVersionIdsToUnlink);
	}

	@Override
	public Collection<LinkedRequirementVersionException> addLinkedReqVersionsToReqVersion(
		Long mainReqVersionId, List<Long> otherReqVersionsIds) {

		List<RequirementVersion> requirementVersions = findRequirementVersions(otherReqVersionsIds);
		List<LinkedRequirementVersionException> rejections = new ArrayList<>();

		RequirementVersion mainReqVersion = reqVersionDao.findOne(mainReqVersionId);
		for(RequirementVersion otherRequirementVersion : requirementVersions) {

			try {
				checkIfLinkAlreadyExists(mainReqVersion, otherRequirementVersion);
				checkIfSameRequirement(mainReqVersion, otherRequirementVersion);
				checkIfVersionsAreLinkable(mainReqVersion, otherRequirementVersion);

				/* Aucune exception -> Ajout */
				RequirementVersionLink newReqVerLink =
					new RequirementVersionLink(
						mainReqVersion,
						otherRequirementVersion,
						reqVersionLinkTypeDao.getDefaultRequirementVersionLinkType(),
						false);
				reqVersionLinkDao.addLink(newReqVerLink);
			} catch(LinkedRequirementVersionException exception) {

				rejections.add(exception);
			}
		}
		return rejections;
	}

	private void checkIfLinkAlreadyExists(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws AlreadyLinkedRequirementVersionException {
		if (reqVersionLinkDao.linkAlreadyExists(reqVersion.getId(), relatedReqVersion.getId())) {
			throw new AlreadyLinkedRequirementVersionException(reqVersion, relatedReqVersion);
		}
	};

	private void checkIfSameRequirement(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws SameRequirementLinkedRequirementVersionException {
			if (reqVersion.getRequirement().getId() == relatedReqVersion.getRequirement().getId()) {
				throw new SameRequirementLinkedRequirementVersionException(reqVersion, relatedReqVersion);
			}
		};

	private void checkIfVersionsAreLinkable(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws UnlinkableLinkedRequirementVersionException {
		if (!reqVersion.isLinkable() || !relatedReqVersion.isLinkable()) {
			throw new UnlinkableLinkedRequirementVersionException(reqVersion, relatedReqVersion);
		}
	};

	private List<RequirementVersion> findRequirementVersions(
		List<Long> requirementNodesIds) {

		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao
			.findAllByIds(requirementNodesIds);

		if (!nodes.isEmpty()) {
			List<Requirement> requirements = new RequirementNodeWalker()
				.walk(nodes);
			if (!requirements.isEmpty()) {
				return extractVersions(requirements);
			}
		}
		return Collections.emptyList();
	}

	private List<RequirementVersion> extractVersions(List<Requirement> requirements) {

		List<RequirementVersion> rvs = new ArrayList<>(requirements.size());

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		for (Requirement requirement : requirements) {

			// normal mode
			if (!activeMilestone.isPresent()) {
				rvs.add(requirement.getResource());
			}
			// milestone mode
			else {
				rvs.add(requirement.findByMilestone(activeMilestone.get()));
			}
		}
		return rvs;
	}
}
