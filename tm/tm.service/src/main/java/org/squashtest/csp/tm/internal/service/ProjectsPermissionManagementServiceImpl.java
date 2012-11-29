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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.core.security.acls.model.ObjectAclService;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectPermission;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;

@Service("squashtest.tm.service.ProjectsPermissionManagementService")
public class ProjectsPermissionManagementServiceImpl implements ProjectsPermissionManagementService {

	private static final String NAMESPACE = "squashtest.acl.group.tm";
	private static final String PROJECT_CLASS_NAME = "org.squashtest.csp.tm.domain.project.Project";

	@Inject
	private ObjectAclService aclService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return aclService.findAllPermissionGroupsByNamespace(NAMESPACE);
	}

	@Override
	public void deleteUserProjectOldPermission(String userLogin, long projectId) {
		ObjectIdentity entityRef = createProjectIdentity(projectId);
		aclService.removeAllResponsibilities(userLogin, entityRef);

		Project project = projectDao.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, clibraryRef);
	}

	private ObjectIdentity createProjectIdentity(long projectId) {
		return new ObjectIdentityImpl(Project.class, projectId);
	}

	private ObjectIdentity createCampaignLibraryIdentity(Project project) {
		return new ObjectIdentityImpl(CampaignLibrary.class, project.getCampaignLibrary().getId());
	}

	private ObjectIdentity createTestCaseLibraryIdentity(Project project) {
		return new ObjectIdentityImpl(TestCaseLibrary.class, project.getTestCaseLibrary()
				.getId());
	}

	private ObjectIdentity createRequirementLibraryIdentity(Project project) {
		return new ObjectIdentityImpl(RequirementLibrary.class, project.getRequirementLibrary()
				.getId());
	}

	@Override
	public List<ProjectPermission> findProjectPermissionByLogin(String userLogin) {
		List<ProjectPermission> newResult = new ArrayList<ProjectPermission>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromUserLogin(userLogin, PROJECT_CLASS_NAME);
		for (Object[] objects : result) {
			Project project = projectDao.findById((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Override
	public List<Project> findProjectWithoutPermissionByLogin(String userLogin) {
		List<Long> idList = aclService.findObjectWithoutPermissionByLogin(userLogin, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return projectDao.findAllByIds(idList);
	}

	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permissionName) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);
		User user = userDao.findById(userId);
		aclService.addNewResponsibility(user.getLogin(), projectRef, permissionName);

		Project project = projectDao.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.addNewResponsibility(user.getLogin(), rlibraryRef, permissionName);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.addNewResponsibility(user.getLogin(), tclibraryRef, permissionName);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.addNewResponsibility(user.getLogin(), clibraryRef, permissionName);

	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);
		User user = userDao.findById(userId);
		aclService.removeAllResponsibilities(user.getLogin(), projectRef);

		Project project = projectDao.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getLogin(), rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getLogin(), tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getLogin(), clibraryRef);
	}

	@Override
	public List<UserProjectPermissionsBean> findUserPermissionsBeanByProject(long projectId) {
		return findUserPermissionBeanByProjectOfGivenType(projectId, Project.class);
	}

	@Override
	public List<User> findUserWithoutPermissionByProject(long projectId) {
		List<Long> idList = aclService.findUsersWithoutPermissionByObject(projectId, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return userDao.findAllByIds(idList);
	}

	/**
	 * @see ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(Project, ProjectTemplate)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project newProject, ProjectTemplate projectTemplate) {
		long templateId = projectTemplate.getId();
		copyAssignedUsersFromTemplate(newProject, templateId);
	}

	private List<UserProjectPermissionsBean> findUserPermissionsBeanByProjectTemplate(long projectId) {
		return findUserPermissionBeanByProjectOfGivenType(projectId, ProjectTemplate.class);
	}

	private List<UserProjectPermissionsBean> findUserPermissionBeanByProjectOfGivenType(long projectId,
			Class<?> projectType) {
		List<UserProjectPermissionsBean> newResult = new ArrayList<UserProjectPermissionsBean>();

		List<Object[]> result = aclService.retriveUserAndAclGroupNameFromIdentityAndClass(projectId, projectType);
		for (Object[] objects : result) {
			User user = userDao.findById((Long) objects[0]);
			newResult.add(new UserProjectPermissionsBean(user, (PermissionGroup) objects[1]));
		}
		return newResult;

	}

	/**
	 * @see org.squashtest.csp.tm.service.ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(org.squashtest.csp.tm.domain.project.Project,
	 *      long)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project project, long templateId) {
		List<UserProjectPermissionsBean> templateUserPermissions = findUserPermissionsBeanByProjectTemplate(templateId);

		for (UserProjectPermissionsBean userPermission : templateUserPermissions) {
			long userId = userPermission.getUser().getId();
			long projectId = project.getId();
			String permissionName = userPermission.getPermissionGroup().getQualifiedName();
			addNewPermissionToProject(userId, projectId, permissionName);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.ProjectsPermissionManagementService#removeAllPermissionsFromProjectTemplate(long)
	 */
	@Override
	public void removeAllPermissionsFromProjectTemplate(long templateId) {
		ObjectIdentity projectRef = new ObjectIdentityImpl(ProjectTemplate.class, templateId);
		aclService.removeAllResponsibilities(projectRef);
	}
}
