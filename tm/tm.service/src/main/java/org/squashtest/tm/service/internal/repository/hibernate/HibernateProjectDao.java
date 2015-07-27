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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.internal.repository.CustomProjectDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.acls.jdbc.JdbcManageableAclService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;


@Repository("CustomProjectDao")
public class HibernateProjectDao extends HibernateEntityDao<Project> implements CustomProjectDao {

	@Inject
	private ObjectAclService aclService;

	@Inject
	private PartyDao partyDao;

	@Inject
	private ProjectsPermissionManagementService projectsPermissionManagementService;

	@Inject
	private JdbcManageableAclService jdbcManageableAclService;
	@Override
	public long countNonFoldersInProject(long projectId) {
		Long req = (Long) executeEntityNamedQuery("project.countNonFolderInRequirement", idParameter(projectId));
		Long tc = (Long) executeEntityNamedQuery("project.countNonFolderInTestCase", idParameter(projectId));
		Long camp = (Long) executeEntityNamedQuery("project.countNonFolderInCampaign", idParameter(projectId));

		return req + tc + camp;
	}

	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter(ParameterNames.PROJECT_ID, id);
	}

	private SetQueryParametersCallback idParameters(final List<Long> ids) {
		return new SetProjectIdsParameterCallback(ids);
	}

	@Override
	public List<String> findUsersWhoCreatedTestCases(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoCreatedTestCases", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoModifiedTestCases(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoModifiedTestCases", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoCreatedRequirementVersions(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoCreatedRequirementVersions", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoModifiedRequirementVersions(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoModifiedRequirementVersions", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoCanAccessProject(List<Long> projectIds) {

		List<String> list = new ArrayList<>();

		List<PartyProjectPermissionsBean> findPartyPermissionBeanByProject = new ArrayList<PartyProjectPermissionsBean>();

		for (Long projectId : projectIds) {
			findPartyPermissionBeanByProject.addAll(projectsPermissionManagementService
					.findPartyPermissionsBeanByProject(projectId));
		}

		for (PartyProjectPermissionsBean partyProjectPermissionsBean : findPartyPermissionBeanByProject) {
			if (partyProjectPermissionsBean.isUser()) {
				list.add(partyProjectPermissionsBean.getParty().getName());
			}
		}
		return list;

	}
}
