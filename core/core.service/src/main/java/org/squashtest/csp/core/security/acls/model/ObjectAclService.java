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
package org.squashtest.csp.core.security.acls.model;

import java.util.List;

import org.springframework.security.acls.model.ObjectIdentity;
import org.squashtest.csp.core.security.acls.PermissionGroup;

public interface ObjectAclService {
	
	List<PermissionGroup> findAllPermissionGroupsByNamespace(String namespace);

	void removeAllResponsibilities(String userLogin, ObjectIdentity entityRef);
	
	List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, String qualifiedClassName);
	
	List<Long> findObjectWithoutPermissionByLogin(String userLogin, String qualifiedClass);
	
	void addNewResponsibility(String userLogin, ObjectIdentity entityRef, String qualifiedName);
	
	List<String> findUsersWithWritePermission(List<ObjectIdentity> entityRefs);

	List<Object[]> retrieveUserAndAclClassFromProject(long projectId, String projectClassName);

	List<Long> findUsersWithoutPermissionByObject(long objectId, String qualifiedClassName);

	List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs);
}
