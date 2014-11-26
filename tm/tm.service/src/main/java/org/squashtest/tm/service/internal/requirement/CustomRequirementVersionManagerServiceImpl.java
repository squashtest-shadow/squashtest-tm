/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomRequirementVersionManagerService")
@Transactional
public class CustomRequirementVersionManagerServiceImpl implements CustomRequirementVersionManagerService {

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private InfoListItemFinderService infoListItemService;

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#changeCriticality(long,
	 *      org.squashtest.tm.domain.requirement.RequirementCriticality)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeCriticality(long requirementVersionId, RequirementCriticality criticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);
		RequirementCriticality oldCriticality = requirementVersion.getCriticality();
		requirementVersion.setCriticality(criticality);
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId, oldCriticality);
	}

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#findAllByRequirement(long,
	 *      org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<RequirementVersion>> findAllByRequirement(long requirementId, PagingAndSorting pas) {
		List<RequirementVersion> versions = requirementVersionDao.findAllByRequirement(requirementId, pas);
		long versionsCount = requirementVersionDao.countByRequirement(requirementId);

		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(pas, versionsCount, versions);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	public List<RequirementVersion> findAllByRequirement(long requirementId) {
		DefaultPagingAndSorting pas = new DefaultPagingAndSorting("versionNumber", true);
		pas.setSortOrder(SortOrder.DESCENDING);
		return findAllByRequirement(requirementId, pas).getPagedItems();
	}

	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeCategory(long requirementVersionId, String categoryCode){
		RequirementVersion version = requirementVersionDao.findById(requirementVersionId);
		InfoListItem category = infoListItemService.findByCode(categoryCode);

		version.setCategory(category);
	}

}
