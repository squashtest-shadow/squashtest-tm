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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;

import javax.inject.Inject;
import java.util.*;

@Service("squashtest.tm.service.LinkedRequirementVersionManagerService")
@Transactional
public class LinkedRequirementVersionManagerServiceImpl implements LinkedRequirementVersionManagerService {

	@Inject
	private RequirementVersionLinkDao reqVersionLinkDao;

	@Override
	public PagedCollectionHolder<List<LinkedRequirementVersion>>
		findAllByRequirementVersion(long requirementVersionId, PagingAndSorting pagingAndSorting) {

		List<RequirementVersionLink> requirementVersionLinksList =
			reqVersionLinkDao.findAllByReqVersionId(requirementVersionId);

		List<LinkedRequirementVersion> linkedReqVersionsList =
			new ArrayList<LinkedRequirementVersion>();

		for(RequirementVersionLink reqVerLink : requirementVersionLinksList) {
			if(requirementVersionId == reqVerLink.getRequirementVersion1().getId()) {
			/* The current RequirementVersion is the requirementVersion1 of the Link,
				so we want to display the requirementVersion2 */
				linkedReqVersionsList.add(
					new LinkedRequirementVersion(
						reqVerLink.getRequirementVersion2(),
						reqVerLink.getLinkType().getRole2()));
			} else {
			// It is the requirementVersion2 of the Link.
				linkedReqVersionsList.add(
					new LinkedRequirementVersion(
						reqVerLink.getRequirementVersion1(),
						reqVerLink.getLinkType().getRole1()));
			}
		}

		return new PagingBackedPagedCollectionHolder<>(pagingAndSorting, requirementVersionLinksList.size(), linkedReqVersionsList);
	}

}
