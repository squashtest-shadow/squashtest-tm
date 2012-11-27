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

package org.squashtest.csp.tm.internal.service.project;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.core.service.security.ObjectIdentityService;
import org.squashtest.csp.tm.domain.NoBugTrackerBindingException;
import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.csp.tm.internal.repository.BugTrackerBindingDao;
import org.squashtest.csp.tm.internal.repository.BugTrackerDao;
import org.squashtest.csp.tm.internal.repository.GenericProjectDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;
import org.squashtest.csp.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomGenericProjectManager")
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {
	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject private ProjectDao projectDao;
	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@Inject
	private SessionFactory sessionFactory;
	@Inject
	private UserDao userDao;
	@Inject
	private ObjectIdentityService objectIdentityService;
	@Inject
	private Provider<GenericToAdministrableProject> genericToAdministrableConvertor;
	@Inject
	private ProjectsPermissionManagementService permissionsManager;
	@Inject
	private InsecureTestAutomationManagementService autotestService;

	private static final String MANAGE_PROJECT_OR_ROLE_ADMIN = "hasPermission(#projectId, 'org.squashtest.csp.tm.domain.project.GenericProject', 'MANAGEMENT') or hasRole('ROLE_ADMIN')";
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomGenericProjectManagerImpl.class);

	/**
	 * @see org.squashtest.csp.tm.service.project.CustomGenericProjectManager#findSortedProjects(org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting) {
		List<GenericProject> projects = genericProjectDao.findAll(pagingAndSorting);
		long count = genericProjectDao.countGenericProjects();
		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(pagingAndSorting, count, projects);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void persist(GenericProject project) {
		Session session = sessionFactory.getCurrentSession();

		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		session.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		session.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		session.persist(tcl);

		session.persist(project);
		session.flush(); // otherwise ids not available

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());

	}

	/**
	 * @see org.squashtest.csp.tm.service.project.CustomGenericProjectManager#coerceTemplateIntoProject(long)
	 */
	@Override
	public void coerceTemplateIntoProject(long templateId) {
		Project project = genericProjectDao.coerceTemplateIntoProject(templateId);

		objectIdentityService.addObjectIdentity(templateId, Project.class);
		permissionsManager.copyAssignedUsersFromTemplate(project, templateId);
		permissionsManager.removeAllPermissionsFromProjectTemplate(templateId);
		objectIdentityService.removeObjectIdentity(templateId, ProjectTemplate.class);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteProject(long projectId) {
		// TODO visit project and check for folder before delete if is not template
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public AdministrableProject findAdministrableProjectById(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		return genericToAdministrableConvertor.get().convertToAdministrableProject(genericProject);
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		permissionsManager.addNewPermissionToProject(userId, projectId, permission);
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public void removeProjectPermission(long userId, long projectId) {
		permissionsManager.removeProjectPermission(userId, projectId);

	}

	@Override
	public List<UserProjectPermissionsBean> findUserPermissionsBeansByProject(long projectId) {
		return permissionsManager.findUserPermissionsBeanByProject(projectId);
	}

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return permissionsManager.findAllPossiblePermission();
	}

	@Override
	public List<User> findUserWithoutPermissionByProject(long projectId) {
		return permissionsManager.findUserWithoutPermissionByProject(projectId);
	}

	@Override
	public User findUserByLogin(String userLogin) {
		return userDao.findUserByLogin(userLogin);
	}

	// ********************************** Test automation section *************************************

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public void bindTestAutomationProject(long projectId, TestAutomationProject taProject) {
		TestAutomationProject persistedProject = autotestService.persistOrAttach(taProject);
		genericProjectDao.findById(projectId).bindTestAutomationProject(persistedProject);
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public TestAutomationServer getLastBoundServerOrDefault(long projectId) {
		GenericProject project = genericProjectDao.findById(projectId);
		if (project.hasTestAutomationProjects()) {
			return project.getServerOfLatestBoundProject();
		}

		else {
			return autotestService.getDefaultServer();
		}
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public List<TestAutomationProject> findBoundTestAutomationProjects(long projectId) {
		return genericProjectDao.findBoundTestAutomationProjects(projectId);
	}

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
	public void unbindTestAutomationProject(long projectId, long taProjectId) {
		GenericProject project = genericProjectDao.findById(projectId);
		project.unbindTestAutomationProject(taProjectId);

	}

	// ********************************** bugtracker section *************************************

	@Override
	@PreAuthorize(MANAGE_PROJECT_OR_ROLE_ADMIN)
		public void changeBugTracker(long projectId, Long newBugtrackerId) {

		GenericProject project = genericProjectDao.findById(projectId);
		BugTracker newBugtracker = bugTrackerDao.findById(newBugtrackerId);
		if (newBugtracker != null) {
			changeBugTracker(project, newBugtracker);
		} else {
			throw new UnknownEntityException(newBugtrackerId, BugTracker.class);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#project, 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
		public void changeBugTracker(GenericProject project, BugTracker newBugtracker) {
		LOGGER.debug("changeBugTracker for project " + project.getId() + " bt: " + newBugtracker.getId());

		// the project doesn't have bug-tracker connection yet
		if (!project.isBugtrackerConnected()) {
			BugTrackerBinding bugTrackerBinding = new BugTrackerBinding(project.getName(), newBugtracker, project);
			project.setBugtrackerBinding(bugTrackerBinding);
		}
		// the project has a bug-tracker connection
		else {
			// and the new one is different from the old one
			if (projectBugTrackerChanges(newBugtracker.getId(), project)) {
				project.getBugtrackerBinding().setBugtracker(newBugtracker);
			}
		}
	}

	private boolean projectBugTrackerChanges(Long newBugtrackerId, GenericProject project) {
		boolean change = true;
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		long bugtrackerId = bugtrackerBinding.getBugtracker().getId();
		if (bugtrackerId == newBugtrackerId) {
			change = false;
		}
		return change;
	}

	@Override
	public void removeBugTracker(long projectId) {
		LOGGER.debug("removeBugTracker for project " + projectId);
		GenericProject project = genericProjectDao.findById(projectId);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			project.removeBugTrackerBinding();
			bugTrackerBindingDao.remove(bugtrackerBinding);
		}
	}

	@Override
	public void changeBugTrackerProjectName(long projectId, String projectBugTrackerName) {
		GenericProject project = genericProjectDao.findById(projectId);
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		if (bugtrackerBinding == null) {
			throw new NoBugTrackerBindingException();
		}
		bugtrackerBinding.setProjectName(projectBugTrackerName);

	}

}
