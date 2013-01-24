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
package org.squashtest.tm.service.security.acls.model;

import java.util.List;

import org.springframework.security.acls.model.ObjectIdentity;
import org.squashtest.tm.service.security.acls.PermissionGroup;

public interface ObjectAclService {
	
	List<PermissionGroup> findAllPermissionGroupsByNamespace(String namespace);

	void removeAllResponsibilities(String userLogin, ObjectIdentity entityRef);

	/**
	 * Removes ALL RESPONSIBILITIES from the given object.
	 * 
	 * @param entityRef
	 */
	void removeAllResponsibilities(ObjectIdentity entityRef);

	List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, String qualifiedClassName);

	List<Long> findObjectWithoutPermissionByLogin(String userLogin, String qualifiedClass);

	void addNewResponsibility(String userLogin, ObjectIdentity entityRef, String qualifiedName);

	List<String> findUsersWithWritePermission(List<ObjectIdentity> entityRefs);
	/**
	 * Will find squash User ids and theirs permission names for the given acl_object_identity.identity and acl_object_identity.className
	 * @param objectId : the acl_object_identity.identity
	 * @param objectClassName : the acl_object_identity.acl_class.className
	 * @return a list of Object[] containing at index 0 the user id and , at index 1, the user's acl_group.qualified_name for the given acl_object.
	 */
	List<Object[]> retriveUserAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass);

	List<Long> findUsersWithoutPermissionByObject(long objectId, String qualifiedClassName);

	List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs);
}
