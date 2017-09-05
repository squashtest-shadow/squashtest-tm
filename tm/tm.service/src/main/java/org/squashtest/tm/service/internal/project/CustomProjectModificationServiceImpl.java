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
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.GenericProjectManagerService;
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
	private DSLContext DSL;

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


		Long userId = DSL
			.select(CORE_USER.PARTY_ID)
			.from(CORE_USER)
			.where(CORE_USER.LOGIN.eq(username))
			.fetchOne(CORE_USER.PARTY_ID);

		Integer countAdmin = DSL.select(CORE_PARTY.PARTY_ID.count())
			.from(CORE_USER)
				.join(CORE_GROUP_MEMBER).using(CORE_USER.PARTY_ID)
				.join(CORE_GROUP).using(CORE_GROUP_MEMBER.GROUP_ID)
			.where(CORE_GROUP.QUALIFIED_NAME.eq(UsersGroup.ADMIN)
				.and(CORE_PARTY.PARTY_ID.eq(userId)))
			.fetchOne().value1();

		Boolean isAdmin = countAdmin > 0;

		if (isAdmin) {
			return DSL.select(PROJECT.PROJECT_ID)
				.from(PROJECT)
				.where(PROJECT.PROJECT_TYPE.eq("P"))
				.fetch(PROJECT.PROJECT_ID, Long.class);
		} else {
			//1 We must merge team id with user id.
			List<Long> partyIds = DSL.select(CORE_TEAM.PARTY_ID)
				.from(CORE_TEAM)
				.join(CORE_TEAM_MEMBER).on(CORE_TEAM_MEMBER.TEAM_ID.eq(CORE_TEAM.PARTY_ID))
				.where(CORE_TEAM_MEMBER.USER_ID.eq(userId))
				.fetch(CORE_TEAM.PARTY_ID, Long.class);

			partyIds.add(userId);

			//2 We must retrieve the set of projects ids that all this core parties can read.

		}
		return null;

	}

}
