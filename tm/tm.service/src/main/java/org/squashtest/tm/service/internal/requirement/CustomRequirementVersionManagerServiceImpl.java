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

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
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
	private MilestoneDao milestoneDao;

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

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public Requirement findRequirementById(long requirementId) {
		return requirementVersionDao.findRequirementById(requirementId);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void createNewVersion(long requirementId) {
		Requirement req = requirementVersionDao.findRequirementById(requirementId);
		RequirementVersion previousVersion = req.getCurrentVersion();

		req.increaseVersion();
		em.unwrap(Session.class).persist(req.getCurrentVersion());
		RequirementVersion newVersion = req.getCurrentVersion();
		indexationService.reindexRequirementVersions(req.getRequirementVersions());
		customFieldValueService.copyCustomFieldValues(previousVersion, newVersion);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE')"
			+ OR_HAS_ROLE_ADMIN)
	public void createNewVersion(long requirementId, Collection<Long> milestoneIds) {

		createNewVersion(requirementId);
		Requirement req = requirementVersionDao.findRequirementById(requirementId);

		for (RequirementVersion version : req.getRequirementVersions()) {
			for (Long mid : milestoneIds) {
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
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
			+ OR_HAS_ROLE_ADMIN)
	public void changeCriticality(long requirementVersionId, RequirementCriticality criticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findOne(requirementVersionId);
		RequirementCriticality oldCriticality = requirementVersion.getCriticality();
		requirementVersion.setCriticality(criticality);
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId,
				oldCriticality);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
			+ OR_HAS_ROLE_ADMIN)
	public void rename(long requirementVersionId, String newName) {
		RequirementVersion v = requirementVersionDao.findOne(requirementVersionId);

		/*
		 * FIXME : there is a loophole here. What exactly means DuplicateNameException for requirements, that can have
		 * multiple names (one for each version) ? What happens when the library is displayed in milestone mode and that
		 * two versions of different requirements happens to have the same name and same milestone (hint : they would be
		 * displayed both anyway).
		 *
		 * Because of this we are waiting for better specs on that matter, and the implementation here remains trivial
		 * in the mean time.
		 */

		v.setName(newName.trim());
	}

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#findAllByRequirement(long,
	 *      org.springframework.data.Pageable)
	 */
	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Page<RequirementVersion> findAllByRequirement(long requirementId, Pageable pageable) {

		Page<RequirementVersion> page = requirementVersionDao.findAllByRequirementId(requirementId, pageable);

                return page;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public List<RequirementVersion> findAllByRequirement(long requirementId) {
                Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.DESC, "versionNumber");
		return findAllByRequirement(requirementId, pageable).getContent();
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
			+ OR_HAS_ROLE_ADMIN)
	public void changeCategory(long requirementVersionId, String categoryCode) {
		RequirementVersion version = requirementVersionDao.findOne(requirementVersionId);
		InfoListItem category = infoListItemService.findByCode(categoryCode);

		if (infoListItemService.isCategoryConsistent(version.getProject().getId(), categoryCode)) {
			version.setCategory(category);
		} else {
			throw new InconsistentInfoListItemException("requirementCategory", categoryCode);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long versionId) {
		return milestoneManager.findMilestonesForRequirementVersion(versionId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestones(long versionId) {
		return milestoneManager.findAssociableMilestonesToRequirementVersion(versionId);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
			+ OR_HAS_ROLE_ADMIN)
	public void bindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.bindRequirementVersionToMilestones(versionId, milestoneIds);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
			+ OR_HAS_ROLE_ADMIN)
	public void unbindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.unbindRequirementVersionFromMilestones(versionId, milestoneIds);
	}

	@Override
	public Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> reqVersionIds) {
		Collection<Milestone> milestones = null;

		for (Long reqVersionId : reqVersionIds) {
			List<Milestone> mil = requirementVersionDao.findOne(reqVersionId).getProject().getMilestones();
			if (milestones != null) {
				// keep only milestone that in ALL selected requirementVersion
				milestones.retainAll(mil);
			} else {
				// populate the collection for the first time
				milestones = new ArrayList<>(mil);
			}
		}
		filterLockedAndPlannedStatus(milestones);
		return milestones;
	}

	private void filterLockedAndPlannedStatus(Collection<Milestone> milestones) {
		CollectionUtils.filter(milestones, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {

				return ((Milestone) milestone).getStatus() != MilestoneStatus.LOCKED
						&& ((Milestone) milestone).getStatus() != MilestoneStatus.PLANNED;
			}
		});
	}

	@Override
	public Collection<Long> findBindedMilestonesIdForMassModif(List<Long> reqVersionIds) {
		Collection<Milestone> milestones = null;

		for (Long reqVersionId : reqVersionIds) {
			Set<Milestone> mil = requirementVersionDao.findOne(reqVersionId).getMilestones();
			if (milestones != null) {
				// keep only milestone that in ALL selected requirementVersion
				milestones.retainAll(mil);
			} else {
				// populate the collection for the first time
				milestones = new ArrayList<>(mil);
			}
		}
		filterLockedAndPlannedStatus(milestones);
		return CollectionUtils.collect(milestones, new Transformer() {

			@Override
			public Object transform(Object milestone) {

				return ((Milestone) milestone).getId();
			}
		});
	}

	@Override
	public boolean haveSamePerimeter(List<Long> reqVersionIds) {

		if (reqVersionIds.size() != 1) {

			Long first = reqVersionIds.remove(0);
			List<Milestone> toCompare = requirementVersionDao.findOne(first).getProject().getMilestones();

			for (Long reqVersionId : reqVersionIds) {
				List<Milestone> mil = requirementVersionDao.findOne(reqVersionId).getProject().getMilestones();

				if (mil.size() != toCompare.size() || !mil.containsAll(toCompare)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds) {
		return milestoneDao.isOneMilestoneAlreadyBindToAnotherRequirementVersion(reqVIds, milestoneIds);

	}

	@Override
	public Long findReqVersionIdByRequirementAndVersionNumber(
			long requirementId, Integer versionNumber) {
		RequirementVersion requirementVersion = requirementVersionDao.findByRequirementIdAndVersionNumber(requirementId,versionNumber);
		if (requirementVersion!=null) {
			return requirementVersion.getId();
		}
		return null;
	}



}
