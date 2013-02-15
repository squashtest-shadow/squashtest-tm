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
package org.squashtest.tm.service.project;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.tm.service.security.acls.PermissionGroup;

/**
 * @author mpagnon
 * 
 */
@Transactional(readOnly = true)
public interface ProjectsPermissionFinder {

	List<PermissionGroup> findAllPossiblePermission();

	List<ProjectPermission> findProjectPermissionByLogin(String userLogin);

	List<Project> findProjectWithoutPermissionByLogin(String userLogin);

	List<ProjectPermission> findProjectPermissionByParty(long partyId);

	List<Project> findProjectWithoutPermissionByParty(long partyId);
	
	List<UserProjectPermissionsBean> findUserPermissionsBeanByProject(long projectId);	
	
	PagedCollectionHolder<List<UserProjectPermissionsBean>> findUserPermissionsBeanByProject(PagingAndSorting sorting, Filtering filtering, long projectId);

	List<PartyProjectPermissionsBean> findPartyPermissionsBeanByProject(long projectId);	
	
	PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(PagingAndSorting sorting, Filtering filtering, long projectId);
	
	List<User> findUserWithoutPermissionByProject(long projectId);

	List<Party> findPartyWithoutPermissionByProject(long projectId);
	/**
	 * @param userId
	 * @param projectId
	 */
	void removeProjectPermission(long userId, long projectId);
	
	public boolean isInPermissionGroup(String userLogin, Long projectId, String permissionGroup);
}
