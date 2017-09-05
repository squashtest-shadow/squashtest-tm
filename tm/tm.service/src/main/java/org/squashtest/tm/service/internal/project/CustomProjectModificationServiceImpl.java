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
package org.squashtest.tm.service.internal.project;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.jooq.domain.Tables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * @author mpagnon
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	@Inject
	private ProjectTemplateDao projectTemplateDao;
	@Inject
	private GenericProjectManagerService genericProjectManager;
	@Inject
	private ProjectDao projectDao;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private GenericProjectDao genericProjectDao;

	@Inject
	private UserDao userDao;

	@Inject
	private TeamDao teamDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Project> findAllReadable() {
		return projectDao.findAll();
	}

	@Override
	public Project addProjectFromtemplate(Project newProject, long templateId,
										  GenericProjectCopyParameter params)
		throws NameAlreadyInUseException {
		genericProjectManager.persist(newProject);

		ProjectTemplate projectTemplate = projectTemplateDao.findOne(templateId);
		genericProjectManager.synchronizeGenericProject(newProject, projectTemplate, params);

		return newProject;
	}


	@Override
	public List<GenericProject> findAllICanManage() {
		List<GenericProject> projects = genericProjectDao.findAll();
		List<GenericProject> manageableProjects = new ArrayList<>();

		for (GenericProject project : projects) {
			if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

	/**
	 * Optimized implementation with SQL and no hibernate entities.
	 */
	@Override
	public List<Long> findAllReadableIds() {
		String username = UserContextHolder.getUsername();
		Long userId = userDao.findUserId(username);
		boolean isAdmin = permissionEvaluationService.hasRole(Authorizations.ROLE_ADMIN);

		if (isAdmin) {
			return projectDao.findAllProjectIds();
		} else {
			//1 We must merge team id with user id.
			List<Long> partyIds = teamDao.findTeamIds(userId);
			partyIds.add(userId);
			//2 We must retrieve the set of projects ids that all this core parties can read.
			// by definition, all profile that give access to a project give access at least with read authorization (ie you cannot write or anything else if you can't read...)
			return projectDao.findAllProjectIds(partyIds);

		}

	}

}
