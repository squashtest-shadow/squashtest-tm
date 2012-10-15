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
package org.squashtest.csp.tm.internal.service.requirement;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.service.CustomRequirementVersionManagerService;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomRequirementVersionManagerService")
public class CustomRequirementVersionManagerServiceImpl implements CustomRequirementVersionManagerService {
	@Inject
	private RequirementVersionDao requirementVersionDao;

	/**
	 * @see org.squashtest.csp.tm.service.CustomRequirementVersionManagerService#changeCriticality(long,
	 *      org.squashtest.csp.tm.domain.requirement.RequirementCriticality)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.csp.tm.domain.requirement.RequirementVersion', 'SMALL_EDIT') or hasRole('ROLE_ADMIN')")
	public void changeCriticality(long requirementVersionId, RequirementCriticality criticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);
		// FIXME should send event to test cases
		requirementVersion.setCriticality(criticality);

	}

	/**
	 * @see org.squashtest.csp.tm.service.CustomRequirementVersionManagerService#findAllByRequirement(long,
	 *      org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<RequirementVersion>> findAllByRequirement(long requirementId, PagingAndSorting pas) {
		List<RequirementVersion> versions = requirementVersionDao.findAllByRequirement(requirementId, pas);
		long versionsCount = requirementVersionDao.countByRequirement(requirementId);

		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(pas, versionsCount, versions);
	}
}
