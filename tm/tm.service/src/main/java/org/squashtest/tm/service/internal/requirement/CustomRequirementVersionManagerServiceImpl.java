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
package org.squashtest.tm.service.internal.requirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;
import static org.squashtest.tm.service.security.Authorizations.*;

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

	@Inject
	private MilestoneMembershipManager milestoneManager;

	@Inject
	private IndexationService indexationService;

	@Inject
	private PrivateCustomFieldValueService customFieldValueService;


	@Inject
	private SessionFactory sessionFactory;

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')" + OR_HAS_ROLE_ADMIN)
	public Requirement findRequirementById(long requirementId){
		return requirementVersionDao.findRequirementById(requirementId);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE')" + OR_HAS_ROLE_ADMIN)
	public void createNewVersion(long requirementId) {
		Requirement req = requirementVersionDao.findRequirementById(requirementId);
		RequirementVersion previousVersion = req.getCurrentVersion();

		req.increaseVersion();
		sessionFactory.getCurrentSession().persist(req.getCurrentVersion());
		RequirementVersion newVersion = req.getCurrentVersion();
		indexationService.reindexRequirementVersions(req.getRequirementVersions());
		customFieldValueService.copyCustomFieldValues(previousVersion, newVersion);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE')" + OR_HAS_ROLE_ADMIN)
	public void createNewVersion(long requirementId, Collection<Long> milestoneIds) {

		createNewVersion(requirementId);
		Requirement req = requirementVersionDao.findRequirementById(requirementId);

		for (RequirementVersion version : req.getRequirementVersions()){
			for (Long mid : milestoneIds){
				version.unbindMilestone(mid);
			}
		}

		milestoneManager.bindRequirementVersionToMilestones(req.getCurrentVersion().getId(), milestoneIds);

	}


	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#changeCriticality(long,
	 *      org.squashtest.tm.domain.requirement.RequirementCriticality)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void changeCriticality(long requirementVersionId, RequirementCriticality criticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);
		RequirementCriticality oldCriticality = requirementVersion.getCriticality();
		requirementVersion.setCriticality(criticality);
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId, oldCriticality);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void rename(long requirementVersionId, String newName) {
		RequirementVersion v = requirementVersionDao.findById(requirementVersionId);

		/*
		 *  FIXME : there is a loophole here. What exactly means DuplicateNameException for requirements, that can have multiple names (one for each
		 *  version) ? What happens when the library is displayed in milestone mode and that two versions of different requirements happens to have the
		 *  same name and same milestone (hint : they would be displayed both anyway).
		 * 
		 *    Because of this we are waiting for better specs on that matter, and the implementation here remains trivial in the mean time.
		 */

		v.setName(newName.trim());
	}

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#findAllByRequirement(long,
	 *      org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<RequirementVersion>> findAllByRequirement(long requirementId, PagingAndSorting pas) {
		List<RequirementVersion> versions = requirementVersionDao.findAllByRequirement(requirementId, pas);
		long versionsCount = requirementVersionDao.countByRequirement(requirementId);

		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(pas, versionsCount, versions);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<RequirementVersion> findAllByRequirement(long requirementId) {
		DefaultPagingAndSorting pas = new DefaultPagingAndSorting("versionNumber", true);
		pas.setSortOrder(SortOrder.DESCENDING);
		return findAllByRequirement(requirementId, pas).getPagedItems();
	}

	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void changeCategory(long requirementVersionId, String categoryCode){
		RequirementVersion version = requirementVersionDao.findById(requirementVersionId);
		InfoListItem category = infoListItemService.findByCode(categoryCode);

		if (infoListItemService.isCategoryConsistent(version.getProject().getId(), categoryCode)){
			version.setCategory(category);
		}
		else{
			throw new InconsistentInfoListItemException("requirementCategory", categoryCode);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')" + OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long versionId) {
		return milestoneManager.findMilestonesForRequirementVersion(versionId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')" + OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestones(long versionId) {
		return milestoneManager.findAssociableMilestonesToRequirementVersion(versionId);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void bindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.bindRequirementVersionToMilestones(versionId, milestoneIds);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void unbindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.unbindRequirementVersionFromMilestones(versionId, milestoneIds);
	}

	@Override
	public Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> reqVersionIds) {
		Collection<Milestone> milestones = null;

		for (Long reqVersionId : reqVersionIds){
			List<Milestone> mil = requirementVersionDao.findById(reqVersionId).getProject().getMilestones();
			if (milestones != null){
				//keep only milestone that in ALL selected requirementVersion
				milestones.retainAll(mil);
			} else {
				//populate the collection for the first time
				milestones = new ArrayList<Milestone>(mil);
			}
		}

		return milestones;
	}

}
