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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao.HolderConsumer;
import org.squashtest.tm.service.milestone.CustomMilestoneManager;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomMilestoneManager")
public class CustomMilestoneManagerServiceImpl implements CustomMilestoneManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomMilestoneManagerServiceImpl.class);

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private UserContextService userContextService;

	@Inject
	private UserAccountService userService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private SessionFactory sessionFactory;

	@Override
	public void addMilestone(Milestone milestone) {
		milestoneDao.checkLabelAvailability(milestone.getLabel());
		milestone.setOwner(userService.findCurrentUser());
		milestoneDao.persist(milestone);
	}

	@Override
	public List<Milestone> findAll() {
		return milestoneDao.findAll();
	}

	@Override
	public void removeMilestones(Collection<Long> ids) {
		for (final Long id : ids) {
			Milestone milestone = milestoneDao.findById(id);
			deleteMilestoneBinding(milestone);
			deleteMilestone(milestone);
		}
	}

	private void deleteMilestoneBinding(final Milestone milestone) {
		List<GenericProject> projects = milestone.getProjects();
		for (GenericProject project : projects) {
			project.unbindMilestone(milestone);
		}
	}

	private void deleteMilestone(final Milestone milestone) {

		milestoneDao.remove(milestone);
	}

	@Override
	public Milestone findById(long milestoneId) {
		return milestoneDao.findById(milestoneId);
	}

	@Override
	public void verifyCanEditMilestone(long milestoneId) {
		if (!canEditMilestone(milestoneId)) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}
	}

	private boolean isGlobal(Milestone milestone) {
		boolean isGlobal = false;
		if (milestone.getRange().equals(MilestoneRange.GLOBAL)) {
			isGlobal = true;
		}
		return isGlobal;
	}

	private boolean isCreatedBySelf(Milestone milestone) {
		boolean isCreatedBySelf = false;
		String myName = userContextService.getUsername();
		if (myName.equals(milestone.getOwner().getLogin())) {
			isCreatedBySelf = true;
		}
		return isCreatedBySelf;
	}

	@Override
	public void verifyCanEditMilestoneRange() {
		// only admin can edit range
		if (!permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}

	}

	@Override
	public boolean canEditMilestone(long milestoneId) {
		Milestone milestone = milestoneDao.findById(milestoneId);
		// admin can edit all milestones
		if (!permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			// project manager can't edit global milestone or milestone they don't own
			if (isGlobal(milestone) || !isCreatedBySelf(milestone)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Long> findAllIdsOfEditableMilestone() {
		List<Milestone> milestones = findAll();
		List<Long> ids = new ArrayList<Long>();
		for (Milestone milestone : milestones) {
			if (canEditMilestone(milestone.getId())) {
				ids.add(milestone.getId());
			}
		}
		return ids;
	}

	@Override
	public List<Milestone> findAllVisibleToCurrentManager() {

		List<Milestone> allMilestones = findAll();
		List<Milestone> milestones = new ArrayList<Milestone>();

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			milestones.addAll(allMilestones);
		} else {
			for (Milestone milestone : allMilestones) {
				if (isGlobal(milestone) || isCreatedBySelf(milestone) || isInAProjetICanManage(milestone)) {
					milestones.add(milestone);
				}
			}
		}
		return milestones;
	}

	// security provided by projectFinder.findAllReadable(); is enough here
	@Override
	public List<Milestone> findAllVisibleToCurrentUser() {

		Set<Milestone> allMilestones = new HashSet<>();

		Collection<Project> projects = projectFinder.findAllReadable();

		for (Project p : projects) {
			allMilestones.addAll(p.getMilestones());
		}

		return new ArrayList<>(allMilestones);
	}

	private boolean isInAProjetICanManage(Milestone milestone) {
		boolean isInAProjetICanManage = false;
		List<GenericProject> perimeter = milestone.getPerimeter();

		for (GenericProject project : perimeter) {
			if (canIManageThisProject(project)) {
				isInAProjetICanManage = true;
				break;
			}
		}
		return isInAProjetICanManage;
	}

	private boolean canIManageThisProject(GenericProject project) {
		if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)) {
			return true;
		}
		return false;
	}

	private List<GenericProject> getProjectICanManage(Collection<GenericProject> projects) {

		List<GenericProject> manageableProjects = new ArrayList<GenericProject>();

		for (GenericProject project : projects) {
			if (canIManageThisProject(project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

	@Override
	public boolean isBoundToATemplate(Long milestoneId) {
		Milestone milestone = findById(milestoneId);
		return milestone.isBoundToATemplate();
	}

	@Override
	public void cloneMilestone(long motherId, Milestone milestone, boolean bindToRequirements, boolean bindToTestCases,
			boolean bindToCampaigns) {
		Milestone mother = findById(motherId);
		boolean copyAllPerimeter = permissionEvaluationService.hasRole("ROLE_ADMIN") || !isGlobal(mother)
				&& isCreatedBySelf(mother);

		bindProjectsAndPerimeter(mother, milestone, copyAllPerimeter);
		bindRequirements(mother, milestone, bindToRequirements, copyAllPerimeter);
		bindTestCases(mother, milestone, bindToTestCases, copyAllPerimeter);
		bindCampaigns(mother, milestone, bindToCampaigns, copyAllPerimeter);
		addMilestone(milestone);
	}

	@Override
	public void migrateMilestones(MilestoneHolder member) {

		Collection<Milestone> projectMilestones = member.getProject().getMilestones();
		Collection<Milestone> memberMilestones = member.getMilestones();

		Iterator<Milestone> memberIterator = memberMilestones.iterator();
		while (memberIterator.hasNext()) {
			Milestone m = memberIterator.next();
			if (!projectMilestones.contains(m)) {
				memberIterator.remove();
			}
		}
	}

	private void bindProjectsAndPerimeter(Milestone mother, Milestone milestone, boolean copyAllPerimeter) {

		if (copyAllPerimeter) {
			milestone.bindProjects(mother.getProjects());
			milestone.addProjectsToPerimeter(mother.getPerimeter());
		} else {
			milestone.bindProjects(projectFinder.findAllICanManage());
			milestone.addProjectsToPerimeter(projectFinder.findAllICanManage());

		}

	}

	private void bindCampaigns(Milestone mother, Milestone milestone, boolean bindToCampaigns, boolean copyAllPerimeter) {
		if (bindToCampaigns) {
			for (Campaign camp : mother.getCampaigns()) {
				if (copyAllPerimeter || canIManageThisProject(camp.getProject())) {
					milestone.bindCampaign(camp);
				}
			}
		}
	}

	private void bindTestCases(Milestone mother, Milestone milestone, boolean bindToTestCases, boolean copyAllPerimeter) {
		if (bindToTestCases) {
			for (TestCase tc : mother.getTestCases()) {
				if (copyAllPerimeter || canIManageThisProject(tc.getProject())) {
					milestone.bindTestCase(tc);
				}
			}
		}
	}

	private void bindRequirements(Milestone mother, Milestone milestone, boolean bindToRequirements,
			boolean copyAllPerimeter) {
		if (bindToRequirements) {
			for (RequirementVersion req : mother.getRequirementVersions()) {
				if (copyAllPerimeter || canIManageThisProject(req.getProject())) {
					milestone.bindRequirementVersion(req);
				}
			}
		}
	}

	@Override
	public void synchronize(long sourceId, long targetId, boolean extendPerimeter, boolean isUnion) {

		Milestone source = findById(sourceId);
		Milestone target = findById(targetId);
		verifyCanSynchronize(source, target, isUnion);
		synchronizePerimeterAndProjects(source, target, extendPerimeter, isUnion);
		synchronizeTestCases(source, target, isUnion, extendPerimeter);
		synchronizeRequirementVersions(source, target, isUnion, extendPerimeter);
		synchronizeCampaigns(source, target, isUnion, extendPerimeter);
	}

	private void verifyCanSynchronize(Milestone source, Milestone target, boolean isUnion) {

		if (isUnion
				&& (source.getStatus() != MilestoneStatus.IN_PROGRESS || !permissionEvaluationService
						.hasRole("ROLE_ADMIN") && isGlobal(source))) {
			throw new IllegalArgumentException(
					"milestone can't be synchronized because it's status or range don't allow it");
		}

		if (target.getStatus() != MilestoneStatus.IN_PROGRESS || !permissionEvaluationService.hasRole("ROLE_ADMIN")
				&& isGlobal(target)) {
			throw new IllegalArgumentException(
					"milestone can't be synchronized because it's status or range don't allow it");
		}

	}

	private void synchronizeCampaigns(Milestone source, Milestone target, boolean isUnion, boolean extendPerimeter) {

		milestoneDao.synchronizeCampaigns(source.getId(), target.getId(),
				getProjectsToSynchronize(source, target, extendPerimeter, isUnion));
		if (isUnion) {
			milestoneDao.synchronizeCampaigns(target.getId(), source.getId(),
					getProjectsToSynchronize(target, source, extendPerimeter, isUnion));
		}
	}

	private void synchronizeRequirementVersions(Milestone source, Milestone target, boolean isUnion,
			boolean extendPerimeter) {
		milestoneDao.synchronizeRequirementVersions(source.getId(), target.getId(),
				getProjectsToSynchronize(source, target, extendPerimeter, isUnion));
		if (isUnion) {
			milestoneDao.synchronizeRequirementVersions(target.getId(), source.getId(),
					getProjectsToSynchronize(target, source, extendPerimeter, isUnion));
		}
	}

	private void synchronizeTestCases(Milestone source, Milestone target, boolean isUnion, boolean extendPerimeter) {
		milestoneDao.synchronizeTestCases(source.getId(), target.getId(),
				getProjectsToSynchronize(source, target, extendPerimeter, isUnion));
		if (isUnion) {
			milestoneDao.synchronizeTestCases(target.getId(), source.getId(),
					getProjectsToSynchronize(target, source, extendPerimeter, isUnion));
		}
	}

	private List<Long> getProjectsToSynchronize(Milestone source, Milestone target, boolean extendPerimeter,
			boolean isUnion) {

		Set<GenericProject> result = new HashSet<GenericProject>(source.getPerimeter());
		if (permissionEvaluationService.hasRole("ROLE_ADMIN") || extendPerimeter && isCreatedBySelf(target)) {
			result.addAll(target.getPerimeter());
		} else {
			result.retainAll(target.getPerimeter());
			if (!isCreatedBySelf(target)) {
				result.retainAll(getProjectICanManage(result));
			}
		}
		List<Long> ids = new ArrayList<Long>();
		for (GenericProject p : result) {
			ids.add(p.getId());
		}
		return ids;
	}

	private void adminSynchronize(Milestone source, Milestone target, boolean isUnion) {
		if (isUnion) {
			source.bindProjects(target.getProjects());
			source.addProjectsToPerimeter(target.getPerimeter());
			target.bindProjects(source.getProjects());
			target.addProjectsToPerimeter(source.getPerimeter());
		} else {
			target.bindProjects(source.getProjects());
			target.addProjectsToPerimeter(source.getPerimeter());
		}
	}

	private void projectManagerSynchronize(Milestone source, Milestone target, boolean isUnion, boolean extendPerimeter) {
		if (isUnion) {
			if (isCreatedBySelf(target)) {
				target.bindProjects(source.getProjects());
				target.addProjectsToPerimeter(source.getPerimeter());
			}
			if (isCreatedBySelf(source)) {
				source.bindProjects(target.getProjects());
				source.addProjectsToPerimeter(target.getPerimeter());
			}

		} else {
			if (isCreatedBySelf(target) && extendPerimeter) {
				// can extend perimeter only if own milestone
				target.bindProjects(source.getProjects());
				target.addProjectsToPerimeter(source.getPerimeter());
			}
		}
	}

	private void synchronizePerimeterAndProjects(Milestone source, Milestone target, boolean extendPerimeter,
			boolean isUnion) {

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			adminSynchronize(source, target, isUnion);
		} else {
			projectManagerSynchronize(source, target, isUnion, extendPerimeter);
		}
	}

	/**
	 * @see org.squashtest.tm.service.milestone.CustomMilestoneManager#enableFeature()
	 */
	@Override
	public void enableFeature() {
		// NOOP (AFAIK)

	}

	/**
	 * @see org.squashtest.tm.service.milestone.CustomMilestoneManager#disableFeature()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void disableFeature() {
		LOGGER.info("Disabling the Milestones feature : I am about to nuke all milestones from database");

		milestoneDao.performBatchUpdate(new HolderConsumer() {
			@Override
			public void consume(MilestoneHolder holder) {
				holder.unbindAllMilestones();
			}
		});

		Session session = sessionFactory.getCurrentSession();
		List<Milestone> milestones = session.createQuery("from Milestone").list();

		for (Milestone milestone : milestones) {
			milestone.unbindAllProjects();
			milestone.clearPerimeter();
			session.delete(milestone);
		}
	}

	@Override
	public boolean isBoundToAtleastOneObject(long milestoneId) {
		return 	milestoneDao.isBoundToAtleastOneObject(milestoneId);
	}

	@Override
	public void unbindAllObjects(long milestoneId) {
		
		milestoneDao.unbindAllObjects(milestoneId);
		Milestone milestone = findById(milestoneId);
		milestone.clearObjects();
		}
	
	
}
