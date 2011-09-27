/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectPermission;

public interface ProjectsPermissionManagementService {

	List<PermissionGroup> findAllPossiblePermission();
	
	void deleteUserProjectOldPermission(String userLogin, long projectId);
	
	void addNewPermissionToProject(long userId, long projectId, String permissionName);
	
	List<ProjectPermission> findProjectPermissionByLogin(String userLogin);
	
	List<Project> findProjectWithoutPermissionByLogin(String userLogin);
	
	void removeProjectPermission(long userId, long projectId);
}
