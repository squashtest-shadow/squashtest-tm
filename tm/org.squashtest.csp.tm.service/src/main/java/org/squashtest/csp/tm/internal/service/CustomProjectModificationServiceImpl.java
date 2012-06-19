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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.CannotDeleteProjectException;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.CustomProjectModificationService;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;

/**
 * 
 * @author mpagnon
 * 
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProjectModificationServiceImpl.class);
	@Inject
	private ProjectDao projectDao;
	@Inject
	private UserDao userDao;
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	@Inject
	private ProjectsPermissionManagementService permissionService;

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	public Project findById(long projectId) {
		return projectDao.findById(projectId);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	public AdministrableProject findAdministrableProjectById(long projectId) {
		Project project = findById(projectId);
		boolean isDeletable = true;
		try {
			projectDeletionHandler.checkProjectContainsOnlyFolders(projectId);
		} catch (CannotDeleteProjectException e) {
			isDeletable = false;
		}
		AdministrableProject administrableProject = new AdministrableProject(project);
		administrableProject.setDeletable(isDeletable);
		return administrableProject;
	}

	@Override
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		permissionService.addNewPermissionToProject(userId, projectId, permission);

	}

	@Override
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.csp.tm.domain.project.Project', 'MANAGEMENT') or hasRole('ROLE_ADMIN')")
	public void removeProjectPermission(long userId, long projectId) {
		permissionService.removeProjectPermission(userId, projectId);

	}

	@Override
	public List<UserProjectPermissionsBean> findUserPermissionsBeansByProject(long projectId) {
		return permissionService.findUserPermissionsBeanByProject(projectId);
	}

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return permissionService.findAllPossiblePermission();
	}

	@Override
	public List<User> findUserWithoutPermissionByProject(long projectId) {
		return permissionService.findUserWithoutPermissionByProject(projectId);
	}

	@Override
	public User findUserByLogin(String userLogin) {
		return userDao.findUserByLogin(userLogin);
	}
}
