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
package org.squashtest.tm.service.requirement;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;

import java.util.Collection;
import java.util.List;

/**
 * Service for management of Requirement Versions linked to other Requirement Versions.
 *
 * @author jlor
 *
 */
public interface LinkedRequirementVersionManagerService {

	@Transactional(readOnly = true)
	PagedCollectionHolder<List<LinkedRequirementVersion>> findAllByRequirementVersion(
		long requirementVersionId, PagingAndSorting pagingAndSorting);

	void removeLinkedRequirementVersionsFromRequirementVersion(
		long requirementVersionId, List<Long> requirementVersionIdsToUnlink);

	Collection<LinkedRequirementVersionException> addLinkedReqVersionsToReqVersion(
		Long singleReqVersionId, List<Long> otherReqVersionsIds);

	Collection<LinkedRequirementVersionException> addDefaultLinkWithNodeIds(
		Long reqVersionNodeId, Long relatedReqVersionNodeId);

	List<RequirementVersionLinkType> getAllReqVersionLinkTypes();

	void updateLinkTypeAndDirection(
		long requirementVersionId, long relatedRequirementNodeId,
		long reqVersionLinkTypeId, boolean reqVersionLinkTypeDirection);
}
